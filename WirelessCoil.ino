#include <TimerOne.h>

#define ledPin 10
#define bluetoothPin 5
#define statePin 4
#define echoPin 7
#define trigPin 6
#define redLED 13
#define yellowLED 12
#define greenLED 11
#define greenLimit 15 //define the limit for the green light in seconds 
#define yellowLimit 25//define the limit for the yellow light in seconds 
#define redLimit 50//define the limit for the red light in seconds 
#define nearLimit 2
#define farLimit 3.01

double distance, duration;
char command = 'e';
byte state = LOW;
long count = 0;
long tempcount = 0;
bool flag = false, redFlag = false;
const int outPins[6] = {ledPin, bluetoothPin, trigPin, redLED, yellowLED, greenLED };
void isr()
{
  count++;
  flag = true;
}
void redLight()
{
  digitalWrite(redLED, HIGH);
  digitalWrite(yellowLED, LOW);
  digitalWrite(greenLED, LOW);
  redFlag = true;
}
void yellowLight()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, HIGH);
  digitalWrite(greenLED, LOW);
  redFlag = false;
}
void greenLight()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, LOW);
  digitalWrite(greenLED, HIGH);
  redFlag = false;
}
void lightsOut()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, LOW);
  digitalWrite(greenLED, LOW);
}

void trafficSignal()
{
  if (count % 4 == 0 && count != 0)
    tempcount++;
  if (tempcount > 0 && tempcount < greenLimit)
    greenLight();
  else if (tempcount >= greenLimit && tempcount < yellowLimit )
    yellowLight();
  else if (tempcount >= yellowLimit && tempcount < redLimit)
    redLight();
  else
    tempcount = 0;

}
void activateCoil()
{
   digitalWrite(ledPin, HIGH);
}
void disactivateCoil()
{
 digitalWrite(ledPin, LOW);
}

void setup() {
  Serial.begin(9600); // Default communication rate of the Bluetooth module
  Timer1.initialize(250000);         // initialize timer1, and set a 1/4 second period
  Timer1.attachInterrupt(isr); // attaches isrTimer() as a timer overflow interrupt
  pinMode(statePin, INPUT);
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
  digitalWrite(bluetoothPin,HIGH);
for (int i = 0; i < 6; i++)
  {
    pinMode(outPins[i], OUTPUT);
  }
  redLight();
  delay(500);
  yellowLight();
  delay(500);
  greenLight();
  delay(500);
  lightsOut();
  digitalWrite(ledPin, HIGH);
  delay(500);
  digitalWrite(ledPin, LOW);
}
void loop() {
  if (Serial.available() > 0) { // Checks whether data is comming from the serial port
    command = Serial.read(); // Reads the data from the serial port
  }
  if (flag) {
    flag = false;
    digitalWrite(trigPin, LOW); // Clears the trigPin
    delayMicroseconds(2);
    // Sets the trigPin on HIGH command for 10 micro seconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    duration = pulseIn(echoPin, HIGH);
    // Calculating the distance
    distance = duration * 340 / 2000000;
    trafficSignal();
    
    }
  if (distance < nearLimit && redFlag) {
      if (command == 'F') {
        activateCoil();
        command = 0;
      }
    }
    else
      disactivateCoil();
    if (command == 'Q') {
      disactivateCoil();
      command = 0;
    }
}
