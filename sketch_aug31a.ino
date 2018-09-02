   #define ledPin 9
   #define bluePin 2
   #define echoPin 12
   #define trigPin 11
   double distance, duration;
   char state = 'e';
   int count = 0;
   void setup() {
      Serial.begin(9600); // Default communication rate of the Bluetooth module
      pinMode(echoPin, INPUT); // Sets the echoPin as an Input
      pinMode(trigPin,OUTPUT);
      pinMode(ledPin,OUTPUT);
      pinMode(bluePin,OUTPUT);
      digitalWrite(bluePin, HIGH);
    }
    void loop() {
     if(Serial.available() > 0){ // Checks whether data is comming from the serial port
        state = Serial.read(); // Reads the data from the serial port
     }
     digitalWrite(trigPin, LOW); // Clears the trigPin
    delayMicroseconds(2);
    // Sets the trigPin on HIGH state for 10 micro seconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    duration = pulseIn(echoPin, HIGH);
    // Calculating the distance
distance = duration * 340 / 2000000;
     Serial.println(state);
     Serial.print("\n\nDistance:");
     Serial.println(distance);
      // Controlling the LED
 if (distance >= 3.01)
 digitalWrite(bluePin, LOW);
 //delay(10000);}
 if (distance < 2)
 digitalWrite(bluePin, HIGH);

 if (state == 'h') {
  digitalWrite(ledPin, HIGH); // LED ON
  state = 0;
 }
 else if (state == 'e') {
  digitalWrite(ledPin, LOW); // LED ON
  state = 0;
 }
 count++;
    }
