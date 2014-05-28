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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import purejavacomm.CommPortIdentifier;

import com.ibm.ws.arduino.Arduino;

public class ServiceManager {
    private final static Logger LOGGER = Logger.getLogger(ServiceManager.class.getName());

    public static final String DEFAULT_ID = "default";

    private static final String DEFAULT_SPEED = "1000000";

    private static Map<String, ArduinoAsyncImpl> arduinos = new HashMap<String, ArduinoAsyncImpl>();
    private static Map<String, Dictionary<String, ?>> configs = new HashMap<String, Dictionary<String, ?>>();

    public static synchronized Arduino get(String portName, String arduinoName) throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "get " + portName + ", "  + arduinoName);
        ArduinoAsyncImpl arduino = arduinos.get(portName);
        if (arduino != null) {
            if (arduinoName != null) {
                if (!!!arduinoName.equals(arduino.getArduinoName())) {
                    throw new IllegalStateException("Requested Arduino named " + arduinoName + " but found " + arduino.getArduinoName());
                }
            }
        } else {
            boolean defined = configs.containsKey(portName);
//            if (!!!DEFAULT_ID.equals(name) && !!!configs.containsKey(name)) {
//                throw new IllegalArgumentException("Arduino id not defined in server.xml: " + name);
//            }
            Dictionary<String, ?> config = configs.get(portName);
            if (config == null) {
                config = new Hashtable<String, String>(); // TODO: shouldn't this use Activator getDefaults?
            }
            String ip = (String) config.get("ip");
            String ports = (String) config.get("ports");
            if (ports == null && !!!defined) {
                ports = portName;
            }
            String speed = (String) config.get("speed");
            if (speed == null)
                speed = DEFAULT_SPEED;
            String debug = (String) config.get("debug");
            if (debug == null)
                debug = "0";
            arduino = new ArduinoAsyncImpl(ip, ports, Integer.parseInt(speed), Integer.parseInt(debug), arduinoName);
            arduinos.put(portName, arduino);
            arduino.open();
        }
        return arduino;
    }

    public static ArduinoAsyncImpl close(String id) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, "close " + id);
        ArduinoAsyncImpl a = arduinos.get(id);
        try {
            if (a != null)
                a.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }

    public static void remove(String id) {
        LOGGER.log(Level.FINE, "remove", id);
        configs.remove(id);
        close(id);
        arduinos.remove(id);
    }

    public static void closeAll() {
        LOGGER.log(Level.FINE, "closeAll");
        for (ArduinoAsyncImpl a : arduinos.values()) {
            if (a != null)
                a.close();
        }
    }

    public static void update(String id, Dictionary<String, ?> config) {
        if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "update: " + config);
        configs.put(id, config);
        ArduinoAsyncImpl a = arduinos.get(id);
        if (a == null) {
            arduinos.put(id, null);
        } else {
            if (a.isOpen()) {
                try {
                    a.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            a.update((String) config.get("ports"), (String) config.get("speed"), (String) config.get("debug"));
        }
    }

    public static List<String> getAvailablePortNames() {
        List<String> availablePorts = new ArrayList<String>();
        Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            availablePorts.add(currPortId.getName());
        }
        return availablePorts;
    }
}
