package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;

public class EEPROMTestcase {

    @Test
    public void testInvoke() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);

        arduino.eepromWrite(0, 23);
        arduino.eepromWrite(10, 42);
        arduino.eepromWrite(20, 71);
        assertEquals(23, arduino.eepromRead(0));
        assertEquals(42, arduino.eepromRead(10));
        assertEquals(71, arduino.eepromRead(20));
    }

}
