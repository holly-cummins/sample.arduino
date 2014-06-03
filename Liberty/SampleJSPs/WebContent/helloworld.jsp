<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ibm.ws.sample.arduino.*" %>
<%@ page import="static com.ibm.ws.sample.arduino.Arduino.Level.LOW" %>
<%@ page import="static com.ibm.ws.sample.arduino.Arduino.Level.HIGH" %>
<%@ page import="static com.ibm.ws.sample.arduino.Arduino.Mode.OUTPUT" %>
<html>
  <body >
    <h2>Liberty Arduino Demo - Helloworld</h2>
     Refresh the page to switch on and off
    <p><b>
    <%
        Arduino arduino = ArduinoService.get();
        int led = 13;    
        arduino.pinMode(led, OUTPUT);
        if (arduino.digitalRead(led) == LOW) {
           arduino.digitalWrite(led, HIGH);
           %>Light on!<%
        } else {
           arduino.digitalWrite(led, LOW);
           %>Light off!<%
        }
    %>
    </b></p>
  </body>
</html>
