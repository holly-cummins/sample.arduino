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

/** -------------------------------------------------------------------------------------------
 * WebSphere Liberty Arduino Library
 *
 * This library enables user applications running on a Liberty server to interact with an
 * Arduino microcontroller, including reading/writing to Arduino I/O pins and memory, invoking
 * functions on the Arduino and having Arduino make callbacks to Liberty application code.
 *
 * ------------------------------------------------------------------------------------------- */

#include "Arduino.h"
#include "Liberty.h"
#include "EEPROM.h"

#define OK 0
#define LOG 34
#define BAD_COMMAND 99
#define ARGS_ERROR 98
#define FUNCTION_NOT_FOUND 97
#define TOO_MANY_CALLBACKS_ERROR 96
#define NARGS_ERROR 95
#define CBFUNCTION_ARGS_ERROR 94
#define NO_SRAM_ERROR 93
#define SRAM_OVERFLOW_ERROR 92

#define CALLBACK_TRIGGERED 31
#define CALLBACK_RESET 32
#define NOTIFICATION_EVENT 33

#define MAX_INVOKERS 10
#define MAX_CALLBACKS 10

#define VERSION "1.0.0"

typedef enum CMD {
        CMD_NOT_USEDNOW1,       //0
        CMD_PIN_MODE,           //1
        CMD_DIGITAL_READ,       //2
        CMD_DIGITAL_WRITE,      //3
        CMD_ANALOG_READ,        //4
        CMD_ANALOG_WRITE,       //5
        CMD_EEPROM_READ,        //6
        CMD_EEPROM_WRITE,       //7
        CMD_SRAM_READ,          //8
        CMD_SRAM_WRITE,         //9
        CMD_INVOKE,             //10
        CMD_EEPROM_READ_STRING, //11
        CMD_SRAM_READ_STRING,   //12
        CMD_CALLBACK,           //13
        CMD_NOOP,               //14
        CMD_CLEAR_CALLBACKS,    //15
        CMD_VERSION             //16
} command_t;

char *arduinoName = NULL;

unsigned long baud = 115200;

int readTimeout = 500; 

byte *sramBytes;
int sramSize;

int currentCmdId;

const char *invokerNames[MAX_INVOKERS];
typedef int (*fx0)();
typedef int (*fx1)(int);
typedef int (*fx2)(int, int);
int (*invokerFs[MAX_INVOKERS])();
int fargs[MAX_INVOKERS];
int invokers = 0;

typedef enum COMPARITOR {
	CMP_LT,             //0
	CMP_EQ,             //1
	CMP_GT,             //2
	CMP_CHG             //3
} comparitor_t;

// callbacks
typedef struct {
  int callbackId;
  int callbackType; // digital, analog, function
  int pin; // pin or invokerId
  int comparitor;  // -1 is lessThan, 0 is equal, +1 is greaterThan, +2 is changedBy
  int triggerValue;
  int lastFiredValue;
} callback;

callback callbacks[MAX_CALLBACKS];
int currentCallbacks = 0;


// -------------------------------------------------------------------------------------------

Liberty::Liberty() {
}

Liberty::Liberty(char *name) {
  arduinoName = name;
}

Liberty::Liberty(unsigned long speed) {
  baud = speed;
}

Liberty::Liberty(char *name, unsigned long speed) {
  arduinoName = name;
  baud = speed;
}

void Liberty::begin() {
  Serial.begin(baud);
  while (!Serial);
}

void Liberty::update() {
   if (Serial.available() > 0) {

      currentCmdId = Serial.parseInt();
      int cmd = Serial.parseInt();

      switch (cmd) {
         case CMD_PIN_MODE: {
            doPinMode();
            break;
         }
         case CMD_DIGITAL_READ: {
            doDigitalRead();
            break;
         }
         case CMD_DIGITAL_WRITE: {
            doDigitalWrite();
            break;
         }
         case CMD_ANALOG_READ: {
            doAnalogRead();
            break;
         }
         case CMD_ANALOG_WRITE: {
            doAnalogWrite();
            break;
         }
         case CMD_EEPROM_READ: {
            doEepromRead();
            break;
         }
         case CMD_EEPROM_WRITE: {
            doEepromWrite();
            break;
         }
         case CMD_EEPROM_READ_STRING: {
            doEepromReadString();
            break;
         }
         case CMD_SRAM_READ: {
            doSramRead();
            break;
         }
         case CMD_SRAM_WRITE: {
            doSramWrite();
            break;
         }
         case CMD_INVOKE: {
            doInvoke();
            break;
         }
         case CMD_SRAM_READ_STRING: {
            doSramReadString();
            break;
         }
         case CMD_CALLBACK: {
            doCallback();
            break;
         }
         case CMD_NOOP: {
            doNoop();
            break;
         }
         case CMD_CLEAR_CALLBACKS: {
            doClearCallbacks();
            break;
         }
         case CMD_VERSION: {
            doVersion();
            break;
         }
         default: 
            sendResponse(BAD_COMMAND);           
      }
   }

   if (currentCallbacks > 0) {
     checkCallbacks();
   } 

   Serial.flush();
}

