<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ibm.wasdev.arduino.*;" %>
<html>
  <body >

   <h2>Liberty Arduino Temperature</h2>
<%
    Arduino arduino = ArduinoService.get();
%> 
   <p>Current temperature: <%= toDegrees(arduino.analogRead(5)) %>C</p>

<%!
    public int toDegrees(int r) {
       double v = 5000 / 1024f;  // 5 volts divided by 10 bit resolution
       int c = (int) ((r * v) - 500) / 10; // minus 500 millivolt offset, divide by 10 for degrees C
       return c;
    }
%>
  </body>
</html>
