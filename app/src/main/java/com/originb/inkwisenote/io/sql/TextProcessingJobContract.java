package com.originb.inkwisenote.io.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingJobStatus;
import com.originb.inkwisenote.modules.backgroundjobs.data.TextProcessingStage;

public class TextProcessingJobContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TextProcessingJobContract() {
    }

    /* Inner class that defines the table contents */
    protected static class TextProcessingJobEntry implements BaseColumns {
        public static final String TABLE_NAME = "text_processing_jobs";
        public static final String COLUMN_NAME_NOTE_ID = "note_id";
        public static final String COLUMN_NAME_STAGE = "stage";
    }

    public static class TextProcessingDbQueries extends InkwiseNoteDbHelper {

        public TextProcessingDbQueries(Context context) {
            super(context, DATABASE_NAME, DATABASE_VERSION);
        }

        protected String getSqlCreateQuery() {
            return "CREATE TABLE " + TextProcessingJobEntry.TABLE_NAME + "(" +
                    TextProcessingJobEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID + " INTEGER NOT NULL," +
                    TextProcessingJobEntry.COLUMN_NAME_STAGE + " TEXT NOT NULL" +
                    ")";
        }

        protected String getSqlDropQuery() {
            return "DROP TABLE IF EXISTS " + TextProcessingJobEntry.TABLE_NAME;
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(getSqlDropQuery());
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }


        public TextProcessingJobStatus readFirstNoteJobStatus() {
            SQLiteDatabase db = getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    TextProcessingJobEntry._ID,
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID,
                    TextProcessingJobEntry.COLUMN_NAME_STAGE
            };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    TextProcessingJobEntry.COLUMN_NAME_NOTE_ID + " DESC";

            Cursor cursor = db.query(
                    TextProcessingJobEntry.TABLE_NAME,   // The table to query
                    projection,                          // The array of columns to return (pass null to get all)
                    null,                                // No where clause
                    null,                                // No where clause arguments
                    null,                                // Don't group the rows
                    null,                                // Don't filter by row groups
                    sortOrder                            // The sort order with LIMIT
            );
            
            TextProcessingJobStatus textProcessingJobStatus = null;
            while (cursor.moveToNext()) {
                textProcessingJobStatus = new TextProcessingJobStatus(cursor.getLong(
                        cursor.getColumnIndexOrThrow(TextProcessingJobEntry.COLUMN_NAME_NOTE_ID)),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(TextProcessingJobEntry.COLUMN_NAME_STAGE)));
            }
            cursor.close();
            Log.d("TextProcessingJob", "Loaded text jobs");
            return textProcessingJobStatus;
        }

        public void insertJob(Long noteId) {
            SQLiteDatabase db = getWritableDatabase();

            try {
                // Start a transaction
                db.beginTransaction();

                ContentValues values = new ContentValues();
                values.put(TextProcessingJobEntry.COLUMN_NAME_NOTE_ID, noteId);
                values.put(TextProcessingJobEntry.COLUMN_NAME_STAGE, TextProcessingStage.Tokenization);

                // Insert the new row
                long newRowId = db.insert(TextProcessingJobEntry.TABLE_NAME, null, values);

                if (newRowId == -1) {
                    Log.e("TextProcessingJob", "Failed to insert job for noteId: " + noteId);
                } else {
                    Log.d("TextProcessingJob", "Successfully inserted job for noteId: " + noteId);
                }

                // Mark the transaction as successful
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.e("TextProcessingJob", "Error inserting job", ex);
            } finally {
                // End the transaction
                db.endTransaction();
            }
        }

        public void deleteJob(Long noteId) {
            SQLiteDatabase db = getWritableDatabase();

            try {
                // Start a transaction
                db.beginTransaction();

                String selection = TextProcessingJobEntry.COLUMN_NAME_NOTE_ID + " = ?";
                // Specify the arguments in placeholder order
                String[] selectionArgs = {String.valueOf(noteId)};

                // Insert the new row
                long newRowId = db.delete(TextProcessingJobEntry.TABLE_NAME, selection, selectionArgs);

                if (newRowId == -1) {
                    Log.e("TextProcessingJob", "Failed to delete job for noteId: " + noteId);
                } else {
                    Log.d("TextProcessingJob", "Successfully deleted job for noteId: " + noteId);
                }

                // Mark the transaction as successful
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.e("TextProcessingJob", "Error deleting job", ex);
            } finally {
                // End the transaction
                db.endTransaction();
            }
        }
    }
}

