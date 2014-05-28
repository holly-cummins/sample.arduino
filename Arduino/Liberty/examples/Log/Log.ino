/*
  Log

  Example showing how to use the Liberty log function to log messages
  from the Arduino sketch to the Liberty server log.
  
 */
#include <EEPROM.h>
#include <Liberty.h>

Liberty liberty;

unsigned long lastTime = 0;

void setup() {
   liberty.begin();
}

void loop(){
  liberty.update();

  // log a message every 2 seconds 
  unsigned long x = micros();
  if ((x - lastTime) > 2000000) {
     lastTime = x;
     liberty.log(String(x));
  }    

}