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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import purejavacomm.CommPortIdentifier;
import purejavacomm.CommPortOwnershipListener;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import com.ibm.ws.arduino.Arduino;
import com.ibm.ws.arduino.Callback;
import com.ibm.ws.arduino.Notification;

public class ArduinoAsyncImpl implements Arduino, CommPortOwnershipListener, Runnable {
    private final static Logger LOGGER = Logger.getLogger(ArduinoAsyncImpl.class.getName());

    private static final int CMD_PIN_MODE = 1;
    private static final int CMD_DIGITAL_READ = 2;
    private static final int CMD_DIGITAL_WRITE = 3;
    private static final int CMD_ANALOG_READ = 4;
    private static final int CMD_ANALOG_WRITE = 5;
    private static final int CMD_EEPROM_READ = 6;
    private static final int CMD_EEPROM_WRITE = 7;
    private static final int CMD_SRAM_READ_BYTES = 8;
    private static final int CMD_SRAM_WRITE_BYTES = 9;
    private static final int CMD_INVOKE = 10;
    private static final int CMD_EEPROM_READ_STRING = 11;
    private static final int CMD_SRAM_READ_STRING = 12;
    private static final int CMD_CALLBACK = 13;
    private static final int CMD_CLEAR_CALLBACKS = 15;
    private static final int CMD_VERSION = 16;
    private static final int CMD_REMOTE = 17;
    private static final int CMD_CALLBACK_TRIGGERED = 31;
    private static final int CMD_CALLBACK_RESET = 32;
    private static final int CMD_NAMED_CALLBACK_FIRED = 33;
    private static final int CMD_LOG = 34;

    private static final int RESPONSE_OK = 0;

    private static Map<Integer, String> ERROR_MSGS;
    static {
        ERROR_MSGS = new HashMap<Integer, String>();
        ERROR_MSGS.put(99, "Uknown command");
        ERROR_MSGS.put(98, "Argument error");
        ERROR_MSGS.put(97, "Function not found");
        ERROR_MSGS.put(96, "Too many callbacks");
        ERROR_MSGS.put(95, "Wrong number of function arguments");
        ERROR_MSGS.put(94, "Callback function cannot have arguments");
        ERROR_MSGS = Collections.unmodifiableMap(ERROR_MSGS);
    }

    private String possibleCommPorts;
    
    private CommPortIdentifier commPort;
    private SerialPort serialPort;
    private String commPortName;

    private OutputStreamWriter portWriter;
    private BufferedReader portReader;

    private Socket socket;
    private String ip = "192.168.1.94";
    private int port = 2000;

    // 1000000 seems to work fast and reliably, could drop down to 115200 if problems
    private int speed = 1000000;

    private boolean asyncMode = false;

    private int pendingResponseId;
    private Thread responseReaderThread;
    private volatile String asyncResponse;
    private final Object responseMutex = new Object();

    private Map<Integer, Callback> callbacks = new ConcurrentHashMap<Integer, Callback>();
    private Map<String, List<Notification>> notifications = new ConcurrentHashMap<String, List<Notification>>();

    ExecutorService executor = Executors.newFixedThreadPool(5);

    private long timeout = 1000; // 1 seconds
    private int retries = 5;
    
    private int debug = 0;

    private String arduinoLibVersion;
    private String arduinoName;
    private String targetArduinoName;

    public ArduinoAsyncImpl(String ip, String commPorts, int speed, int debug, String arduinoName) {
        this.ip = ip;
        this.possibleCommPorts = commPorts;
        this.speed = speed;
        asyncMode = true;
        this.debug = debug;
        this.targetArduinoName = arduinoName;
    }

