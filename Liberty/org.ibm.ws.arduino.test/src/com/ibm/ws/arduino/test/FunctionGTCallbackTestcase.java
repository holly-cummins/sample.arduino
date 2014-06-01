package com.ibm.ws.arduino.test;

import static com.ibm.ws.arduino.Arduino.Comparitor.GT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;
import com.ibm.ws.arduino.Callback;
import com.ibm.ws.arduino.impl.ArduinoAsyncImpl;

public class FunctionGTCallbackTestcase {

    boolean triggeredCalled = false;
    boolean resetCalled = false;
    Object mutex = new Object();
    static Arduino arduino;

    @BeforeClass
    public static void setup() throws IOException {
        arduino = ArduinoService.get(TestPort.PORT);
        arduino.clearCallbacks();
    }

    @AfterClass
    public static void teardown() throws IOException {
        ((ArduinoAsyncImpl) arduino).close();
    }

    @Test
    public void testLessThanCallback() throws IOException, InterruptedException {

        arduino.sramWrite(0, new byte[]{10});

        Callback cb = new Callback() {
            public void triggered(int value) {
                triggeredCalled = true;
                synchronized (mutex) {
                    mutex.notify();
                }
            }

            public void reset(int value) {
                resetCalled = true;
                synchronized (mutex) {
                    mutex.notify();
                }
            }
        };
        arduino.functionCallback("foo", GT, 55, cb);

        Thread.sleep(1000);
        for (int i = 0; i < 1; i++) {
            testIt();
        }

    }

    private void testIt() throws IOException, InterruptedException {
        triggeredCalled = false;
        resetCalled = false;

        assertFalse(triggeredCalled);
        assertFalse(resetCalled);

        arduino.sramWrite(0, new byte[]{126});
        synchronized (mutex) {
            if (!!!triggeredCalled)
                mutex.wait(1000);
        }

        assertTrue(triggeredCalled);
        assertFalse(resetCalled);

        triggeredCalled = false;

        arduino.sramWrite(0, new byte[]{11});
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(1000);
        }
        assertTrue(resetCalled);
        assertFalse(triggeredCalled);
    }
}
