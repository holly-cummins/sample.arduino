package com.ibm.ws.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.ArduinoService;
import com.ibm.ws.arduino.Notification;
import com.ibm.ws.arduino.impl.ArduinoAsyncImpl;

public class NotificationTestcase {

    int notificationCalled = 0;

    static Arduino arduino;

    @BeforeClass
    public static void setup() throws IOException {
        arduino = ArduinoService.get(TestPort.PORT);
    }

    @AfterClass
    public static void teardown() throws IOException {
        ((ArduinoAsyncImpl) arduino).close();
    }

    @Test
    public void testCallback() throws IOException, InterruptedException {

        arduino.invoke("foo");

        arduino.addNotification("testN1", new Notification() {
            @Override
            public void event(String arduinoName, int value) {
                System.out.println("event " + arduinoName + " " + value);
                assertEquals("TestNode", arduinoName);
                notificationCalled++;
            }
        });
        
        arduino.invoke("start");
        Thread.sleep(1100);
        int x = arduino.invoke("stop");
        assertTrue(x > 4);
        assertEquals(x, notificationCalled);
    }
}
