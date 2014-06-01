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
package com.ibm.ws.arduino;

import java.io.IOException;

public interface Arduino {

    void pinMode(int pin, Mode mode) throws IOException;

    Level digitalRead(int pin) throws IOException;

    void digitalWrite(int pin, Level value) throws IOException;

    int analogRead(int pin) throws IOException;

    void analogWrite(int pin, int value) throws IOException;

    byte[] eepromRead(int address, int length) throws IOException;
    String eepromReadString(int address) throws IOException;
    void eepromWrite(int address, byte[] value) throws IOException;
    void eepromWrite(int address, String s) throws IOException;

    byte[] sramRead(int address, int length) throws IOException;
    String sramReadString(int address) throws IOException;
    void sramWrite(int address, byte[] value) throws IOException;
    void sramWrite(int address, String s) throws IOException;

    int invoke(String function) throws IOException;

    int invoke(String function, int x) throws IOException;

    int invoke(String function, int x, int y) throws IOException;

    int digitalCallback(int pin, Level state, Callback cb) throws IOException;

    int analogCallback(int pin, Comparitor comparitor, int value, Callback cb) throws IOException;

    int functionCallback(String function, Comparitor comparitor, int value, Callback cb) throws IOException;

    void clearCallbacks() throws IOException;

    void addNotification(String name, Notification n);
    void removeNotification(Notification n);

    Arduino getRemote(String name);
    
    /**
     * 
     */
    public enum Mode {
        INPUT(0), OUTPUT(1), INPUT_PULLUP(2);
        private int value;

        private Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Level {
        LOW(0), HIGH(1);
        private int value;

        private Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Comparitor {
        LT(-1), GT(1), EQ(0), CHGBY(2);
        private int value;

        private Comparitor(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
