/*
 * IRremote: IRsendDemo - demonstrates sending IR codes with IRsend
 * An IR LED must be connected to Arduino PWM pin 3.
 * Version 0.1 July, 2009
 * Copyright 2009 Ken Shirriff
 * http://arcfn.com
 */

#include <IRremote.h>
#include <dht.h>


IRsend irsend;

//DHT
DHT sensor = DHT();

//projecrtor
#define ONOFF  0x40BFB847

//radio
#define RADIO_MODE 0xFF629D
#define RADIO_PLAY_PAUSE 0xFFC23D
#define RADIO_VOLUME_UP 0xFFA857 
#define RADIO_VOLUME_DOWN 0xFFE01F 
#define RADIO_NEXT 0xFF02FD
#define RADIO_PREV 0xFF22DD
#define RADIO_CHANNEL_UP 0xFFE21D
#define RADIO_CHANNEL_DOWN 0xFFA25D


//electric fan
#define FAN_ON 0x143226DB
#define FAN_OFF 0xA32AB931
#define FAN_DIRECTION 0x39D41DC6
#define FAN_FORCE 0x371A3C86

String message = "";

int motionPin = 2;
int val = 0;

class JsonString
{
 public:
   JsonString() {}
   int add(String _key, String _val) {
     _str += "\"" + _key + "\":" + "\"" + _val + "\"";
   }

   int add(String _key, int _val) {
     _str += "\"" + _key + "\":"  + _val;
   }

   int add(String _key, float _val) {
     _str += "\"" + _key + "\":" +  _val;
   }

   int comma() {
     _str += " , ";
   }

   String str() {
     return _str + " } ";
   }

 private:
   String _str = "{ ";
 };


void setup()
{
    Serial.begin(9600);   //시리얼모니터
    sensor.attach(A5);
    pinMode(motionPin , INPUT);
    delay(1000);
  
}

int counter = 0 ;
void loop() {
   JsonString jsonString = JsonString();
    for(int i = 0 ; i< 40 ; i++){
      if(Serial.available()>0){
          int temp = Serial.read();
          
         switch(temp){
           case '0' :
             irsend.sendJVC(ONOFF,32,0);
             break;     
           case '1' :
             irsend.sendJVC(RADIO_PLAY_PAUSE,32,0);
             break;
           case '2' :
             irsend.sendJVC(RADIO_MODE,32,0);
             break;
           case '3' :
             irsend.sendJVC(RADIO_VOLUME_UP,32,0);
             break;
           case '4' :
             irsend.sendJVC(RADIO_VOLUME_DOWN,32,0);
             break;
           case '5' :
             irsend.sendJVC(RADIO_NEXT,32,0);
             break;
           case '6' :
             irsend.sendJVC(RADIO_PREV,32,0);
             break;
           case '7' :
             irsend.sendJVC(RADIO_CHANNEL_UP,32,0);
             break;
           case '8' :
             irsend.sendJVC(RADIO_CHANNEL_DOWN,32,0);
             break;
           case '9' :
             irsend.sendJVC(FAN_ON,32,0);
             break;
           case '10' :
             irsend.sendJVC(FAN_OFF,32,0);
             break;
           case '11' :
             irsend.sendJVC(FAN_DIRECTION,32,0);
             break;
           case '12' :
             irsend.sendJVC(FAN_FORCE,32,0);
             break;
         }
      }
      delay(50);
    }
    message="";
    
    val = digitalRead(motionPin);
    if(val==0){
      jsonString.add("MOTION","OFF");

    }else{
      jsonString.add("MOTION","ON");
    }


      sensor.update();
  
      switch (sensor.getLastError())
      {
          case DHT_ERROR_OK:
             jsonString.comma();

             
             jsonString.add("TEMP",String(sensor.getTemperatureInt()));
             jsonString.comma();
             jsonString.add("HUM",String(sensor.getHumidityInt()));
             Serial.println(jsonString.str());

              break;
          case DHT_ERROR_START_FAILED_1:
  //            Serial.println("Error: start failed (stage 1)");
              break;
          case DHT_ERROR_START_FAILED_2:
  //            Serial.println("Error: start failed (stage 2)");
              break;
          case DHT_ERROR_READ_TIMEOUT:
  //            Serial.println("Error: read timeout");
              break;
          case DHT_ERROR_CHECKSUM_FAILURE:
  //            Serial.println("Error: checksum error");
              break;
      }
     
    
    if(counter>=30){
      counter = 0;
    }else{
      counter++;
    }
//  }
//  if (temp =='PROJECTOR:ONOFF') {
//  Serial.println(temp);    
//      irsend.sendJVC(ONOFF,32,0); // Sony TV power code
//
//  }else if(temp == '2'){
// Serial.println(temp);
//
//    irsend.sendJVC(RADIO_PLAY_PAUSE,32,0); // 
//  }else if(temp == '3'){
//    Serial.println(temp);
//     irsend.sendJVC(RADIO_VOLUME_UP,32,0); // 
//  }else if(temp == '4'){
//    Serial.println(temp);
//     irsend.sendJVC(RADIO_VOLUME_DOWN,32,0); // 
//  }
}
