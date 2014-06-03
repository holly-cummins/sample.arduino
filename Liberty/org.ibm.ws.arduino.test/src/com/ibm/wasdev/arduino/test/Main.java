package com.ibm.wasdev.arduino.test;

import static com.ibm.wasdev.arduino.Arduino.Comparitor.CHGBY;

import java.io.IOException;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;
import com.ibm.wasdev.arduino.Callback;
import com.ibm.wasdev.arduino.NotificationListener;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
//        arduino.clearCallbacks();
//        
//        arduino.analogCallback(3, CHGBY, 5, new Callback() {
//            @Override
//            public void triggered(int value) {
//                System.out.println("triggered: " + value);
//            }
//
//            @Override
//            public void reset(int value) {
//                System.out.println("reset: " + value);
//            }});        
//   
        arduino.addNotificationListener("test", new NotificationListener() {
            @Override
            public void notify(String arduinoName, int value) {
                System.out.println("Event 'test' from: " + arduinoName + " value: " + value);
            }});
        
        synchronized (Main.class) {
            Main.class.wait();
        }
    }

}
