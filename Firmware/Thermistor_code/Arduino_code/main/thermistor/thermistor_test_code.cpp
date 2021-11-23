#include "Arduino.h"

void thermistor_init() {

  // put your setup code here, to run once:
  Serial.begin(9600);
}

void thermistor_repeat() {

  // put your main code here, to run repeatedly:
int thermistor_value;
thermistor_value  = analogRead(26);
thermistor_value = thermistor_value/23.17;
Serial.print(thermistor_value);
Serial.print("F");
Serial.print("\n");
delay(1000);
}
