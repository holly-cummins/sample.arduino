package com.ibm.ws.arduino.test;

import static com.ibm.ws.arduino.Arduino.Level.HIGH;
import static com.ibm.ws.arduino.Arduino.Level.LOW;
import static com.ibm.ws.arduino.Arduino.Mode.OUTPUT;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;

@Ignore
public class RemoteInvokerTestcase {

    @Test
    public void testInvoke() throws IOException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        // assertEquals(42, arduino.invoke("foo"));
        arduino = arduino.getRemote("Node1");
        assertEquals(77, arduino.invoke("foo"));
    }

    @Test
    public void testInvoke2() throws IOException, InterruptedException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        arduino = arduino.getRemote("Node1");
        arduino.pinMode(2, OUTPUT);
        for (int i = 0; i < 10; i++) {
            arduino.digitalWrite(2, HIGH);
            Thread.sleep(1000);
            arduino.digitalWrite(2, LOW);
            Thread.sleep(1000);
        }

        // assertEquals(77, arduino.invoke("foo"));
    }

}
