package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;

public class SRAMTestcase {


    @Test
    public void testSramReadWriteBytes() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);

        byte[] bs = {42};
        arduino.sramWrite(0, bs);
        byte[] bs2 = arduino.sramRead(0, 1);

        assertTrue(bs.length == bs2.length);
        assertEquals(bs[0], bs2[0]);

        byte[] bs3 = {0,1,2,3,4,5,(byte)255}; 
        arduino.sramWrite(5, bs3);
        bs2 = arduino.sramRead(5, bs3.length);
        assertTrue(bs3.length == bs2.length);
        for (int i=0; i<bs3.length; i++) {
            assertEquals(bs3[i], bs2[i]);
        }
        assertEquals(255, bs2[6] & 0xFF);
    }

    @Test
    public void testSramReadWriteString() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino.sramWrite(0, "Ariana");
        assertEquals("Ariana", arduino.sramReadString(0));
        arduino.sramWrite(0, "A");
        assertEquals("A", arduino.sramReadString(0));
    }

    @Test
    public void testNullString() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino.sramWrite(10, "");
        assertEquals("", arduino.sramReadString(10));
    }

    @Test
    public void testReadWriteZeroBytes() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        byte[] bytes = {};
        arduino.sramWrite(10, bytes);
        byte[] bytes2 = arduino.sramRead(10, 0);
        assertEquals(bytes.length, bytes2.length);
        assertEquals(bytes.length, bytes2.length);
    }
    
    @Test
    public void test2Strings() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        String name = "Ant1";
        String pin = "98765";
        String speed = "2";
        arduino.sramWrite(0, name);
        arduino.sramWrite(21, pin);
        arduino.sramWrite(28, speed);

        assertEquals("Ant1", arduino.sramReadString(0));
        assertEquals("98765", arduino.sramReadString(21));
        assertEquals("2", arduino.sramReadString(28));
    }
}
