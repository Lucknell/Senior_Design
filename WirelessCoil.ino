#include <TimerOne.h>

#define ledPin 10
#define bluetoothPin 5
#define echoPin 7
#define trigPin 6
#define redLED 13
#define yellowLED 12
#define greenLED 11
double distance, duration;
char state = 'e';
int count = 0;
bool flag = false;
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
}
void yellowLight()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, HIGH);
  digitalWrite(greenLED, LOW);
}
void greenLight()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, LOW);
  digitalWrite(greenLED, HIGH);
}
void lightsOut()
{
  digitalWrite(redLED, LOW);
  digitalWrite(yellowLED, LOW);
  digitalWrite(greenLED, LOW);
}

void setup() {
  Serial.begin(9600); // Default communication rate of the Bluetooth module
  Timer1.initialize(250000);         // initialize timer1, and set a 1/4 second period
  Timer1.attachInterrupt(isr); // attaches isrTimer() as a timer overflow interrupt
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
  for (int i = 0; i < 6; i++)
  {
    pinMode(outPins[i], OUTPUT);
  }
  digitalWrite(bluetoothPin, HIGH);
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
    state = Serial.read(); // Reads the data from the serial port
  }
  if (flag) {
    flag = false;
    digitalWrite(trigPin, LOW); // Clears the trigPin
    delayMicroseconds(2);
    // Sets the trigPin on HIGH state for 10 micro seconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    duration = pulseIn(echoPin, HIGH);
    // Calculating the distance
    distance = duration * 340 / 2000000;
  }
  Serial.println(state);
  Serial.print("\n\nDistance:");
  Serial.println(distance);
  // Controlling the LED
  if (distance >= 3.01)
    digitalWrite(bluetoothPin, LOW);
  //delay(10000);}
  if (distance < 2)
    digitalWrite(bluetoothPin, HIGH);

  if (state == 'h') {
    digitalWrite(ledPin, HIGH); // LED ON
    state = 0;
  }
  else if (state == 'e') {
    digitalWrite(ledPin, LOW); // LED ON
    state = 0;
  }
  if (count % 20 == 0 && count != 0)
  {
    redLight();
  }
  else if (count % 41 == 0 && count != 0)
  {
    yellowLight();
  }
  else if (count % 62 == 0 && count !=0){
    greenLight();
  }
}