void Liberty::doPinMode() {
  int pin = Serial.parseInt(); 
  int value = Serial.parseInt();
  if (cmdEndOk()) {
    if (value == 0) {
      pinMode(pin, INPUT);
    } else if (value == 1) {
      pinMode(pin, OUTPUT);
    } else {
      pinMode(pin, INPUT_PULLUP);
    }
    sendResponse(OK);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doAnalogRead() {
  int pin = Serial.parseInt(); 
  if (cmdEndOk()) {
    int v = analogRead(pin);
    sendResponseValue(v);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doAnalogWrite() {
  int pin = Serial.parseInt(); 
  int value = Serial.parseInt(); 
  if (cmdEndOk()) {
    analogWrite(pin, value);
    sendResponse(OK);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doDigitalRead() {
  int pin = Serial.parseInt(); 
  if (cmdEndOk()) {
    int value = digitalRead(pin);
    sendResponseValue(value == 0 ? LOW : HIGH);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doDigitalWrite() {
  int pin = Serial.parseInt(); 
  int value = Serial.parseInt(); 
  if (cmdEndOk()) {
    if (value == 0) {
      digitalWrite(pin, LOW);
    } else {
      digitalWrite(pin, HIGH);
    }
    sendResponse(OK);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doEepromRead() {
  int address = Serial.parseInt(); 
  int length = Serial.parseInt(); 
  if (cmdEndOk()) {
    Serial.print(currentCmdId);
    Serial.print(",");
    Serial.print(OK);
    Serial.print(",");
    while (length-- > 0) {
      Serial.print(EEPROM.read(address++));
      if (length > 0) Serial.print(",");
    }
    Serial.println();
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doEepromWrite() {
  int address = Serial.parseInt(); 
  int length = Serial.parseInt(); 

// TODO: How to programatically find EEPROM size?
//  if ((address+length) > EEPROM_SIZE) {
//     while (int c = Serial.read() != '\n');
//     sendResponse(EEPROM_OVERFLOW_ERROR);
//  } else {
    
     while (length-- > 0) {
        EEPROM.write(address++, (byte)Serial.parseInt());
     }

     if (cmdEndOk()) {
        sendResponse(OK);
     } else {
        sendResponse(ARGS_ERROR);
     } 
//  }
}  

void Liberty::doEepromReadString() {
  int address = Serial.parseInt(); 
  if (cmdEndOk()) {
       Serial.print(currentCmdId);
       Serial.print(",");
       Serial.print(OK);
       Serial.print(",");
       // TODO: reduce number of reads
       while (EEPROM.read(address) != 0) {
          Serial.print(EEPROM.read(address++));
          if (EEPROM.read(address) != 0) Serial.print(",");
       }
       Serial.println();
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doSramRead() {
  int address = Serial.parseInt(); 
  int length = Serial.parseInt(); 
  if (cmdEndOk()) {
    if (sramBytes == NULL) {
       sendResponse(NO_SRAM_ERROR);
    }
    if ((address+length) > sramSize) {
       sendResponse(SRAM_OVERFLOW_ERROR);
    }

    Serial.print(currentCmdId);
    Serial.print(",");
    Serial.print(OK);
    Serial.print(",");
    while (length-- > 0) {
      Serial.print(sramBytes[address++]);
      if (length > 0) Serial.print(",");
    }
    Serial.println();

  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doSramWrite() {
  int address = Serial.parseInt(); 
  int length = Serial.parseInt(); 

  if (sramBytes == NULL) {
     while (int c = Serial.read() != '\n');
     sendResponse(NO_SRAM_ERROR);
  } else if ((address+length) > sramSize) {
     while (int c = Serial.read() != '\n');
     sendResponse(SRAM_OVERFLOW_ERROR);
  } else {
    
     while (length-- > 0) {
        sramBytes[address++] = Serial.parseInt();
     }

     if (cmdEndOk()) {
        sendResponse(OK);
     } else {
        sendResponse(ARGS_ERROR);
     } 
  }
}  

void Liberty::doSramReadString() {
  int address = Serial.parseInt(); 
  if (cmdEndOk()) {
    if (sramBytes == NULL) {
       sendResponse(NO_SRAM_ERROR);
    } else {
       Serial.print(currentCmdId);
       Serial.print(",");
       Serial.print(OK);
       Serial.print(",");
       while (sramBytes[address] != 0) {
          Serial.print(sramBytes[address++]);
          if (sramBytes[address] != 0) Serial.print(",");
       }
       Serial.println();
    }
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::doInvoke() {

   int arg1, arg2; 
   int args = Serial.parseInt();
   if (args>=1) {
     arg1 = Serial.parseInt();
   }
   if (args>=2) {
     arg2 = Serial.parseInt();
   }

   int i = findInvoker();

   if (i == -1) {
      sendResponse(FUNCTION_NOT_FOUND);
   } else if (i == -2) {
      sendResponse(ARGS_ERROR);
   } else if (args != fargs[i]) {
      sendResponse(NARGS_ERROR);
   } else {
      if (args == 0) {
         sendResponseValue(invokerFs[i]());
      } else if (args == 1) {
         sendResponseValue(((fx1)invokerFs[i])(arg1));
      } else if (args == 2) {
         sendResponseValue(((fx2)invokerFs[i])(arg1, arg2));
      } else {
         sendResponse(NARGS_ERROR);
      }
   }
}

int Liberty::findInvoker() {

  int nameLength = Serial.parseInt(); 

  boolean matched[MAX_INVOKERS];
  for (int j=0; j<MAX_INVOKERS; j++) {
    matched[j] = true;
  }

  for (int i=0; i<nameLength; i++) {
    int c = readWithWait();
    for (int j=0; j<MAX_INVOKERS; j++) {
      if (matched[j]) {
         matched[j] = (c == invokerNames[j][i]);
      }
    }
  }

  if (cmdEndOk()) {
    for (int j=0; j<MAX_INVOKERS; j++) {
      if (matched[j]) {
        return j;
      }
    }

    return -1; // FUNCTION_NOT_FOUND

  } else {
    return -2; // ARGS_ERROR
  } 
}

void Liberty::sram(byte *bytes, int size) {
   sramBytes = bytes;
   sramSize = size;
}

void Liberty::invocable(char *name, int (*f)()) {
   invokerNames[invokers] = name;
   fargs[invokers] = 0;
   invokerFs[invokers++] = f;
}

void Liberty::invocable(char *name, int (*f)(int)) {
   invokerNames[invokers] = name;
   fargs[invokers] = 1;
   invokerFs[invokers++] = (fx0)f;
}

void Liberty::invocable(char *name, int (*f)(int, int)) {
   invokerNames[invokers] = name;
   fargs[invokers] = 2;
   invokerFs[invokers++] = (fx0)f;
}

void Liberty::notify(char* name, int value) {
       Serial.print(NOTIFICATION_EVENT);
       Serial.print(",");
       Serial.print(name);
       Serial.print(",");
       Serial.print(arduinoName);
       Serial.print(",");
       Serial.println(value);
}

void Liberty::doNoop() {
  if (cmdEndOk()) {
    sendResponse(OK);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}  

void Liberty::log(char* msg) {
    Serial.print(LOG);
    Serial.print(",");
    Serial.println(msg);
}  

void Liberty::log(String s) {
    Serial.print(LOG);
    Serial.print(",");
    Serial.println(s);
}  

void Liberty::doCallback() {
  int callbackId = Serial.parseInt(); 
  int type = Serial.parseInt(); 
  int value = Serial.parseInt();
  int comparitor = Serial.parseInt();
  int pin; 
  if (type == 2) {
    pin = findInvoker();
    if (fargs[pin] != 0) {
       pin = -3;
    }
  } else {
    pin = Serial.parseInt(); 
    if (!cmdEndOk()) {
       pin = -2;
    }
  }

  if (pin == -1) {
     sendResponse(FUNCTION_NOT_FOUND);
  } else if (pin == -2) {
     sendResponse(ARGS_ERROR);
  } else if (pin == -3) {
     sendResponse(CBFUNCTION_ARGS_ERROR);
  } else {
     if (currentCallbacks == MAX_CALLBACKS) {
        sendResponse(TOO_MANY_CALLBACKS_ERROR);
     } else {
        callback x = { callbackId, type, pin, comparitor, value}; 
        callbacks[currentCallbacks++] = x; 
        sendResponse(OK);
     }
  }
}

void Liberty::doClearCallbacks() {
  if (cmdEndOk()) {
    currentCallbacks = 0;
    sendResponse(OK);
  } else {
    sendResponse(ARGS_ERROR);
  } 
}

void Liberty::checkCallbacks() {

   for (int i=0; i<currentCallbacks; i++) {

     int cx;
     if (callbacks[i].callbackType == 0) {
        cx = digitalRead(callbacks[i].pin);
     } else if (callbacks[i].callbackType == 1) {
        cx = analogRead(callbacks[i].pin);
     } else { // function
        cx = invokerFs[callbacks[i].pin]();
     }

     boolean triggered = false;
     boolean reset = false;

     if (callbacks[i].comparitor == 0) { // equals
       if (cx == callbacks[i].triggerValue) {
         if (callbacks[i].lastFiredValue != callbacks[i].triggerValue) triggered = true;
       } else {
         if (callbacks[i].lastFiredValue == callbacks[i].triggerValue) reset = true;
       }
     } else if (callbacks[i].comparitor == -1) { // lessThan
       if (cx < callbacks[i].triggerValue) {
         if (callbacks[i].lastFiredValue >= callbacks[i].triggerValue) triggered = true;
       } else {
         if (callbacks[i].lastFiredValue < callbacks[i].triggerValue) reset = true;
       }
     } else if (callbacks[i].comparitor == 1) { // greaterThan
       if (cx > callbacks[i].triggerValue) {
         if (callbacks[i].lastFiredValue <= callbacks[i].triggerValue) triggered = true;
       } else {
         if (callbacks[i].lastFiredValue > callbacks[i].triggerValue) reset = true;
       }
     } else if (callbacks[i].comparitor == 2) { // changedBy
       if (cx > (callbacks[i].lastFiredValue + callbacks[i].triggerValue)) {
         triggered = true;
       } else if (cx < (callbacks[i].lastFiredValue - callbacks[i].triggerValue)) {
         reset = true;
       }
     }

     // use abs of change greater than one to avoid jitter around the triggerValue
     if ((triggered || reset) && (callbacks[i].callbackType == 0 || abs(callbacks[i].lastFiredValue - cx) > 1)) {

       callbacks[i].lastFiredValue = cx;
     
       int cmd;
       if (triggered) {
          cmd = CALLBACK_TRIGGERED;
       } else {
          cmd = CALLBACK_RESET;
       }
       Serial.print(cmd);
       Serial.print(",");
       Serial.print(callbacks[i].callbackId);
       Serial.print(",");
       Serial.println(cx);

     }    
   }
}

void Liberty::doVersion() {
  if (cmdEndOk()) {
    Serial.print(currentCmdId);
    Serial.print(",");
    Serial.print(OK);
    Serial.print(",");
    Serial.print(VERSION);
    if (arduinoName == NULL) {
       Serial.println();
    } else { 
       Serial.print(",");
       Serial.println(arduinoName);
    }
  } else {
    sendResponse(ARGS_ERROR);
  } 
}

void Liberty::sendResponse(int response) {
    Serial.print(currentCmdId);
    Serial.print(",");
    Serial.println(response);
}

void Liberty::sendResponseValue(int value) {
    Serial.print(currentCmdId);
    Serial.print(",");
    Serial.print(OK);
    Serial.print(",");
    Serial.println(value);
}

boolean Liberty::cmdEndOk() {
  return (readWithWait() == '\n');
}

int Liberty::readWithWait() {
  long startTime = millis();

  do {
    int c = Serial.read();
    if (c >= 0) return c;
  } while(millis() - startTime < readTimeout); // try for half a second

  // timeout
  return -1; 
}

void Liberty::setReadTimeout(int t) {
   readTimeout = t;
}
// EOF
