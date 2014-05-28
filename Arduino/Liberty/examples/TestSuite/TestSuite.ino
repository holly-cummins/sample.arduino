/*
  TestSuite sketch

  Use this sketch when running the Liberty Arduino Feature test suite. 

 */
#include <EEPROM.h>
#include <Liberty.h>

Liberty liberty("TestNode");

void setup() {
   liberty.invocable("foo", &foo);
   liberty.invocable("bar", &bar);
   liberty.invocable("qaz", &qaz);
   liberty.invocable("times", &times);
   liberty.begin();
}
void loop() {
  liberty.update();
}
int foo() {
 return liberty.sramRead(0);
}
int bar() {
 return 42;
}
int qaz(int x) {
 return x * 4;
}
int times(int x, int y) {
 return x * y;
}