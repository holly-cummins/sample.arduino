package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.sample.arduino.Arduino;
import com.ibm.ws.sample.arduino.ArduinoService;

public class EEPROMTestcase {

    @Test
    public void testReadWriteInvoke() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);

        arduino.eepromWrite(0, new byte[]{23});
        arduino.eepromWrite(10, new byte[]{42});
        arduino.eepromWrite(20, new byte[]{71});
        assertEquals(23, arduino.eepromRead(0,1)[0]);
        assertEquals(42, arduino.eepromRead(10,1)[0]);
        assertEquals(71, arduino.eepromRead(20,1)[0]);
    }

    @Test
    public void testReadWriteBytes() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);

        arduino.eepromWrite(11, new byte[]{0});
        arduino.eepromWrite(15, new byte[]{0});
        arduino.eepromWrite(12, new byte[]{23,24,25});
        byte[] bs = arduino.eepromRead(11,5);
        assertEquals(5, bs.length);
        assertEquals(0, bs[0]);
        assertEquals(23, bs[1]);
        assertEquals(24, bs[2]);
        assertEquals(25, bs[3]);
        assertEquals(0, bs[4]);
    }
    
    @Test
    public void testReadWriteStrings() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino.eepromWrite(0, "Ariana");
        assertEquals("Ariana", arduino.eepromReadString(0));
        arduino.eepromWrite(0, "A");
        assertEquals("A", arduino.eepromReadString(0));
    }

    @Test
    public void testNullString() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino.eepromWrite(10, "");
        assertEquals("", arduino.sramReadString(10));
    }
}
