package com.ibm.ws.arduino.test;

import static com.ibm.ws.sample.arduino.Arduino.Level.HIGH;
import static com.ibm.ws.sample.arduino.Arduino.Level.LOW;
import static com.ibm.ws.sample.arduino.Arduino.Mode.OUTPUT;
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

public class DigitalCallbackTestcase {

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

        int pin = 4;
        arduino.pinMode(pin, OUTPUT);
        arduino.digitalWrite(pin, LOW);

        Callback cb = new Callback() {
            public void triggered(int value) {
                triggeredCalled = true;
                System.out.println("triggered: " + value);
                synchronized (mutex) {
                    mutex.notify();
                }
            }

            public void reset(int value) {
                resetCalled = true;
                System.out.println("reset: " + value);
                synchronized (mutex) {
                    mutex.notify();
                }
            }
        };
        arduino.digitalCallback(pin, HIGH, cb);

        for (int i = 0; i < 1; i++) {
            testIt(pin);
            triggeredCalled = false;
            resetCalled = false;
        }

    }

    private void testIt(int pin) throws IOException, InterruptedException {
        assertFalse(triggeredCalled);
        assertFalse(resetCalled);

        arduino.digitalWrite(pin, HIGH);
        synchronized (mutex) {
            if (!!!triggeredCalled)
                mutex.wait(1000);
        }

        assertTrue(triggeredCalled);
        assertFalse(resetCalled);

        triggeredCalled = false;

        arduino.digitalWrite(pin, LOW);
        synchronized (mutex) {
            if (!!!resetCalled)
                mutex.wait(1000);
        }
        assertTrue(resetCalled);
    }
}
