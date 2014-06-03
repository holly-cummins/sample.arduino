package com.ibm.wasdev.arduino.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class BlunoTest {

    public static void main(String[] args) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException, IOException, InterruptedException {

//        jtermios.JTermios.JTermiosLogging.setLogMask(0xFF);
        
        CommPortIdentifier commPort = CommPortIdentifier.getPortIdentifier("COM15");
        SerialPort serialPort = (SerialPort) commPort.open(BlunoTest.class.getName(), 2000);
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        serialPort.setDTR(true); // needed for Arduino Leonardo and Micro

        OutputStream os = serialPort.getOutputStream();

        
//        while (true) {
//            System.out.println("writing");            
//            os.write("Hello\n".getBytes());
//            os.flush();
//            System.out.println("write sleeping");            
//            Thread.sleep(10000);
//        }
        
        Thread.sleep(1700);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        doit1(serialPort, os);
        
    }

    private static InputStream doit1(SerialPort serialPort, OutputStream os) throws InterruptedException, IOException {
//        Thread.sleep(1700);
        os.write("1,16\n".getBytes());
        os.flush();
  //      Thread.sleep(500);
        InputStream is = serialPort.getInputStream();
        int c = 0;
        while ((c = is.read()) != -1) {
            System.out.print((char)c);
            if (c == '\n') break;
        }
        return is;
    }
}
