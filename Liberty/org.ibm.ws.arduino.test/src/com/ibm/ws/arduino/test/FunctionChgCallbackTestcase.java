package com.ibm.ws.arduino.test;

import static com.ibm.ws.sample.arduino.Arduino.Comparitor.CHGBY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.ws.sample.arduino.Arduino;
import com.ibm.ws.sample.arduino.ArduinoService;
import com.ibm.ws.sample.arduino.Callback;
import com.ibm.ws.sample.arduino.impl.ArduinoAsyncImpl;

public class FunctionChgCallbackTestcase {

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
    public void testChangeCallback() throws IOException, InterruptedException {

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
        arduino.functionCallback("foo", CHGBY, 10, cb);

        for (int i = 0; i < 5; i++) {
            triggeredCalled = false;
            resetCalled = false;
            testIt();
        }

    }

    private void testIt() throws IOException, InterruptedException {

        // reset to last fired at 100
        arduino.sramWrite(0, new byte[]{0});
        arduino.sramWrite(0, new byte[]{100});
        Thread.sleep(100);
        triggeredCalled = false;
        resetCalled = false;

        // small update, no callback
        arduino.sramWrite(0, new byte[]{99});
        Thread.sleep(100);
        assertFalse(triggeredCalled);
        assertFalse(resetCalled);
        arduino.sramWrite(0, new byte[]{105});
        Thread.sleep(100);
        assertFalse(triggeredCalled);
        assertFalse(resetCalled);

        // low change, reset called
        arduino.sramWrite(0, new byte[]{89});
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(500);
        }
        assertTrue(resetCalled);
        assertFalse(triggeredCalled);
        triggeredCalled = false;
        resetCalled = false;

        // another low change, reset called again
        arduino.sramWrite(0, new byte[]{69});
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(500);
        }
        assertTrue(resetCalled);
        assertFalse(triggeredCalled);
        triggeredCalled = false;
        resetCalled = false;

        // increase change, triggered called
        arduino.sramWrite(0, new byte[]{90});
        synchronized (mutex) {
            if (!!!triggeredCalled)
                mutex.wait(500);
        }
        assertTrue(triggeredCalled);
        assertFalse(resetCalled);
        triggeredCalled = false;
        resetCalled = false;

        // another low change, reset called
        arduino.sramWrite(0, new byte[]{79});
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(500);
        }
        assertTrue(resetCalled);
        assertFalse(triggeredCalled);
        triggeredCalled = false;
        resetCalled = false;

        // increase change, triggered called
        arduino.sramWrite(0, new byte[]{90});
        synchronized (mutex) {
            if (!!!triggeredCalled)
                mutex.wait(500);
        }
        assertTrue(triggeredCalled);
        assertFalse(resetCalled);
        triggeredCalled = false;
        resetCalled = false;

    }
}
