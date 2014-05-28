package com.ibm.ws.arduino.test;

import static com.ibm.ws.arduino.Arduino.Comparitor.EQ;
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

public class FunctionEQCallbackTestcase {

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
    public void testCallback() throws IOException, InterruptedException {

        arduino.sramWrite(0, 0);

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
        arduino.functionCallback("foo", EQ, 10, cb);

        for (int i = 0; i < 5; i++) {
            testIt();
            triggeredCalled = false;
            resetCalled = false;
        }

    }

    private void testIt() throws IOException, InterruptedException {
        assertFalse(triggeredCalled);
        assertFalse(resetCalled);

        arduino.sramWrite(0, 10);
        synchronized (mutex) {
            if (!!!triggeredCalled)
                mutex.wait(2000);
        }

        assertTrue(triggeredCalled);
        assertFalse(resetCalled);

        triggeredCalled = false;

        arduino.sramWrite(0, 0);
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(1000);
        }
        assertTrue(resetCalled);
    }
}
