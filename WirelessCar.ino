#include <Adafruit_SSD1306.h>
#include <TimerOne.h>
#include <Wire.h>
#include <SPI.h>

#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);
#define bluetoothPin 2
#define statePin 4
#define motorPin 0
#define screenLimit 21

char state = 'g';
int count = 0, pCount = 0, connection = 99;
float shuntvoltage = 0;
float busvoltage = 0;
float current_mA = 0;
float loadvoltage = 0;
float energy = 0;
String test = "";
void isr()
{
  count++;
}
void decrypt(char info)
{

  test = "Connection Status :";
  test += connection;
  test += "\nVoltage :";
  test += loadvoltage;
  test += "V";
  test += "\nCurrent : ";
  test += current_mA;
  test += "A";
  test += "\nmW : " ;
  test += (loadvoltage * current_mA);
  test += "mWh";
  display.clearDisplay();
  display.setTextColor(WHITE);
  display.setTextSize(1);
  display.setCursor(0, 0);
  //display.print("Connection Status :")
  display.println(test);
  //display.println("\nVoltage : \nCurrent : \nmWH : ");
  display.display();
}
void setup() {
  Serial.begin(9600); // Default communication rate of the Bluetooth module
  pinMode(bluetoothPin, OUTPUT);
  digitalWrite(bluetoothPin, HIGH);
  pinMode(statePin, INPUT);
  Timer1.initialize(250000);         // initialize timer1, and set a 1/4 second period
  Timer1.attachInterrupt(isr); // attaches isrTimer() as a timer overflow interrupt
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
}
void loop() {

  int x = analogRead(motorPin); //Check if attached motor is moving
  double voltage = (x / 1023.0) * 5;
  //  Serial.println(voltage);
  if (voltage != 0) //if the motor is moving shut the bluetooth sensor down
    digitalWrite(bluetoothPin, LOW);
  else { //if the motor is not moving turn on the bluetooth sensor
    digitalWrite(bluetoothPin, HIGH);
    //Serial.println(Serial.available());
    if (Serial.available() > 0) { // Checks whether data is comming from the serial port
      {
        state = Serial.read(); // Reads the data from the serial port
        pCount++;
      }
      //Serial.println(state);
      connection = digitalRead(statePin);
      decrypt(state);
      if (count % 40 == 0 && count != 0) {
        Serial.write('Q');
        delay(100);
      }
      else
        Serial.write('F');
    }
  }
}
