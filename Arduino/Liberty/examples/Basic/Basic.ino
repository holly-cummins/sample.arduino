#include <EEPROM.h>
#include <Liberty.h>

Liberty liberty;

void setup()
{
   liberty.begin();
}

void loop()
{
  liberty.update();
}

