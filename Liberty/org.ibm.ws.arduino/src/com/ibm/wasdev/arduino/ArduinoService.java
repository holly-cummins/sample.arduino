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

import java.io.IOException;
import java.util.List;

import com.ibm.wasdev.arduino.impl.ServiceManager;

public class ArduinoService {

    public static Arduino get() throws IOException {
        return get(ServiceManager.DEFAULT_ID);
    }

    public static Arduino get(String portName) throws IOException {
        return ServiceManager.get(portName, null);
    }
    
    public static Arduino get(String portName, String arduinoName) throws IOException {
        return ServiceManager.get(portName, arduinoName);
    }
    
    public static List<String> getAvailablePortNames() {
        return ServiceManager.getAvailablePortNames();
    }
    
}
