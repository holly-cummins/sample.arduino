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
package com.ibm.wasdev.arduino;

/**
 * NotificationListener enables server-side application code to receive notify events from an Arduino
 */
public interface NotificationListener {

    /**
     * Called when the Arduino signals a notify has occured
     * 
     * @param arduinoName  the name of the Arduino which did the notify
     * @param value  the notify value
     */
    void notify(String arduinoName, int value);

}
