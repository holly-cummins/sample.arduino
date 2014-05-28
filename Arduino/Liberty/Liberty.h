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

#ifndef Liberty_h
#define Liberty_h

#include "Arduino.h"

class Liberty
{

  public:
    Liberty();
    Liberty(char *name);
    Liberty(unsigned long speed);
    Liberty(char *name, unsigned long speed);
    void begin();
    void update();
    byte sramRead(int address);
    void sramWrite(int address, byte value);
    void invocable(char *name, int (*f)());
    void invocable(char *name, int (*f)(int x));
    void invocable(char *name, int (*f)(int x, int y));
    int call(char* name, int x);
    void log(char* msg);
    void log(String s);
    void setReadTimeout(int t);

  private:
    void doPinMode();
    void doAnalogRead();
    void doAnalogWrite();
    void doDigitalRead();
    void doDigitalWrite();
    void doEepromRead();
    void doEepromWrite();
    void doSramRead();
    void doSramWrite();
    void doSramReadString();
    void doSramWriteString();
    void doInvoke();
    int findInvoker();
    void doCallback();
    void doDigitalCallback();
    void doAnalogCallback();
    void doFunctionCallback();
    void doClearCallbacks();
    void checkCallbacks();
    void checkCallback();
    void readCallback();
    void doNoop();
    void sendResponse(int response);
    void sendResponseValue(int value);
    boolean cmdEndOk();
    int readWithWait();
    void doVersion();
};

#endif

