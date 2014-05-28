# The Liberty Profile Arduino Feature

The Arduino Feature for the Liberty Profile enables interacting with Arduino microcontrollers from your Java EE application code running on Liberty. This enables building Internet of Things style systems which combine the features of the Liberty server with cheap and easy to use Arduino components for interacting with the real world.

The Liberty Arduino feature provides a Java API which is designed in a way which makes it familiar and easy to use by anyone who has coded Arduino sketches. It uses a Firmata style protocol for comunicating with Arduinos, which may be directly connected to the Liberty server via USB connections, or connected wirelessly by using one of the many different types of Arduino wireless accessories.

## Installation

If you haven't already got a Liberty runtime and the Arduino IDE installed then first install those. Get Liberty from [IBM's WASdev](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-non-eclipse-environments/), and the Arduino IDE from the [Arduino website](http://arduino.cc/en/main/software) 

The Liberty Arduino feature has two parts - a library for the Arduino IDE, and a feature for Liberty runtime.

### Install the Arduino library

Download the Arduino Liberty library to your local file system from: http://github.com/WASdev/sample.arduino.wlp/releases/download/v.0.0.15/Arduino-liberty-library-0.0.15.zip

Install the Arduino library in the Arduino IDE - on the menu bar choose "Sketch -> Import Library -> Add Library..." and select library zip you just downloaded. You will need to then restart the Arduino IDE to pick up the new library. 

### Install the Liberty Feature

At a command prompt in your Liberty wlp directory use the featureManager command to install the Arduino feature:

```bin\featuremanager install https://github.com/WASdev/sample.arduino.wlp/releases/download/v.0.0.15/arduino-feature-0.0.15.esa```

## A first app

As a helloworld style first app we'll use the Liberty Arduino feature to switch on and off an LED on an Arduino from a JSP running in a webapp on Liberty.  

### Program an Arduino with a Liberty sketch

In the Arduino IDE program an Arduino with the Liberty basic example. In the Arduino IDE menu bar choose "File -> Examples -> Liberty -> Basic", and then click the "Upload" button to upload the sketch to the Arduino.  

### Liberty setup

Use the following command in the Liberty wlp directory to create a server:

```bin\server create myServer```

Edit the config file ```wlp\usr\servers\myServer\server.xml``` and add the Arduino feature to the <featureManager> section and a <usr_arduino> element for your Arduino, updating the ports attribute value "COM10" to match the serial port of your Arduino:

```xml
<server description="new server">

    <!-- Enable features -->
    <featureManager>
        <feature>jsp-2.2</feature>
        <feature>usr:arduino-1.0</feature>
    </featureManager>

    <!-- To access this server from a remote client add a host attribute to the following element, e.g. host="*" -->
    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="9080"
                  httpsPort="9443" />

    <usr_arduino id="default" ports="COM10" />

</server>
```

### The JSP application

In the directory wlp\usr\servers\myServer\dropins create a new directory named helloworld.war and in there create a new file named index.jsp with the following contents:

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ibm.ws.arduino.*" %>
<%@ page import="static com.ibm.ws.arduino.Arduino.Level.LOW" %>
<%@ page import="static com.ibm.ws.arduino.Arduino.Level.HIGH" %>
<%@ page import="com.ibm.ws.arduino.Arduino.Mode" %>

<html>
  <body >

    <h2>Liberty Arduino Demo - Helloworld</h2>

     Refresh the page to switch on and off

<%
     Arduino arduino = ArduinoService.get();

     int led = 13;    
     arduino.pinMode(led, Mode.OUTPUT);

     if (arduino.digitalRead(led) == LOW) {

       arduino.digitalWrite(led, HIGH);
       %><p><b>Light on!</b></p><%

    } else {

       arduino.digitalWrite(led, LOW);
       %><p><b>Light off!</b></p><%

    }
%>
  </body>
</html>
```

### Test the application

Start your Liberty server with the following command in the Liberty wlp directory:

```bin\server run myServer```

Now on a web browser go to http://localhost:9080/helloworld. 
You should see the helloworld page, refreshing the page should switch the Arduino LED on and off.

## Legal

COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing application programs conforming to the application programming interface for the operating platform for which the sample code is written. Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE. 

(C) Copyright IBM Corp. 2014
 
All Rights Reserved. Licensed Materials - Property of IBM.  

	 

