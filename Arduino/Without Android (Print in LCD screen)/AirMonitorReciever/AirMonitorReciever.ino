#include <SoftwareSerial.h>//Biblioteca de conexión bluetooth
#include <VirtualWire.h>

int bluetoothTx = 2;
int bluetoothRx = 3;

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup() {
  bluetooth.begin(9600);
  Serial.begin(9600);
  vw_set_rx_pin(11);
  vw_setup(2000);
  vw_rx_start();
}

void loop() {
  byte buf[VW_MAX_MESSAGE_LEN];
  byte buflen = VW_MAX_MESSAGE_LEN;
  if (vw_get_message(buf, &buflen)) {
    for (int i = 0; i < buflen; i++) {
      Serial.print((char)buf[i]);
      bluetooth.print((char)buf[i]);
    }
    Serial.println();
  }
}
