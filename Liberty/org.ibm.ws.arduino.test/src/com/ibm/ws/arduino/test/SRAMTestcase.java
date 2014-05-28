package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;

public class SRAMTestcase {

    @Test
    public void testInvoke() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);

        arduino.sramWrite(0, 23);
        arduino.sramWrite(1, 42);
        arduino.sramWrite(2, 71);
        assertEquals(23, arduino.sramRead(0));
        assertEquals(42, arduino.sramRead(1));
        assertEquals(71, arduino.sramRead(2));
    }

    @Test
    public void testString() throws IOException {
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
    public void test2Strings() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        String name = "Ant1";
        String pin = "4444";
        String speed = "2";
        System.out.println("name: " + name);
        System.out.println("pin: " + pin);
        System.out.println("speed: " + speed);

        arduino.sramWrite(0, name);
        arduino.sramWrite(21, pin);
        arduino.sramWrite(26, speed);

        assertEquals("Ant1", arduino.sramReadString(0));
        assertEquals("4444", arduino.sramReadString(21));
        assertEquals("2", arduino.sramReadString(26));
    }
}
