package com.originb.inkwisenote.modules;

import com.originb.inkwisenote.modules.messaging.BackgroundOps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BackgroundOpsTest {

    @Test
    public void registerListener() {
        final int number = 0;
        BackgroundOps.execute(() -> {
            int newNumber = number + 1;
            assertTrue(newNumber == 2);
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