    public synchronized void open() throws IOException {
        try {

            if (ip != null) {
                socket = new Socket(ip, port);
                socket.setSoTimeout(10000);
                InputStream is = socket.getInputStream();

                // initial response from RN-XV wifly module is *HELLO* so read and discard
                is.read(); is.read(); is.read(); is.read(); is.read(); is.read(); is.read();

                portReader = new BufferedReader(new InputStreamReader(is));
                portWriter = new OutputStreamWriter(socket.getOutputStream());

            } else {
                
                if (debug > 1) 
                   jtermios.JTermios.JTermiosLogging.setLogMask(0xFF);
                
                this.commPort = findCommPort();
                this.commPortName = commPort.getName();

                serialPort = (SerialPort) commPort.open(this.getClass().getName(), 2000);
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                serialPort.setDTR(true); // needed for Arduino Leonardo and Micro

                portWriter = new OutputStreamWriter(serialPort.getOutputStream());
                portReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

                // don't timeout the async receive thread but for sync no timeout hangs close
                if (!!!asyncMode) serialPort.enableReceiveTimeout(3000);
                commPort.addPortOwnershipListener(this);

            }

            if (asyncMode) {
                responseReaderThread = new Thread(this);
                responseReaderThread.start();
            }
            
            updateArduinoVersion();
            
            if (targetArduinoName != null && !!!targetArduinoName.equals(arduinoName)) {
                throw new IllegalStateException("Requested Arduino named " + targetArduinoName + " but found " + arduinoName);
            }
            
            LOGGER.log(java.util.logging.Level.INFO, "Arduino" + (arduinoName != null ? (" \""+arduinoName+"\"") : "") + " opened on " + (ip != null ? ip : commPortName) + " running library version: " + arduinoLibVersion);

        } catch (UnsupportedCommOperationException e) {
            throw new IOException(e);
        } catch (PortInUseException e) {
            throw new IOException(e);
        }
    }

    public boolean isOpen() {
        return serialPort != null || socket != null;
    }

    public synchronized void close() {
        if (commPort != null) {
            if (commPort != null)
                commPort.removePortOwnershipListener(this);
            if (serialPort != null)
                serialPort.close();
            serialPort = null;
            portReader = null;
            portWriter = null;
            responseReaderThread = null;
            commPort = null;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            jtermios.JTermios.JTermiosLogging.setLogMask(0x00);
            LOGGER.log(java.util.logging.Level.INFO, "Arduino closed on "  + (ip != null ? ip : commPortName));
        }
    }

