package com.ibm.wasdev.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;

public class InvokerTestcase {

    @Test
    public void testInvoke() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        assertEquals(42, arduino.invoke("bar"));
    }

    @Test
    public void testInvokeFoo() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino.sramWrite(0, new byte[]{123});
        assertEquals(123, arduino.invoke("foo"));
        arduino.sramWrite(0, new byte[]{97});
        assertEquals(97, arduino.invoke("foo"));
    }
    @Test
    public void testInvokeWithArg() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        assertEquals(20, arduino.invoke("qaz", 5));
    }

    @Test
    public void testInvokeWithTwoArgs() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        assertEquals(12, arduino.invoke("times", 3, 4));
    }

    @Test
    public void testInvokeUnknown() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        try {
            arduino.invoke("BLABLABAL");
            fail("expecting exception");
        } catch (IOException e) {
            assertEquals("Function not found", e.getMessage());
        }
    }

    @Test
    public void testNumArgsErro() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        try {
            arduino.invoke("foo", 666);
            fail("expecting exception");
        } catch (IOException e) {
            assertEquals("Wrong number of function arguments", e.getMessage());
        }
    }
}
