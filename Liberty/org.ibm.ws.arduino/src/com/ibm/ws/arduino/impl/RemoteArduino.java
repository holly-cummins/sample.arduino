/**
 * (C) Copyright IBM Corporation 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.ws.arduino.impl;

import java.io.IOException;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.Callback;
import com.ibm.ws.arduino.Notification;

public class RemoteArduino implements Arduino {

    private ArduinoAsyncImpl arduino;
    private String name;

    public RemoteArduino(ArduinoAsyncImpl arduinoAsyncImpl, String name) {
        this.arduino = arduinoAsyncImpl;
        this.name = name;

    }

    @Override
    public void pinMode(int pin, Mode mode) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.pinMode(pin, mode);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public Level digitalRead(int pin) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.digitalRead(pin);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void digitalWrite(int pin, Level value) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.digitalWrite(pin, value);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int analogRead(int pin) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.analogRead(pin);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void analogWrite(int pin, int value) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.analogWrite(pin, value);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int eepromRead(int address) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.eepromRead(address);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void eepromWrite(int address, int value) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.eepromWrite(address, value);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public byte[] sramRead(int address, int length) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.sramRead(address, length);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public String sramReadString(int address) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.sramReadString(address);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void sramWrite(int address, byte[] value) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.sramWrite(address, value);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void sramWrite(int address, String value) throws IOException {
        try {
            arduino.setRemote(name);
            arduino.sramWrite(address, value);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int invoke(String function) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.invoke(function);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int digitalCallback(int pin, Level state, Callback cb) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.digitalCallback(pin, state, cb);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int analogCallback(int pin, Comparitor comparitor, int value, Callback cb) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.analogCallback(pin, comparitor, value, cb);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int functionCallback(String function, Comparitor comparitor, int value, Callback cb) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.functionCallback(function, comparitor, value, cb);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public Arduino getRemote(String name) {
        // TODO
        return null;
    }

    @Override
    public void clearCallbacks() throws IOException {
        try {
            arduino.setRemote(name);
            arduino.clearCallbacks();
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int invoke(String function, int x) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.invoke(function, x);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public int invoke(String function, int x, int y) throws IOException {
        try {
            arduino.setRemote(name);
            return arduino.invoke(function, x, y);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void addNotification(String name, Notification n) {
        try {
            arduino.setRemote(name);
            arduino.addNotification(name, n);
        } finally {
            arduino.removeRemote();
        }
    }

    @Override
    public void removeNotification(Notification n) {
        try {
            arduino.setRemote(name);
            arduino.removeNotification(n);
        } finally {
            arduino.removeRemote();
        }
    }

}
