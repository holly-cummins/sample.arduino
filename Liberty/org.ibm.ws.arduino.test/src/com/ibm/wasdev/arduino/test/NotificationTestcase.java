package com.ibm.wasdev.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;
import com.ibm.wasdev.arduino.NotificationListener;
import com.ibm.wasdev.arduino.impl.ArduinoAsyncImpl;

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

        arduino.addNotificationListener("testN1", new NotificationListener() {
            @Override
            public void notify(String arduinoName, int value) {
                System.out.println("notify " + arduinoName + " " + value);
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
