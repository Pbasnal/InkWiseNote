package com.originb.inkwisenote2.modules.backgroundjobs

import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

class BackgroundOps private constructor() {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private val executor: ExecutorService = Executors.newFixedThreadPool(10)

    companion object {
        var instance: BackgroundOps? = null
            get() {
                if (field == null) {
                    field = BackgroundOps()
                }
                return field
            }
            private set

        fun execute(runnable: Runnable?) {
            instance!!.executor.execute(runnable)
        }

        fun execute(runnable: Runnable?, continueOnMainThread: Runnable) {
            try {
                instance!!.executor.execute(runnable)
                instance!!.mainThreadHandler.post { continueOnMainThread.run() }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun <T> execute(callable: Callable<T>?, resultOnMainThread: Consumer<T>) {
            try {
                val callFuture = instance!!.executor.submit(callable)
                val result = callFuture.get()

                instance!!.mainThreadHandler.post { resultOnMainThread.accept(result) }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun <T> executeOpt(callable: Callable<Optional<T>?>?, resultOnMainThread: Consumer<T>) {
            try {
                val callFuture = instance!!.executor.submit(callable)
                val result = callFuture.get()

                if (result!!.isPresent) {
                    instance!!.mainThreadHandler.post {
                        resultOnMainThread.accept(
                            result.get()
                        )
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}
