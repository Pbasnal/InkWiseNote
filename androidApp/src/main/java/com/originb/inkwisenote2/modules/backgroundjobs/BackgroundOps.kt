package com.originb.inkwisenote2.modules.backgroundjobs

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

class BackgroundOps private constructor() {
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

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

        @JvmStatic
        fun execute(runnable: Runnable?) {
            instance!!.executor.execute(runnable)
        }

        @JvmStatic
        fun execute(runnable: Runnable?, continueOnMainThread: Runnable) {
            try {
                instance!!.executor.execute(runnable)
                instance!!.mainThreadHandler.post { continueOnMainThread.run() }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun <T> execute(callable: Callable<T?>, resultOnMainThread: Consumer<T?>) {
            // 1. Capture the stack trace of the CALLER (the UI thread) right now
            val clientStack = Exception("Task submitted from here")

            instance!!.executor.execute(Runnable {
                try {
                    val result = callable.call()
                    instance!!.mainThreadHandler.post(Runnable { resultOnMainThread.accept(result) })
                } catch (e: Exception) {
                    // 2. Stitch the current background exception with the caller's stack trace
                    e.initCause(clientStack)

                    // 3. Now when you log this, you see exactly where it was enqueued
                    handleError(e)
                }
            })
        }

        fun <T> executeOpt(callable: Callable<T?>, resultOnMainThread: Consumer<T?>) {
            instance!!.executor.execute {
                try {
                    val result = callable.call()
                    if (result != null) {
                        instance!!.mainThreadHandler.post { resultOnMainThread.accept(result) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun handleError(e: Exception) {
            // Using a logger helps preserve the full cause chain
            e.printStackTrace()
            Log.e("BackgroundOps", "Background task failed", e)

            // Optional: Post back to UI to show an error dialog/toast
            instance!!.mainThreadHandler.post(Runnable {})
        }
    }
}
