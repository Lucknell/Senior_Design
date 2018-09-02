int state = 20;
   int count = 0;
   void setup() {
      Serial.begin(9600); // Default communication rate of the Bluetooth module
    }
    void loop() {
     if(Serial.available() > 0){ // Checks whether data is comming from the serial port
        state = Serial.read(); // Reads the data from the serial port
     }
     // Controlling the servo motor
     //Serial.println(state);
     if (count == 3){
      Serial.write('h');
      delay(100);
      count=0;
      }
      else {
      Serial.write('e');
      delay(100);        
        }
        count++;
    }
