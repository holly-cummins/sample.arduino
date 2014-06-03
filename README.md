# The Liberty Profile Arduino Feature 

The Arduino feature for the Liberty profile enables connecting JavaEE applications to things in the real world. This makes possible creating Internet of Things applications which combine the enterprise capabilities of Liberty with cheap and easy to use Arduino microcontrollers and accessories. Think switching lights on/off from a webapp, streaming temperature sensor readings to a database, writing sophisticated GUI's for monitor and control of low power embedded devices. 

The Liberty Arduino feature provides a Java API which is designed in a way which makes it familiar and easy to use by anyone who has coded Arduino sketches. It uses a Firmata style protocol for comunicating between Liberty and Arduinos, which may be directly connected to the Liberty server via USB connections, or connected wirelessly by using any of the many different types of Arduino wireless accessories.

## Getting started

To use the feature you need to insatll the Liberty library into the Arduino IDE, and install the Arduino feature into the Liberty runtime. There is a helloworld style sample Liberty server that includes the Arduino feature and a simple JSP application that switches on and off an LED on an Arduino.

If you haven't already got a Liberty runtime and the Arduino IDE installed then get those first. Get Liberty from [IBM's WASdev](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-non-eclipse-environments/), and the Arduino IDE from the [Arduino website](http://arduino.cc/en/main/software) 

### Install the Liberty library to the Arduino IDE

The Liberty Arduino library is named Liberty.zip, download that to your local file system from the [github release page](http://github.com/WASdev/sample.arduino.wlp/releases/download/v0.0.19/Liberty.zip), and install it in the Arduino IDE - on the menu bar choose "Sketch -> Import Library -> Add Library..." and select library zip you just downloaded. 

You will need to then restart the Arduino IDE to pick up the new library. 

Note: to update the library after it has already been installed you must first manually delete the old version. To do that simply delete the Liberty directory from the Arduino libraries directory. For more details see the section on Manual installation on the [Arduino Libraries page](http://arduino.cc/en/Guide/Libraries)

### Program an Arduino with a Liberty sketch

In the Arduino IDE program an Arduino with the Liberty basic example. In the Arduino IDE menu bar choose "File -> Examples -> Liberty -> Basic", and then click the "Upload" button to upload the sketch to the Arduino. 

### Install the Arduino Feature in Liberty

The Arduino feature is available on the release page as a .esa file for manual installation of the feature, however there is also a pre-configured server download that includes the feature and a helloworld style sample application, and that is simplest way to get started.

Download the sample-server jar file from the [github release page](http://github.com/WASdev/sample.arduino.wlp/releases/download/v0.0.19/sample-server-0.0.19.jar), and at a command prompt in your Liberty wlp directory use the java command to install the sample server:

```java -jar sample-server-0.0.19.jar```

That creates a server named 'myServer', you will need to make one change to its config to tell it about your Arduino. Edit the config file ```wlp\usr\servers\myServer\server.xml```, look for the ```<usr_arduino>``` element, and update the ports value (presently "COM10") to match the serial port of your Arduino.

### Run the sample

Start your Liberty server with the following command in the Liberty wlp directory:

```bin\server run myServer```

Now on a web browser go to http://localhost:9080/helloworld 

You should see the helloworld page and refreshing the page should switch the Arduino LED on and off.

For further documentation about using the Arduino feature see the [wiki pages](http://github.com/WASdev/sample.arduino.wlp/wiki).

## Legal

Licensed under the Apache License v2.

COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing application programs conforming to the application programming interface for the operating platform for which the sample code is written. Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE. 

(C) Copyright IBM Corp. 2014
 
All Rights Reserved. Licensed Materials - Property of IBM.  
