<%@page import="java.io.IOException"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ibm.ws.arduino.*" %>
<%@ page import="static com.ibm.ws.arduino.Arduino.Level.LOW" %>
<%@ page import="static com.ibm.ws.arduino.Arduino.Level.HIGH" %>
<%@ page import="com.ibm.ws.arduino.Arduino.Mode" %>
<html>
  <body >

    <h2>Liberty Arduino Demo</h2>

<%
    Arduino arduino = ArduinoService.get();
    arduino.clearCallbacks();

    int pin = 8;
    arduino.pinMode(pin,Mode.INPUT);
    arduino.digitalWrite(pin,HIGH);
    
    arduino.digitalCallback(8, HIGH, new Callback() {
	public void triggered(int value) {
	  try {
    	 Arduino uView = ArduinoService.get("MicroView");
		 uView.sramWrite(0, "Human!");
		 int ok = uView.invoke("updateLCD");
	  } catch (IOException e) {
	     e.printStackTrace();
	  }
	  System.out.println("pin 8 triggered!");
	}
	public void reset(int value) {
	  try {
    	 Arduino uView = ArduinoService.get("MicroView");
		 uView.sramWrite(0, "Human  Gone.");
		 int ok = uView.invoke("updateLCD");
	  } catch (IOException e) {
	     e.printStackTrace();
	  }
	  System.out.println("pin 8 reset");
	}
    });

%>
     Callback on pin 8 goes HI set

  </body>
</html>

