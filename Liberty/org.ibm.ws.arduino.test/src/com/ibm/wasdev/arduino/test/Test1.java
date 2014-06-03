package com.ibm.wasdev.arduino.test;

import static com.ibm.wasdev.arduino.Arduino.Level.HIGH;
import static com.ibm.wasdev.arduino.Arduino.Mode.INPUT;
import static com.ibm.wasdev.arduino.Arduino.Mode.OUTPUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.ibm.wasdev.arduino.Arduino;
import com.ibm.wasdev.arduino.ArduinoService;
import com.ibm.wasdev.arduino.Callback;

public class Test1 {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("192.168.1.94", 2000);
        s.setSoTimeout(30000);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
        s.close();
    }

    public void foo() throws IOException {

        Arduino arduino = ArduinoService.get();

        arduino.digitalCallback(8, HIGH, new Callback() {
            public void triggered(int value) {
                System.out.println("pin 8 triggered!");
            }

            public void reset(int value) {
                System.out.println("pin 8 reset");
            }
        });

    }

    public void test2() throws IOException {
        Arduino arduino = ArduinoService.get();
        arduino.pinMode(1, INPUT);
        arduino.pinMode(1, OUTPUT);

    }
}
