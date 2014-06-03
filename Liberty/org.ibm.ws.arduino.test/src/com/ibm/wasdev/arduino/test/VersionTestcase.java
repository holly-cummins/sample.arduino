package com.ibm.wasdev.arduino.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;
import com.ibm.wasdev.arduino.impl.ArduinoAsyncImpl;

public class VersionTestcase {

    @Test
    public void testVersion() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        assertEquals("0.1.0", ((ArduinoAsyncImpl)arduino).getArduinoLibVersion());
        assertEquals("TestNode", ((ArduinoAsyncImpl)arduino).getArduinoName());
    }

}
