package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;
import com.ibm.ws.arduino.impl.ArduinoAsyncImpl;

public class VersionTestcase {

    @Test
    public void testVersion() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        assertEquals("0.0.15", ((ArduinoAsyncImpl)arduino).getArduinoLibVersion());
        assertEquals("TestNode", ((ArduinoAsyncImpl)arduino).getArduinoName());
    }

}
