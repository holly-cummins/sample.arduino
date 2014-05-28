<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ibm.ws.arduino.*;" %>

<jsp:useBean id="state" class="java.util.Date" scope="session" />

<html>
  <body >

    <h2>Liberty Arduino Demo - mains switch</h2>

     Refresh the page to switch on and off

<%
        Arduino arduino = ArduinoService.get();

        if (state.getTime() != 1) {

           arduino.invoke("on");
           state.setTime(1);
    %><p><b>Mains on!</b></p><%

    } else {

       arduino.invoke("off");
       state.setTime(0);
       %><p><b>Mains off!</b></p><%

    }
%>
  </body>
</html>

