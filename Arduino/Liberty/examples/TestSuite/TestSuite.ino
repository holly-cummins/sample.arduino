/*
  TestSuite sketch

  Use this sketch when running the Liberty Arduino Feature test suite. 

 */
#include <EEPROM.h>
#include <Liberty.h>

Liberty liberty("TestNode");

boolean ns = false;
int nc = 0;
unsigned long ln;
byte bs[50];

void setup() {
   liberty.sram(bs, 50);
   liberty.invocable("foo", &foo);
   liberty.invocable("bar", &bar);
   liberty.invocable("qaz", &qaz);
   liberty.invocable("times", &times);
   liberty.invocable("start", &nstart);
   liberty.invocable("stop", &nstop);
   liberty.begin();
}
void loop() {
  liberty.update();
  if (ns) {
    unsigned long x = millis();
    if (x - ln > 200) {
       ln = x;
       nc++;
       liberty.notify("testN1", nc); 
    }
  }  
}
int foo() {
 return bs[0];
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

int nstart() {
  ns = true;
  nc = 0;
  ln = millis();
}

int nstop() {
  ns = false;
  return nc;
}