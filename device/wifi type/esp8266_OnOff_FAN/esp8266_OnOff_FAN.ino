/*
 J5
 ON/OFF TYPE FAN version
 ESP8266 MQTT
 J5 Protocol v2.0

 device name : FAN
 
 It connects to an MQTT server then:
  - publishes "{"DEVICE":"FAN","STATUS":"OFF/ULOW/LOW/MIDDLE/HIGH","COMMANDON":"ONLINE/OFFLINE"}"
    to the topic "STATUS" by JSON type message when status of FAN be started and changed.
  - subscribes to the topic "FAN", controlling GPIOs by appointed messages it receives.

 It will reconnect to the server if the connection is lost using a blocking reconnect function. 


 To install the ESP8266 board, (using Arduino 1.6.4+):
  - Add the following 3rd party board manager under "File -> Preferences -> Additional Boards Manager URLs":
       http://arduino.esp8266.com/stable/package_esp8266com_index.json
  - Open the "Tools -> Board -> Board Manager" and click install for the ESP8266"
  - Select your ESP8266 in "Tools -> Board"

 Reference : https://github.com/knolleary/pubsubclient

*/

#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define STATUS_ULOW 5
#define STATUS_LOW 12
#define STATUS_MIDDLE 13
#define STATUS_HIGH 14
#define STATUS_SWING 16

#define CONTROL_POWER 15
#define CONTROL_SWING 4

#define ONLINE true
#define OFFLINE false

#define _SWINGOFF 0
#define _SWINGON 1
#define _OFF 0
#define _LOW 1
#define _MIDDLE 2
#define _HIGH 3
#define _ULOW 4     // ultra low

void setStatus(String pay);
void getStatus(boolean online);
void pushButton(int cnt);

const char* ssid = "homegateway";
const char* password = "homegateway";
const char* mqtt_server = "172.16.100.62";
//const char* mqtt_server = "192.168.0.15";


WiFiClient espClient;
PubSubClient client(espClient);
char subTopic[] = {"FAN"};
char pubTopic[] = {"STATUS"};
char fanStatus[120] = {0};
boolean commandOn = false;      // true : online, false : offline
boolean changedStatus[2] = {true,true};   // default value
int lastStatus[2] = {_OFF, _SWINGOFF};          // 0: OFF, 1: LOW, 2: MIDDLE, 3: HIGH, 4: ULOW

void setup() {
  pinMode(STATUS_ULOW, INPUT_PULLUP);
  pinMode(STATUS_LOW, INPUT_PULLUP);
  pinMode(STATUS_MIDDLE, INPUT_PULLUP);
  pinMode(STATUS_HIGH, INPUT_PULLUP);
  pinMode(STATUS_SWING, INPUT_PULLUP);

  pinMode(CONTROL_POWER, OUTPUT);
  pinMode(CONTROL_SWING, OUTPUT);

  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
  
  digitalWrite(CONTROL_POWER,LOW);  // output pin reset

}

void setup_wifi() {

  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {

  String payStr = "";
   
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");

  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    payStr += (char)payload[i];
  }
 
  setStatus(payStr);
  Serial.println();
}

