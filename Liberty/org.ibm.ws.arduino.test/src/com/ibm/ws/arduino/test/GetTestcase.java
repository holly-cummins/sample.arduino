package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.sample.arduino.Arduino;
import com.ibm.ws.sample.arduino.ArduinoService;

public class GetTestcase {

    @Test
    public void testGet() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino = ArduinoService.get(TestPort.PORT);
        arduino = ArduinoService.get(TestPort.PORT);
        arduino.toString();
    }

    @Test
    public void testGetError() throws IOException {
        try {
            ArduinoService.get("SomethingThatDoesntExist");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("SomethingThatDoesntExist"));
        }
    }

    @Test
    public void testNameError() throws IOException {
        try {
            ArduinoService.get(TestPort.PORT, "foo");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("foo"));
        }
    }
}
