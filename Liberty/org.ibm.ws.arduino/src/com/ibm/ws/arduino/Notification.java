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

/**
 * Notifications enable server-side application code to be notified about events on an Arduino
 */
public interface Notification {

    /**
     * Called when the Arduino signals an event has occured
     * 
     * @param arduinoName  the name of the Arduino on which the event occured
     * @param value  the value the event
     */
    void event(String arduinoName, int value);

}
