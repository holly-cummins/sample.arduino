package com.ibm.wasdev.arduino.test;

import static com.ibm.wasdev.arduino.Arduino.Comparitor.LT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;
import com.ibm.wasdev.arduino.Callback;
import com.ibm.wasdev.arduino.impl.ArduinoAsyncImpl;

public class CallbackFunctionErrorTestcase {

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
    public void testCallbackError() throws IOException, InterruptedException {

        Callback cb = new Callback() {
            public void triggered(int value) {
            }

            public void reset(int value) {
            }
        };

        try {
            arduino.functionCallback("qaz", LT, 10, cb);
            fail();
        } catch (IOException e) {
            assertEquals("Callback function cannot have arguments", e.getMessage());
        }
    }
}
