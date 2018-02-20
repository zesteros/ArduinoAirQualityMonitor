#include <DHT.h>//import the library for humidity/temp sensor
#include <Adafruit_BMP085.h>//library for pressure sensor
#include <MQ135.h>//Air sensor library
#define DHTPIN 4//digital pin for dht sensor
#define DHTTYPE DHT11//dht sensor type
#include <SoftwareSerial.h>//Serial com with bt

/*
	February 2018 
	Sketch for read pressure, gas, and temperature sensors and 
	send their values by bluetooth.
	The bmp and the mq135 is analog
	

*/

int bluetoothTx = 6;//pin for transmission bt
int bluetoothRx = 5;//pin for recieve bt
/*Objects*/
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
Adafruit_BMP085 bmp;//pressure sensor (bmp 180)
MQ135 mqSensor(A0);//mqsensor
DHT dht(DHTPIN, DHTTYPE);//temperature sensor

double sensorValue[7];//array for store values

void setup() {
  bluetooth.begin(9600);//start bluetooth serial com
  bmp.begin();//start barometer
  dht.begin();//start humidity
  Serial.begin(9600); // Initialize serial port to send and receive at 9600 baud

}

void loop() {
  analogWrite(9, 0);//turn off led in d9 pin
  readSensors();//read sensors
  sendAndroidValues();//send to android
}

/*Sensors reading to array*/
void readSensors() {
  sensorValue[0] = dht.readHumidity();//humidity from dht11 sensor
  sensorValue[1] = (bmp.readTemperature() + dht.readTemperature()) / 2; //temperature °c of 2 sensor for more accurate
  sensorValue[2] = bmp.readPressure(); //pressure hPa
  sensorValue[3] = bmp.readAltitude(); //altitude
  sensorValue[4] = bmp.readSealevelPressure(); //return the equivalent pressure (hPa) at sea level
  sensorValue[5] = analogRead(A0);//air sensor voltage
  sensorValue[6] = digitalRead(2);//return 1 if ppm is low, and 0 when is a lot
  /*float resistance = mqSensor.getResistance();//resistance
  sensorValue[7] = mqSensor.getCO(resistance);//co ppm
  sensorValue[8] = mqSensor.getCO2(resistance);//co2 ppm
  sensorValue[9] = mqSensor.getEthanol(resistance);//ethanol ppm
  sensorValue[10] = mqSensor.getNH4(resistance); //NH4 ppm
  sensorValue[11] = mqSensor.getToluene(resistance); //toluene ppm
  sensorValue[12] = mqSensor.getAcetone(resistance); //acetone ppm*/
}
/*Parser of data*/
void sendAndroidValues() {
  Serial.print('#');
  bluetooth.print('#');//indicates start of transmission
  for (byte k = 0; k < 7; k++) {//send every value
    bluetooth.print(sensorValue[k]);
    bluetooth.print('+');//indicates the space between values
    Serial.print(sensorValue[k]);
    Serial.print('+');//indicates the space between values
  }
  Serial.print('~');//indicates the end of read
  Serial.println();
  bluetooth.print('~');//indicates the end of read
  analogWrite(9, 130);//turn on led
  delay(100);//debug tx
}
