#include <EEPROM.h>
#include <Liberty.h>
#include <SoftwareSerial.h>

SoftwareSerial btSerial(7, 8); // RX, TX

Liberty liberty;

void setup() {
   liberty.invocable("configure", &configure);
   liberty.begin();
}

void loop() {
  liberty.update();
  
//  configure();
  
  //delay(1000);
  //long b = getBaud();
  //char bc[6];
  //String(b).toCharArray(bc, 6);
  //liberty.log(bc);
 // liberty.log("Configure done");
  
}

int configure() {
  liberty.log("configure");
  
  long currBaud = getBaud();
  liberty.log("Current baud:");
  
  char bs[7];
  String(currBaud).toCharArray(bs,7);
  liberty.log(bs);
  
  btSerial.begin(currBaud); 
  delay(600); 
  
  setPin();
  setName();
  setBaud();

  liberty.log("done");
    
  return 0;
}

void setBaud() {
  liberty.log("setBaud");
  char cmd[9] = "AT+BAUD";
  cmd[7] = (char)liberty.sramRead(29);  
  cmd[8] = 0;
//  btSerial.print(cmd);  
  liberty.log(cmd);
  delay(500);
}

void setName() {
  liberty.log("setName");

  char cmd[28] = "AT+NAME";
  int i;
  for (i=1;i<20;i++) {
     cmd[i+6] = liberty.sramRead(i);
     if (cmd[i+6] == 0) break;
  }
  //btSerial.print(cmd);  
  liberty.log(cmd);

  delay(500);  
}

void setPin() {
  liberty.log("setPin");
  char cmd[11] = "AT+PIN";
  cmd[6] = (char)liberty.sramRead(23);
  cmd[7] = (char)liberty.sramRead(24);
  cmd[8] = (char)liberty.sramRead(25);
  cmd[9] = (char)liberty.sramRead(26);
  cmd[10] = 0;
//  btSerial.print("AT+PIN7654");  
  btSerial.print('A');  
  delay(5);
  btSerial.print('T');  
  delay(5);
  btSerial.print('+');  
  delay(5);
  btSerial.print('P');  
  delay(5);
  btSerial.print('I');  
  delay(5);
  btSerial.print('N');  
  delay(5);
  btSerial.print('7');  
  delay(5);
  btSerial.print('6');  
  delay(5);
  btSerial.print('5');  
  delay(5);
  btSerial.print('4');  
  delay(5);
  liberty.log(cmd);
  delay(600);
  btSerial.read();  
}

long getBaud() {
  liberty.log("getBaud");
  if (testBaud(115200))
     return 115200;
  else if (testBaud(9600)) 
     return 9600;
  else if (testBaud(19200)) 
     return 19200;
  else if (testBaud(57600)) 
     return 57600;
  else if (testBaud(115200)) 
     return 115200;
  return 0;
}

boolean testBaud(long baud) {
//  char bs[7];
//  String(baud).toCharArray(bs,7);
//  liberty.log(bs);
  btSerial.end(); 
  delay(100); 
  btSerial.begin(baud); 
  delay(100); 
  btSerial.print("AT");  
  delay(600); 
  char c1 = btSerial.read();
  delay(10); 
  char c2 = btSerial.read();
  delay(10); 

  char cs[2] = { c1, c2};
 // liberty.log(cs);

  if (c1 == 'O' && c2 == 'K') {
    return true;
  } else {
    return false;
  }
}