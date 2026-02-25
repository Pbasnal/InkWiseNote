package com.originb.inkwisenote2.modules.ocr.worker

import android.content.Context
import android.graphics.Bitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import com.originb.inkwisenote2.common.BitmapScale
import com.originb.inkwisenote2.common.Logger
import com.originb.inkwisenote2.config.AppConfig
import com.originb.inkwisenote2.config.AppSecrets
import com.originb.inkwisenote2.config.ConfigReader.Companion.getInstance
import com.originb.inkwisenote2.config.ConfigReader.Companion.isAzureOcrEnabled
import com.originb.inkwisenote2.functionalUtils.Try
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus.scheduleWorkForFindingRelatedNotesForBook
import com.originb.inkwisenote2.modules.backgroundjobs.WorkManagerBus.scheduleWorkForTextProcessingForBook
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteEntity
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteRepository
import com.originb.inkwisenote2.modules.handwrittennotes.data.HandwrittenNoteWithImage
import com.originb.inkwisenote2.modules.handwrittennotes.data.Stroke
import com.originb.inkwisenote2.modules.ocr.data.AzureOcrResult
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrText
import com.originb.inkwisenote2.modules.ocr.data.NoteOcrTextsDao
import com.originb.inkwisenote2.modules.ocr.data.OcrService.AnalyzeImageTask
import com.originb.inkwisenote2.modules.repositories.AtomicNotesDomain
import com.originb.inkwisenote2.modules.smartnotes.data.AtomicNoteEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class TextParsingWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val handwrittenNoteRepository: HandwrittenNoteRepository,
    private val atomicNotesDomain: AtomicNotesDomain,
    private val noteOcrTextDao: NoteOcrTextsDao
) : Worker(context, workerParams) {
    private val appConfig: AppConfig
    private val appSecrets: AppSecrets
    private val logger = Logger("TextParsingWorker")
    private var recognizer: DigitalInkRecognizer? = null

    data class ParsingInput(var bookId: Long = 0, var noteId: Long = 0)

    init {
        appConfig = getInstance().getAppConfig()
        appSecrets = appConfig.appSecrets

        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        try {
            // Initialize ML Kit recognizer with English model
            val languageTag = "en-US" // Default to English
            val modelIdentifier =
                DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)

            if (modelIdentifier == null) {
                logger.error("Model for language " + languageTag + " not found")
                return
            }

            val model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build()

            // First check if model is already downloaded
            val modelManager = RemoteModelManager.getInstance()


            // Use CompletableFuture to wait for model download/check to complete
            val modelReadyFuture = CompletableFuture<Boolean>()


            // Check if model is already downloaded
            modelManager.isModelDownloaded(model)
                .addOnSuccessListener(OnSuccessListener { isDownloaded: Boolean ->
                    if (isDownloaded) {
                        logger.debug("Model marked as downloaded, verifying...")
                        // Verify the model actually works by creating a test recognizer
                        verifyModelActuallyWorks(model, modelManager, modelReadyFuture)
                    } else {
                        logger.debug("Model not found, starting download")
                        // Model not downloaded, start download
                        modelManager.download(model, DownloadConditions.Builder().build())
                            .addOnSuccessListener(OnSuccessListener { unused: Void? ->
                                logger.debug("Model downloaded successfully, verifying...")
                                // Verify the model actually works after download
                                verifyModelActuallyWorks(model, modelManager, modelReadyFuture)
                            })
                            .addOnFailureListener(OnFailureListener { e: Exception? ->
                                logger.error("Error downloading model: " + e!!.message)
                                modelReadyFuture.complete(false)
                            })
                    }
                })
                .addOnFailureListener(OnFailureListener { e: Exception? ->
                    logger.error("Failed to check if model is downloaded: " + e!!.message)
                    modelReadyFuture.complete(false)
                })


            // Wait for the model to be ready (with timeout)
            val modelReady: Boolean = modelReadyFuture.get(30, TimeUnit.SECONDS)!!

            if (modelReady) {
                // Create recognizer only if model is ready
                recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build()
                )
                logger.debug("Recognizer initialized successfully")
            } else {
                logger.error("Model not ready, recognizer not initialized")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize ML Kit: " + e.message)
        }
    }

    /**
     * Verifies that the model actually works by creating a test recognizer and
     * performing a simple operation
     */
    private fun verifyModelActuallyWorks(
        model: DigitalInkRecognitionModel,
        modelManager: RemoteModelManager,
        resultFuture: CompletableFuture<Boolean>
    ) {
        try {
            // Create a test recognizer
            val testRecognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build()
            )


            // Create a simple ink with one stroke to test
            val inkBuilder = Ink.builder()
            val strokeBuilder = Ink.Stroke.builder()


            // Add a simple line (two points)
            strokeBuilder.addPoint(Ink.Point.create(0f, 0f, 0))
            strokeBuilder.addPoint(Ink.Point.create(10f, 10f, 10))
            inkBuilder.addStroke(strokeBuilder.build())


            // Try to recognize this simple pattern
            testRecognizer.recognize(inkBuilder.build())
                .addOnSuccessListener(OnSuccessListener { result: RecognitionResult? ->
                    // If we get here, the model is working correctly
                    logger.debug("Model verification successful")
                    resultFuture.complete(true)
                })
                .addOnFailureListener(OnFailureListener { e: Exception? ->
                    logger.error("Model verification failed: " + e!!.message)
                    // Model is corrupt or unavailable, try force re-downloading
                    forceRedownloadModel(model, modelManager, resultFuture)
                })
        } catch (e: Exception) {
            logger.error("Error during model verification: " + e.message)
            // Try force re-downloading the model
            forceRedownloadModel(model, modelManager, resultFuture)
        }
    }

    /**
     * Forces a re-download of the model by first deleting it and then downloading again
     */
    private fun forceRedownloadModel(
        model: DigitalInkRecognitionModel,
        modelManager: RemoteModelManager,
        resultFuture: CompletableFuture<Boolean>
    ) {
        logger.debug("Attempting to force re-download the model")


        // First delete the existing model if any
        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener(OnSuccessListener { aVoid: Void? ->
                logger.debug("Successfully deleted existing model")
                // Now download the model again
                modelManager.download(
                    model, DownloadConditions.Builder()
                        .requireWifi() // Require WiFi to ensure better download
                        .build()
                )
                    .addOnSuccessListener(OnSuccessListener { unused: Void? ->
                        logger.debug("Model re-downloaded successfully")
                        resultFuture.complete(true)
                    })
                    .addOnFailureListener(OnFailureListener { e: Exception? ->
                        logger.error("Failed to re-download model: " + e!!.message)
                        resultFuture.complete(false)
                    })
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                logger.error("Failed to delete existing model: " + e!!.message)
                resultFuture.complete(false)
            })
    }

    override fun doWork(): Result {
        val result = Try.to(
            Callable {
                val bookId = getInputData().getLong("book_id", -1)
                val noteId = getInputData().getLong("note_id", -1)
                ParsingInput(bookId, noteId)
            },
            logger
        ).get()
        Optional.ofNullable(result)
            .filter { input -> isNoteIdGreaterThan0(input) }
            .ifPresent { input -> parseTextForNotebook(input) }
        return Result.success()
    }

    fun parseTextForNotebook(input: ParsingInput) {
        val bookId = input.bookId
        val noteId = input.noteId
        logger.debug("Parsing text from a notebook for bookId: " + bookId + ", noteId: " + noteId)

        val atomicNote = atomicNotesDomain.getAtomicNote(noteId)

        val handwrittenNoteWithImage = handwrittenNoteRepository.getNoteImage(atomicNote, BitmapScale.FULL_SIZE)
        val handwrittenNoteEntity = handwrittenNoteWithImage.handwrittenNoteEntity
        if (handwrittenNoteEntity == null) {
            logger.error("Handwritten note entity not found for noteId: " + noteId)
            onFailure(bookId, noteId)
            return
        }

        val noteOcrTextDb = noteOcrTextDao.readTextFromDb(atomicNote.noteId)
        if (noteOcrTextDb.noteHash == handwrittenNoteEntity.bitmapHash) {
            logger.debug("Note already has the latest OCR text, skipping")
            return
        }

        // Try ML Kit first if Azure OCR is not explicitly required
        var noteOcrText: NoteOcrText? = null
        if (!isAzureOcrEnabled) {
            logger.debug("Attempting recognition with ML Kit")
            noteOcrText = parseTextForHandwrittenNote(atomicNote, handwrittenNoteEntity, noteOcrTextDb)
        }


        // Fall back to Azure OCR if ML Kit fails or if Azure is explicitly required
        if (noteOcrText == null) {
            logger.debug("ML Kit recognition failed or Azure OCR was requested, trying Azure OCR")
            noteOcrText = parseTextForHandwrittenNoteWithAzure(atomicNote, handwrittenNoteWithImage, noteOcrTextDb)
        }

        if (noteOcrText == null) {
            logger.error("Both ML Kit and Azure OCR failed to recognize text")
            onFailure(bookId, noteId)
            return
        }

        // Save the recognized text
        try {
            logger.debug("Updating OCR text for noteId: " + noteId)
            noteOcrTextDao.updateTextToDb(noteOcrText)

            logger.debug("Successfully saved OCR text for noteId: " + noteId)
            scheduleWorkForTextProcessingForBook(getApplicationContext(), bookId, noteId)
        } catch (e: Exception) {
            logger.error("Failed to save OCR text: " + e.message)
            onFailure(bookId, noteId)
        }
    }

    fun parseTextForHandwrittenNote(
        atomicNote: AtomicNoteEntity,
        handwrittenNoteEntity: HandwrittenNoteEntity,
        noteOcrTextDb: NoteOcrText?
    ): NoteOcrText? {
        // Check if note has changed since last OCR

        if (noteOcrTextDb != null && noteOcrTextDb.noteHash == handwrittenNoteEntity.bitmapHash) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity)
            return noteOcrTextDb
        }

        // Get strokes from the note
        val strokes: MutableList<Stroke> = handwrittenNoteRepository.getStrokes(atomicNote.noteId)

        if (strokes.isEmpty()) {
            logger.error("No strokes found for note", handwrittenNoteEntity)
            return null
        }

        // Use ML Kit to recognize text from strokes
        val recognizedText = recognizeInkText(strokes)

        if (recognizedText == null || handwrittenNoteEntity.bitmapHash == null) {
            logger.error("Failed to recognize text using ML Kit", handwrittenNoteEntity)
            return null
        }

        // Save recognized text
        val noteOcrText = NoteOcrText(
            atomicNote.noteId,
            handwrittenNoteEntity.bitmapHash ?: "",
            recognizedText
        )
        return noteOcrText
    }

    private fun recognizeInkText(appStrokes: MutableList<Stroke>): String? {
        // First check if recognizer is available
        if (recognizer == null) {
            logger.error("Recognizer not initialized, attempting to initialize it now")
            initializeRecognizer()


            // Check again after initialization attempt
            if (recognizer == null) {
                logger.error("Failed to initialize recognizer, falling back to Azure OCR if available")
                return null
            }
        }

        try {
            // Convert app strokes to ML Kit ink format
            val inkBuilder = Ink.builder()

            for (appStroke in appStrokes) {
                val strokeBuilder = Ink.Stroke.builder()

                for (appPoint in appStroke.points) {
                    strokeBuilder.addPoint(
                        Ink.Point.create(
                            appPoint.x,
                            appPoint.y,
                            appPoint.timestamp
                        )
                    )
                }

                inkBuilder.addStroke(strokeBuilder.build())
            }

            val ink = inkBuilder.build()

            // CompletableFuture to handle async recognition
            val future = CompletableFuture<String?>()

            // Recognize text with timeout
            recognizer!!.recognize(ink)
                .addOnSuccessListener(OnSuccessListener { result: RecognitionResult? ->
                    var text = ""
                    if (!result!!.candidates.isEmpty()) {
                        text = result.candidates[0].text
                        logger.debug("Recognition successful: $text")
                    } else {
                        logger.debug("Recognition returned no candidates")
                    }
                    future.complete(text)
                })
                .addOnFailureListener(OnFailureListener { e: Exception? ->
                    logger.error("Recognition failed: " + e!!.message)
                    future.completeExceptionally(e)
                })

            // Wait for result with timeout
            try {
                return future.get(15, TimeUnit.SECONDS)
            } catch (e: TimeoutException) {
                logger.error("Recognition timed out after 15 seconds")
                return null
            }
        } catch (e: Exception) {
            logger.error("Error during ink recognition: " + e.message)
            return null
        }
    }

    private fun onFailure(bookId: Long, noteId: Long): Result {
        scheduleWorkForFindingRelatedNotesForBook(applicationContext, bookId, noteId)
        return Result.failure()
    }

    private fun isNoteIdGreaterThan0(parsingInput: ParsingInput): Boolean {
        if (parsingInput.bookId == -1L || parsingInput.noteId == -1L) {
            logger.error(
                ("Got incorrect note id (-1) as input: bookId"
                        + parsingInput.bookId + " noteId: " + parsingInput.noteId)
            )
            return false
        }
        return true
    }

    fun parseTextForHandwrittenNoteWithAzure(
        atomicNote: AtomicNoteEntity,
        handwrittenNoteWithImage: HandwrittenNoteWithImage,
        noteOcrTextDb: NoteOcrText?
    ): NoteOcrText? {
        val handwrittenNoteEntity = handwrittenNoteWithImage.handwrittenNoteEntity ?: return null
        val bitmapOpt: Bitmap? = handwrittenNoteWithImage.noteImage
        if (bitmapOpt == null) {
            logger.error("Handwritten note doesn't have image", handwrittenNoteEntity)
            return null
        }

        if (noteOcrTextDb != null && noteOcrTextDb.noteHash == handwrittenNoteEntity.bitmapHash) {
            logger.debug("Note ocr has the latest text, skipping", handwrittenNoteEntity)
            return noteOcrTextDb
        }

        val azureResultOpt = applyAzureOcr(bitmapOpt)
        if (azureResultOpt == null) {
            logger.error("Failed to get text using OCR", handwrittenNoteEntity)
            return null
        }
        val noteOcrText = NoteOcrText(
            atomicNote.noteId,
            handwrittenNoteEntity.bitmapHash ?: "",
            azureResultOpt.readResult?.content ?: ""
        )
        return noteOcrText
    }

    private fun applyAzureOcr(bitmap: Bitmap): AzureOcrResult? {
        logger.debug("got bitmap")
        val azureOcrResult = Try.to(
            Callable {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val imageStream: InputStream = ByteArrayInputStream(byteArray)
                val imageTask = AnalyzeImageTask(appSecrets)
                val result = imageTask.runOcr(imageStream)
                logger.debug("Ocr result: $result")
                result
            },
            Logger("TextParsingJob")
        ).logIfError("Failed to convert handwriting to text").get()
        return azureOcrResult
    }
}