    private CommPortIdentifier findCommPort() {
        CommPortIdentifier commPort = null;
        Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
        List<String> availablePorts = new ArrayList<String>();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (possibleCommPorts.contains(currPortId.getName())) {
                commPort = currPortId;
                break;
            }
            availablePorts.add(currPortId.getName());
        }
        if (commPort == null) {
            throw new IllegalArgumentException("Could not find COM port from : " + possibleCommPorts + ", in available ports: " + availablePorts);
        }
        return commPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#pinMode(int, com.ibm.ws.arduino.impl.ArduinoImpl.Mode)
     */
    @Override
    public void pinMode(int pin, Mode mode) throws IOException {
        parseOkResponse(doCommand(formatCommand(CMD_PIN_MODE, pin, mode.getValue())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#digitalRead(int)
     */
    @Override
    public Level digitalRead(int pin) throws IOException {
        return parseValueResponse(doCommand(formatCommand(CMD_DIGITAL_READ, pin))) == 0 ? Level.LOW : Level.HIGH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#digitalWrite(int, int)
     */
    @Override
    public void digitalWrite(int pin, Level value) throws IOException {
        parseOkResponse(doCommand(formatCommand(CMD_DIGITAL_WRITE, pin, value.getValue())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#analogRead(int)
     */
    @Override
    public int analogRead(int pin) throws IOException {
        return parseValueResponse(doCommand(formatCommand(CMD_ANALOG_READ, pin)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#analogWrite(int, int)
     */
    @Override
    public void analogWrite(int pin, int value) throws IOException {
        parseOkResponse(doCommand(formatCommand(CMD_ANALOG_WRITE, pin, value)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#eepromRead(int, int)
     */
    @Override
    public byte[] eepromRead(int address, int length) throws IOException {
        if (length == 0) return new byte[0];        
        return parseBytesResponse(doCommand(formatCommand(CMD_EEPROM_READ, address, length)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#eepromReadString(int)
     */
    @Override
    public String eepromReadString(int address) throws IOException {
        return new String(parseBytesResponse(doCommand(formatCommand(CMD_EEPROM_READ_STRING, address))));
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#eepromWrite(int, byte[])
     */
    @Override
    public void eepromWrite(int address, byte[] bytes) throws IOException {
        if (bytes.length == 0) return; 
        StringWriter sw = new StringWriter();
        for (int i=0; i<bytes.length; i++) {
            sw.append(String.valueOf(bytes[i]));
            if (i < bytes.length-1) {
                sw.append(',');
            }
        }
        parseOkResponse(doCommand(CMD_EEPROM_WRITE + "," + address + "," + bytes.length + "," + sw.toString() + "\n"));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#eepromWrite(int, String)
     */
    @Override
    public void eepromWrite(int address, String s) throws IOException {
        byte[] bytes = new byte[s.length()+1];
        System.arraycopy(s.getBytes(), 0, bytes, 0, s.length());
        bytes[s.length()] = 0; // Arduino strings are null terminated
        eepromWrite(address, bytes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#sramRead(int, int)
     */
    @Override
    public byte[] sramRead(int address, int length) throws IOException {
        if (length == 0) return new byte[0];        
        return parseBytesResponse(doCommand(formatCommand(CMD_SRAM_READ_BYTES, address, length)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#sramReadString(int)
     */
    @Override
    public String sramReadString(int address) throws IOException {
        return new String(parseBytesResponse(doCommand(formatCommand(CMD_SRAM_READ_STRING, address))));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#sramWrite(int, byte[])
     */
    @Override
    public void sramWrite(int address, byte[] bytes) throws IOException {
        if (bytes.length == 0) return; 
        StringWriter sw = new StringWriter();
        for (int i=0; i<bytes.length; i++) {
            sw.append(String.valueOf(bytes[i]));
            if (i < bytes.length-1) {
                sw.append(',');
            }
        }
        parseOkResponse(doCommand(CMD_SRAM_WRITE_BYTES + "," + address + "," + bytes.length + "," + sw.toString() + "\n"));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#sramWrite(int, String)
     */
    @Override
    public void sramWrite(int address, String s) throws IOException {
        byte[] bytes = new byte[s.length()+1];
        System.arraycopy(s.getBytes(), 0, bytes, 0, s.length());
        bytes[s.length()] = 0; // Arduino strings are null terminated
        sramWrite(address, bytes);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#invoke(string)
     */
    @Override
    public int invoke(String function) throws IOException {
        return parseValueResponse(doCommand(CMD_INVOKE + ",0," + function.length() + function + "\n"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#invoke(string)
     */
    @Override
    public int invoke(String function, int x) throws IOException {
        return parseValueResponse(doCommand(CMD_INVOKE + ",1," + x + "," + function.length() + function + "\n"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#invoke(string)
     */
    @Override
    public int invoke(String function, int x, int y) throws IOException {
        return parseValueResponse(doCommand(CMD_INVOKE + ",2," + x + "," + y + "," + function.length() + function + "\n"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.ws.arduino.impl.Arduino#digitalCallback(int, int, Callback)
     */
    @Override
    public int digitalCallback(int pin, Level state, Callback cb) throws IOException {
        if (state != Level.HIGH && state != Level.LOW)
            throw new IllegalArgumentException("State must be HI or LOW");
        int cbid = getNextCallbackID();
        callbacks.put(cbid, cb);
        parseOkResponse(doCommand(formatCommand(CMD_CALLBACK, cbid, 0, state.getValue(), 0, pin)));
        return cbid;
    }

    @Override
    public int analogCallback(int pin, Comparitor comparitor, int value, Callback cb) throws IOException {
        int cbid = getNextCallbackID();
        callbacks.put(cbid, cb);
        parseOkResponse(doCommand(formatCommand(CMD_CALLBACK, cbid, 1, value, comparitor.getValue(), pin)));
        return cbid;
    }

    @Override
    public int functionCallback(String function, Comparitor comparitor, int value, Callback cb) throws IOException {
        int cbid = getNextCallbackID();
        callbacks.put(cbid, cb);
        parseOkResponse(doCommand(CMD_CALLBACK + "," + cbid + "," + 2 + "," + value + "," + comparitor.getValue() + "," + function.length()
                + function + "\n"));
        return cbid;
    }

    @Override
    public void addNotification(String name, Notification n) {
        List<Notification> ns = notifications.get(name);
        if (ns == null) {
            ns = new ArrayList<Notification>();
            notifications.put(name, ns);
        }
        ns.add(n);
    }

    @Override
    public void removeNotification(Notification n) {
        for (List<Notification> ns : notifications.values()) {
            for (Notification nx : ns) { 
                if (nx.equals(n)) {
                    ns.remove(n);
                    return;
                }
            }
        }
    }

    private String formatCommand(int command, int... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        for (int arg : args) {
            sb.append(",");
            sb.append(arg);
        }
        sb.append("\n");
        return sb.toString();
    }

    private int[] parseResponse(String response) {
        String[] strArray = response.split(",");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    private int parseValueResponse(String response) throws IOException {
        int[] intArray = parseResponse(response);

        checkResponse(intArray[0]);

        if (intArray.length < 2) {
            return -1; // TODO: shouldn't this throw an exception?
        }
        return intArray[1];
    }

    private byte[] parseBytesResponse(String response) throws IOException {
        String[] strArray = response.split(",");
        checkResponse(Integer.parseInt(strArray[0]));
        
        byte[] bytes = new byte[strArray.length-1];
        for (int i=0; i<bytes.length; i++) {
           bytes[i] = (byte)Integer.parseInt(strArray[i+1]);            
        }
        
        return bytes;
    }
    
    private void parseOkResponse(String response) throws IOException {
        int[] intArray = parseResponse(response);
        checkResponse(intArray[0]);
    }

    private void checkResponse(int response) throws IOException {
        if (response != RESPONSE_OK) {
            String error;
            if (ERROR_MSGS.containsKey(response)) {
                error = ERROR_MSGS.get(response);
            } else {
                error = String.valueOf(response);
            }
            throw new IOException(error);
        }
    }

    private synchronized String doCommand(String command) throws IOException {
        int retry = 0;
        while (retry++ < retries) {
            try {
                return doCommand2(command);
            } catch (TimeoutException e) {
                LOGGER.log(java.util.logging.Level.FINE, ("retry " + retry + " after response timeout"));
            }
          }
        throw new IOException("no response received after " + retries + " retries");
    }
    
    private synchronized String doCommand2(String command) throws IOException, TimeoutException {
        if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "doCommand: " + command);

        if (!!!isOpen()) {
            open();
        }

        String response = null;
        
        try {
            if (asyncMode) {
                pendingResponseId = getNextRequestID();
                command = pendingResponseId + "," + command;
            }

            String remoteName = threadRemoteName.get();
            if (remoteName != null) {
                command = pendingResponseId + "," + CMD_REMOTE + ',' + remoteName + ',' + command;
            }

            if (debug > 0) System.out.println("cmd: " + command);

            portWriter.write(command);
            portWriter.flush();

            if (!!!asyncMode) {
                delay(); // without small delay sometimes response get missed resulting in hangs
                response = portReader.readLine();
            } else {
                if (asyncResponse == null) {
                    synchronized (responseMutex) {
                        if (asyncResponse == null) {
                            try {
                                responseMutex.wait(timeout);
                            } catch (InterruptedException e) {
                                if (LOGGER.isLoggable(java.util.logging.Level.FINE))
                                    LOGGER.log(java.util.logging.Level.FINE, "doCommand wait interupted", e);
                            }
                        }
                    }
                }
                response = asyncResponse;
                asyncResponse = null;
            }

            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "doCommand response: " + response);

            if (response != null) {
                return response;
            }

        } catch (IOException e) {
            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "doCommand IOException: " + e);
            close();
            throw e;
        }

        //  if its got here then the response is null
        throw new TimeoutException("no response received");
    }

    protected String getRemoteName() {
        return null;
    }

    private void delay() {
//         try {
//         Thread.sleep(50);
//         } catch (InterruptedException e) {
//         e.printStackTrace();
//         }
    }

    public void update(String ports, String speed, String debug) {
        if (LOGGER.isLoggable(java.util.logging.Level.FINE))
            LOGGER.log(java.util.logging.Level.FINE, "update " + ports + " " + speed);
        if (isOpen()) {
            throw new IllegalStateException();
        }
        if (speed != null) {
            this.speed = Integer.parseInt(speed);
        } else {
            this.speed = 1000000;
        }
        if (ports != null) {
            possibleCommPorts = ports;
        }
        if (debug != null) {
            this.debug = Integer.parseInt(debug);
        } else {
            this.debug = 0;
        }
    }

    public void ownershipChange(int arg0) {
        if (arg0 == CommPortOwnershipListener.PORT_UNOWNED) {
            if (LOGGER.isLoggable(java.util.logging.Level.FINE))
                LOGGER.log(java.util.logging.Level.FINE, "CommPortOwnershipListener closing port " + commPort.getName());
            close();
        }
    }

    @Override
    public void run() {
        BufferedReader portReader = this.portReader;
        while (isOpen()) {
            try {
                delay();
                if (this.portReader == null)
                    return; // closed

                String response = portReader.readLine();
                if (response != null && response.startsWith(" "))
                    response = response.trim();

                if (debug > 0) System.out.println("rsp: " + response);

                if (LOGGER.isLoggable(java.util.logging.Level.FINE))
                    LOGGER.log(java.util.logging.Level.FINE, "run response: " + response);

                if (response != null && response.length() > 0) {
                    int i = response.indexOf(',');
                    if (i < 1) {
                        LOGGER.log(java.util.logging.Level.FINE, "invalid response: " + response);
                    } else {
                        int cmd = Integer.parseInt(response.substring(0, i));
                        if (cmd == pendingResponseId) {

                            this.asyncResponse = response.substring(i + 1);
                            synchronized (responseMutex) {
                                responseMutex.notify();
                            }

                        } else if (cmd == CMD_CALLBACK_TRIGGERED || cmd == CMD_CALLBACK_RESET || cmd == CMD_NAMED_CALLBACK_FIRED) {

                            processCallback(cmd, response);

                        } else if (cmd == CMD_LOG) {

                            log(response);

                        } else {
                            LOGGER.log(java.util.logging.Level.FINE, "unknown response: " + response);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(java.util.logging.Level.FINE, e.getMessage(), e);
                close();
                return;
            }
        }
    }

    private void log(String response) {
        String msg = "Arduino '" + commPort.getName() + "': " + response.substring(response.indexOf(',') + 1);
        if (LOGGER.isLoggable(java.util.logging.Level.INFO))
            LOGGER.log(java.util.logging.Level.INFO, msg);
    }

    private void processCallback(int cmd, String response) {
        if (cmd == CMD_NAMED_CALLBACK_FIRED) {
            String[] strArray = response.split(",");
            List<Notification> nList = notifications.get(strArray[1]);
            if (nList != null) {
                for (Notification n : nList) {
                    runCallback(cmd, strArray[2], n, Integer.parseInt(strArray[3]));
                }
            }
        } else {
            final int[] intArray = parseResponse(response);
            Callback cb = callbacks.get(intArray[1]);
            runCallback(cmd, String.valueOf(intArray[1]), cb, intArray[2]);
        }
    }

    private void runCallback(final int type, final String id, final Object cb, final int value) {
        if (cb != null) {
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (type == CMD_CALLBACK_TRIGGERED) {
                            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "calling callback triggered() for callbackId: " + id);
                            ((Callback)cb).triggered(value);
                        } else if (type == CMD_NAMED_CALLBACK_FIRED) {
                            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "calling event for notification: " + id);
                            ((Notification)cb).event(id, value);
                        } else {
                            if (LOGGER.isLoggable(java.util.logging.Level.FINE)) LOGGER.log(java.util.logging.Level.FINE, "calling callback reset() for callbackId: " + id);
                            ((Callback)cb).reset(value);
                        }
                    } catch (Throwable e) {
                        LOGGER.log(java.util.logging.Level.FINE, "exception while running callback: " + e.getMessage(), e);
                    }
                }
            };
            executor.execute(worker);
        } else {
            LOGGER.log(java.util.logging.Level.FINE, "unknown callback: " + id);
        }
    }

    private int id = 0;

    private synchronized int getNextRequestID() {
        this.id = id + 1;
        // TODO: this is a bit clunky - currently id 31 and 32 are the callback command id's
        // 33 is call cmd.
        // 34 is log cmd. should have a cleaner way to do this
        if (id == 31)
            id = id + 4;
        if (id == 32)
            id = id + 3;
        if (id == 33)
            id = id + 2;
        if (id == 34)
            id = id + 1;
        return id;
    }

    private int cbid = 0;

    private synchronized int getNextCallbackID() {
        this.cbid = cbid + 1;
        return cbid;
    }

    @Override
    public Arduino getRemote(String name) {
        return new RemoteArduino(this, name);
    }

    ThreadLocal<String> threadRemoteName = new ThreadLocal<String>();

    public void setRemote(String name) {
        threadRemoteName.set(name);
    }

    public void removeRemote() {
        threadRemoteName.remove();
    }

    @Override
    public void clearCallbacks() throws IOException {
        parseOkResponse(doCommand(formatCommand(CMD_CLEAR_CALLBACKS)));
    }

    private void updateArduinoVersion() throws IOException {
        String response = doCommand(formatCommand(CMD_VERSION));
        String[] strArray = response.split(",");
        checkResponse(Integer.parseInt(strArray[0]));
        arduinoLibVersion = strArray[1];
        if (strArray.length == 3) {
            arduinoName = strArray[2];
        }
    }

    public String getArduinoLibVersion() {
        return arduinoLibVersion;
    }

    public String getArduinoName() {
        return arduinoName;
    }

}
