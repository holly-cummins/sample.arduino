#include <EEPROM.h>
#include <Liberty.h>

Liberty liberty;

void setup() {
   liberty.invocable("foo", &foo);
   liberty.begin();
}

void loop() {
  liberty.update();
}

int foo() {
 return 42;
}



