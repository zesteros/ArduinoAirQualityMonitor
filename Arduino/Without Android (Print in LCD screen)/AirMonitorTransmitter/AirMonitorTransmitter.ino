#include <DHT.h>
#include <Adafruit_BMP085.h>
#include <MQ135.h>//Biblioteca de sensor de aire
#include <VirtualWire.h>
#define DHTPIN 4
#define DHTTYPE DHT11

Adafruit_BMP085 bmp;
MQ135 mqSensor(A0);
DHT dht(DHTPIN, DHTTYPE);

float sensorValue[13];

void setup() {
  vw_set_tx_pin(3);//pin of rf
  vw_setup(2000);//start rf
  Serial.begin(9600);
  delay(100);
  bmp.begin();//start barometer
  dht.begin();//start humidity
}

void loop() {
  readSensors();
  concatMessage();
}
void readSensors() {
  sensorValue[0] = dht.readHumidity();//humidity from dht11 sensor
  sensorValue[1] = (bmp.readTemperature() + dht.readTemperature()) / 2; //temperature °c of 2 sensor for more accurate
  sensorValue[2] = bmp.readPressure() / 100; //pressure hPa
  sensorValue[3] = bmp.readAltitude(); //altitude
  sensorValue[4] = (bmp.readSealevelPressure() - 20137) / 100; //return the equivalent pressure (hPa) at sea level
  sensorValue[5] = analogRead(A0);//air sensor voltage
  sensorValue[6] = digitalRead(2);//return 1 if ppm is low, and 0 when is a lot
  sensorValue[7] = mqSensor.getCOPPM();//co ppm
  sensorValue[8] = mqSensor.getCO2PPM();//co2 ppm
  sensorValue[9] = mqSensor.getEthanolPPM();//ethanol ppm
  sensorValue[10] = mqSensor.getNH4PPM(); //NH4 ppm
  sensorValue[11] = mqSensor.getToluenePPM(); //toluene ppm
  sensorValue[12] = mqSensor.getAcetonePPM(); //acetone ppm
}

void concatMessage() {//WE MADE 3 PARTS OF FULL MESSAGE BECAUSE THE RF ONLY SUPPORT 32 CHARS
  char completeReading1[32];
  char completeReading2[32];
  char completeReading3[32];
  char completeReading4[32];
  char value[20];
  byte k;

  vw_send((byte *)"&", 1);
  vw_wait_tx();//SENDING FIRST 4
  delay(100);
  
  k = 0;
  for (byte i = 0; i < 3; i++) {//START CYCLE FOR EACH VALUE
    completeReading1[k] = '+';
    k++;
    dtostrf(sensorValue[i], 5, 2, value);//CONVERT FLOAT VALUE TO ARRAY MSG CHAR
    for (byte j = 0; j < strlen(value); j++) {//STARTING TO ADD EACH VALUE TO COMPLETE ARRAY
      completeReading1[k] = value[j];//ADD THE VALUE TO THE POSITION K
      k++;
    }
  }

  completeReading1[k] = '+';//FOR END THIRD 5 SENSORS

  k = 0; 
  for (byte i = 3; i < 6; i++) {//START CYCLE FOR EACH VALUE
    completeReading2[k] = '-';
    k++;
    dtostrf(sensorValue[i], 5, 2, value);//CONVERT FLOAT VALUE TO ARRAY MSG CHAR
    for (byte j = 0; j < strlen(value); j++) {//STARTING TO ADD EACH VALUE TO COMPLETE ARRAY
      completeReading2[k] = value[j];//ADD THE VALUE TO THE POSITION K
      k++;
    }
  }
  completeReading2[k] = '-';//FOR END THIRD 5 SENSORS
  
  k = 0;
  for (byte i = 6; i < 9; i++) {//START CYCLE FOR EACH VALUE
    completeReading3[k] = '*';
    k++;
    dtostrf(sensorValue[i], 5, 2, value);//CONVERT FLOAT VALUE TO ARRAY MSG CHAR
    for (byte j = 0; j < strlen(value); j++) {//STARTING TO ADD EACH VALUE TO COMPLETE ARRAY
      completeReading3[k] = value[j];//ADD THE VALUE TO THE POSITION K
      k++;
    }
  }
  completeReading3[k] = '*';//FOR END THIRD 5 SENSORS
  
  k = 0;
  for (byte i = 9; i < 13; i++) {//START CYCLE FOR EACH VALUE
    completeReading4[k] = '#';
    k++;
    dtostrf(sensorValue[i], 5, 2, value);//CONVERT FLOAT VALUE TO ARRAY MSG CHAR
    for (byte j = 0; j < strlen(value); j++) {//STARTING TO ADD EACH VALUE TO COMPLETE ARRAY
      completeReading4[k] = value[j];//ADD THE VALUE TO THE POSITION K
      k++;
    }
  }
  completeReading4[k] = '#';//FOR END THIRD 5 SENSORS
  
  vw_send((byte *)completeReading1, strlen(completeReading1));
  vw_wait_tx();//SENDING FIRST 4
  delay(100);

  vw_send((byte *)completeReading2, strlen(completeReading2));
  vw_wait_tx();//SENDING SECOND 4
  delay(100);

  vw_send((byte *)completeReading3, strlen(completeReading3));
  vw_wait_tx();//SENDING FIRST 4
  delay(100);

  vw_send((byte *)completeReading4, strlen(completeReading4));
  vw_wait_tx();//SENDING FIRST 4
  delay(100);

  
  vw_send((byte *)"!", 1);
  vw_wait_tx();//SENDING FIRST 4
  delay(100);

  for (byte i = 0; i < strlen(completeReading1); i++) {
    Serial.print(completeReading1[i]);
  }
  Serial.println();
  for (byte i = 0; i < strlen(completeReading2); i++) {
    Serial.print(completeReading2[i]);
  }
  Serial.println();
  for (byte i = 0; i < strlen(completeReading3); i++) {
    Serial.print(completeReading3[i]);
  }
  Serial.println();
}
