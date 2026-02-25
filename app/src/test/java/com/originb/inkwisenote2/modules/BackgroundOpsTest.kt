package com.originb.inkwisenote2.modules

import com.originb.inkwisenote2.modules.backgroundjobs.BackgroundOps.Companion.execute
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackgroundOpsTest {
    @Test
    fun registerListener() {
        val number = 0
        execute(Runnable {
            val newNumber = number + 1
            Assert.assertTrue(newNumber == 2)
        })

        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}
