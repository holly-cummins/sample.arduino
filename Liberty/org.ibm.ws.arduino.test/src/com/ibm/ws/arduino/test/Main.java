package com.ibm.ws.arduino.test;

import java.io.IOException;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.Arduino.Level;
import com.ibm.ws.arduino.Arduino.Mode;
import com.ibm.ws.arduino.ArduinoService;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Arduino arduino = ArduinoService.get(TestPort.PORT);
        
        boolean b = false;
        arduino.pinMode(13, Mode.OUTPUT);
        for (int i=0; i<100; i++) {
//            if (Level.HIGH == arduino.digitalRead(13)) {
              if (b) {
                arduino.digitalWrite(13, Level.LOW);            
             } else {
                 arduino.digitalWrite(13, Level.HIGH);            
             }
              b = !b;
            Thread.sleep(1000);
        }
        
//        synchronized (Main.class) {
//            Main.class.wait();
//        }
    }

}