void reconnect() {

  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect("FAN")) {
      Serial.println("connected");
      client.subscribe(subTopic);
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void pushButton(int pin, int cnt) {
  for(;cnt>0;cnt--) {
    digitalWrite(pin, HIGH);   // push
    delay(500);
    digitalWrite(pin, LOW);  // button status reset
    delay(500);
  }
}


void setStatus(String pay) {

  if(pay.equals("PUSH")) {
    pushButton(CONTROL_POWER,1);      // once push
    if(lastStatus[0] == _OFF) {      
      delay(500);           // delay for stably reading status
    }
    delay(500);           // delay for stably reading status
  } else if(pay.equals("OFF")) {
    switch(lastStatus[0]) {
      case _LOW:   // LOW to OFF
        pushButton(CONTROL_POWER,1);
      case _MIDDLE:   // MIDDLE to OFF
        pushButton(CONTROL_POWER,1);
      case _HIGH:   // HIGH to OFF
        pushButton(CONTROL_POWER,1);
      case _ULOW:   // ULOW to OFF
        pushButton(CONTROL_POWER,1);
      default:  // OFF
//        lastStatus[0] = _OFF;
        break;
    }
  } else if(pay.equals("LOW")) {
    switch(lastStatus[0]) {
      case _MIDDLE:   // MIDDLE to LOW
        pushButton(CONTROL_POWER,1);
      case _HIGH:   // HIGH to LOW
        pushButton(CONTROL_POWER,1);
      case _ULOW:   // ULOW to LOW
        pushButton(CONTROL_POWER,1);
      case _OFF:   // OFF to LOW
        pushButton(CONTROL_POWER,1);
      default:  // LOW
          delay(1000);           // delay for stably reading status
//        lastStatus[0] = _LOW;
        break;
    }
  } else if(pay.equals("MIDDLE")) {
    switch(lastStatus[0]) {
      case _HIGH:   // HIGH to MIDDLE
        pushButton(CONTROL_POWER,1);
      case _ULOW:   // ULOW to MIDDLE
        pushButton(CONTROL_POWER,1);
      case _OFF:   // OFF to MIDDLE
        pushButton(CONTROL_POWER,1);
      case _LOW:   // LOW to MIDDLE
        pushButton(CONTROL_POWER,1);
      default:  // MIDDLE
//        lastStatus[0] = _MIDDLE;
        break;
    }
  } else if(pay.equals("HIGH")) {
    switch(lastStatus[0]) {
      case _ULOW:   // ULOW to HIGH
        pushButton(CONTROL_POWER,1);
      case _OFF:   // OFF to HIGH
        pushButton(CONTROL_POWER,1);
      case _LOW:   // LOW to HIGH
        pushButton(CONTROL_POWER,1);
      case _MIDDLE:   // OFF to HIGH
        pushButton(CONTROL_POWER,1);
      default:  // HIGH
//        lastStatus[0] = _HIGH;
        break;
    }
  } else if(pay.equals("ULOW")) {
    switch(lastStatus[0]) {
      case _OFF:   // OFF to ULOW
        pushButton(CONTROL_POWER,1);
      case _LOW:   // LOW to ULOW
        pushButton(CONTROL_POWER,1);
      case _MIDDLE:   // MIDDLE to ULOW
        pushButton(CONTROL_POWER,1);
      case _HIGH:   // HIGH to ULOW
        pushButton(CONTROL_POWER,1);
      default:  // ULOW
//        lastStatus[0] = _ULOW;
        break;
    }
  } else if(pay.equals("SWING")) {
      pushButton(CONTROL_SWING,1);
  }
  
  commandOn = ONLINE;    // command on online
  delay(500);           // delay for stably reading status

  getStatus(ONLINE);
}


void getStatus(boolean online) {

  if(!digitalRead(STATUS_ULOW)) {    // ULOW
    if(lastStatus[0] != _ULOW) {
      changedStatus[0] = true;
      lastStatus[0] = _ULOW;
    }
  } else if(!digitalRead(STATUS_LOW)) {   // LOW
    if(lastStatus[0] != _LOW) {
      changedStatus[0] = true;
      lastStatus[0] = _LOW;
    }
  } else if(!digitalRead(STATUS_MIDDLE)) {   // MIDDLE
    if((!online) && (lastStatus[0] == _OFF)) {    // exception LOW
      changedStatus[0] = true;
      lastStatus[0] = _LOW;
      delay(1000);           // delay for stably reading status
    } else if(lastStatus[0] != _MIDDLE) {
      changedStatus[0] = true;
      lastStatus[0] = _MIDDLE;
    }
  } else if(!digitalRead(STATUS_HIGH)){    // HIGH
    if(lastStatus[0] != _HIGH) {
      changedStatus[0] = true;
      lastStatus[0] = _HIGH;
    }
  } else {    // OFF
    if(lastStatus[0] != _OFF) {
      changedStatus[0] = true;
      lastStatus[0] = _OFF;
   }
  }

  if(!digitalRead(STATUS_SWING)) {    // SWING ON
    if(lastStatus[1] != _SWINGON) {
      changedStatus[1] = true;
      lastStatus[1] = _SWINGON;  
    }
  } else {   // SWING OFF
    if(lastStatus[1] != _SWINGOFF) {
      changedStatus[1] = true;
      lastStatus[1] = _SWINGOFF;
    }
  }

  if(changedStatus[0] || changedStatus[1]) {
    if(!online) {
      commandOn = OFFLINE;    // command on offline
    }
    switch(lastStatus[0]) {
      case _OFF:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"OFF");
        break;
      case _LOW:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"LOW");
        break;
      case _MIDDLE:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"MIDDLE");
        break;
      case _HIGH:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"HIGH");
        break;
      case _ULOW:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"ULOW");
        break;
      default:
        strcpy(fanStatus,"{\"DEVICE\":\"FAN\",\"STATUS\":\"?????\"");
        break;
    }
    switch(lastStatus[1]) {
      case _SWINGON:
        strcat(fanStatus,"/SW\"");
        break;
      case _SWINGOFF:
        strcat(fanStatus,"\"");
        break;
      default:
        strcat(fanStatus,"????\"");
        break;
    }
    strcat(fanStatus,commandOn?",\"COMMANDON\":\"ONLINE\"}":",\"COMMANDON\":\"OFFLINE\"}");
    client.publish(pubTopic,fanStatus);
    changedStatus[0] = false;
    changedStatus[1] = false;
  }
}

void loop() {

  for(int i=0;i<20;i++) {         // 20 x 50 msec = 1 sec
    if (!client.connected()) {
      reconnect();
    }
    client.loop();
    delay(50);
  }
  getStatus(OFFLINE);    //  check status every one secondes
}
