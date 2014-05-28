package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.ibm.ws.arduino.ArduinoService;

public class PortNamesTestcase {

    @Test
    public void testVersion() throws IOException {
        List<String> ports = ArduinoService.getAvailablePortNames();
        System.out.println(ports);
        assertNotNull(ports);
        assertTrue(ports.size() > 0);
    }

}
