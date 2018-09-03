#define bluetoothPin 2
#define motorPin 0
int state = 20;
int count = 0;
void setup() {
  Serial.begin(9600); // Default communication rate of the Bluetooth module
  pinMode(bluetoothPin, OUTPUT);
  digitalWrite(bluetoothPin, HIGH);
}
void loop() {
  if (Serial.available() > 0) { // Checks whether data is comming from the serial port
    state = Serial.read(); // Reads the data from the serial port
  }
  int x = analogRead(motorPin); //Check if attached motor is moving
  double voltage = (x / 1023.0) * 5;
  Serial.println(voltage);
  if (voltage != 0) //if the motor is moving shut the bluetooth sensor down
    digitalWrite(bluetoothPin, LOW);
  else { //if the motor is not moving turn on the bluetooth sensor
    digitalWrite(bluetoothPin, HIGH);
    if (count == 3) {
      Serial.write('h');
      delay(100);
      count = 0;
    }
    else {
      Serial.write('e');
      delay(100);
    }
    count++;
  }
}
