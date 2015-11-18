/*
 J5
 ON/OFF TYPE HUMIDIFIER version
 ESP8266 MQTT
 J5 Protocol v2.0

 device name : HUMI
 
 It connects to an MQTT server then:
  - publishes "{"DEVICE":"HUMI","STATUS":"OFF/LOW/MIDDLE/HIGH","COMMANDON":"ONLINE/OFFLINE"}"
    to the topic "STATUS" by JSON type message when status of HUMI be started and changed.
  - subscribes to the topic "HUMI", controlling GPIOs by appointed messages it receives.

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

#define STATUS_PIN_G 4
#define STATUS_PIN_R 5
#define CONTROL_PIN 15
#define ONLINE true
#define OFFLINE false

#define _OFF 0
#define _LOW 1
#define _MIDDLE 2
#define _HIGH 3

void setStatus(String pay);
void getStatus(boolean online);
void pushButton(int cnt);

const char* ssid = "homegateway";
const char* password = "homegateway";
const char* mqtt_server = "172.16.100.62";
//const char* mqtt_server = "192.168.0.15";


WiFiClient espClient;
PubSubClient client(espClient);
char subTopic[] = {"HUMI"};
char pubTopic[] = {"STATUS"};
char humiStatus[100] = {0};
boolean commandOn = false;      // true : online, false : offline
boolean changedStatus = true;   // default value
int lastStatus = _OFF;          // 0: OFF, 1: LOW, 2: MIDDLE, 3: HIGH

void setup() {
  pinMode(STATUS_PIN_G, INPUT);
  pinMode(STATUS_PIN_R, INPUT);
  pinMode(CONTROL_PIN, OUTPUT);  

  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
  
  digitalWrite(CONTROL_PIN,HIGH);  // output pin reset

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
    if (client.connect("HUMI")) {
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

void pushButton(int cnt) {
  for(;cnt>0;cnt--) {
    digitalWrite(CONTROL_PIN, LOW);   // push
    delay(500);
    digitalWrite(CONTROL_PIN, HIGH);  // button status reset
    delay(500);
  }
}


void setStatus(String pay) {

  if(pay.equals("PUSH")) {
    pushButton(1);      // once push
/*    
    if(lastStatus == _OFF) {
      strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"LOW\",\"COMMANDON\":\"ONLINE\"}");
      lastStatus = _LOW;
    } else if(lastStatus == _LOW) {
      strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"MIDDLE\",\"COMMANDON\":\"ONLINE\"}");
      lastStatus = _MIDDLE;
    } else if(lastStatus == _MIDDLE) {
      strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"HIGH\",\"COMMANDON\":\"ONLINE\"}");
      lastStatus = _HIGH;
    } else {    // lastStatus == _HIGH
      strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"OFF\",\"COMMANDON\":\"ONLINE\"}");
      lastStatus = _OFF;
    }
*/
  } else if(pay.equals("OFF")) {
    switch(lastStatus) {
      case _LOW:   // LOW to OFF
        pushButton(1);
      case _MIDDLE:   // MIDDLE to OFF
        pushButton(1);
      case _HIGH:   // HIGH to OFF
        pushButton(1);
      default:  // OFF
//        lastStatus = _OFF;
        break;
    }
  } else if(pay.equals("LOW")) {
    switch(lastStatus) {
      case _MIDDLE:   // MIDDLE to LOW
        pushButton(1);
      case _HIGH:   // HIGH to LOW
        pushButton(1);
      case _OFF:   // OFF to LOW
        pushButton(1);
      default:  // LOW
//        lastStatus = _LOW;
        break;
    }
  } else if(pay.equals("MIDDLE")) {
    switch(lastStatus) {
      case _HIGH:   // HIGH to MIDDLE
        pushButton(1);
      case _OFF:   // OFF to MIDDLE
        pushButton(1);
      case _LOW:   // LOW to MIDDLE
        pushButton(1);
      default:  // MIDDLE
//        lastStatus = _MIDDLE;
        break;
    }
  } else if(pay.equals("HIGH")) {
    switch(lastStatus) {
      case _OFF:   // OFF to HIGH
        pushButton(1);
      case _LOW:   // LOW to HIGH
        pushButton(1);
      case _MIDDLE:   // OFF to HIGH
        pushButton(1);
      default:  // HIGH
//        lastStatus = _HIGH;
        break;
    }
  }
//  client.publish(pubTopic,humiStatus);
  
  commandOn = ONLINE;    // command on online
  delay(50);           // delay for stably reading status

  getStatus(ONLINE);
}


void getStatus(boolean online) {
  
  if(!digitalRead(STATUS_PIN_G) && !digitalRead(STATUS_PIN_R)) {    // OFF
    if(lastStatus != _OFF) {
      changedStatus = true;     
    }
    lastStatus = _OFF;
  } else if(digitalRead(STATUS_PIN_G) && !digitalRead(STATUS_PIN_R)) {   // LOW
    if(lastStatus != _LOW) {
      changedStatus = true;
    }
    lastStatus = _LOW;
  } else if(digitalRead(STATUS_PIN_G) && digitalRead(STATUS_PIN_R)) {   // MIDDLE
    if(lastStatus != _MIDDLE) {
      changedStatus = true;
    }
    lastStatus = _MIDDLE;
  } else {                          // HIGH
    if(lastStatus != _HIGH) {
      changedStatus = true;
    }
    lastStatus = _HIGH;
  } 

  if(changedStatus) {
    if(!online) {
      commandOn = OFFLINE;    // command on offline
    }
    switch(lastStatus) {
      case _OFF:
        strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"OFF\"");
        break;
      case _LOW:
        strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"LOW\"");
        break;
      case _MIDDLE:
        strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"MIDDLE\"");
        break;
      case _HIGH:
        strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"HIGH\"");
        break;
      default:
        strcpy(humiStatus,"{\"DEVICE\":\"HUMI\",\"STATUS\":\"?????\"");
        break;
    }
    strcat(humiStatus,commandOn?",\"COMMANDON\":\"ONLINE\"}":",\"COMMANDON\":\"OFFLINE\"}");
    client.publish(pubTopic,humiStatus);
    changedStatus = false;
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
