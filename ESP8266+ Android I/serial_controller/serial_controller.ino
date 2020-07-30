#define LED D5

void setup() {
  Serial.begin(115200);
  pinMode(LED, OUTPUT);
}

void loop() {
  if(Serial.available()) {
    char command = Serial.read();
    if (command == '1') {
      digitalWrite(LED, HIGH);
      Serial.println("LED menyala");
    }

    if (command == '0') {
      digitalWrite(LED, LOW);
      Serial.println("LED dimatikan");
    }
  }
}
