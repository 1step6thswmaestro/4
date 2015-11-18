/*
 J5 ON/OFF TYPE LED version
 ESP8266 MQTT
 J5 Protocol v2.0

 device name & num : LED1
 
 It connects to an MQTT server then:
  - publishes "{"DEVICE":"LED1","STATUS":"ON/OFF","COMMANDON":"ONLINE/OFFLINE"}"
    to the topic "STATUS" by JSON type message when status of LED1 be started and changed.
  - subscribes to the topic "LED1", controlling GPIOs by appointed messages it receives.

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

#define STATUS_PIN 0
#define CONTROL_PIN 2
#define ONLINE true
#define OFFLINE false
#define ON true
#define OFF false

void setStatus(String pay);
void getStatus(boolean online);

const char* ssid = "homegateway";
const char* password = "homegateway";
const char* mqtt_server = "172.16.100.62";
//const char* mqtt_server = "192.168.0.15";


WiFiClient espClient;
PubSubClient client(espClient);
char subTopic[] = {"LED1"};
char pubTopic[] = {"STATUS"};
char ledStatus[100] = {0};
boolean commandOn = false;   // true : online, false : offline
boolean changedStatus = true;   // default value
boolean lastStatus = OFF;   // true : ON, false : OFF


void setup() {
  pinMode(STATUS_PIN, INPUT);
  pinMode(CONTROL_PIN, OUTPUT);  

  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);

  digitalWrite(CONTROL_PIN,LOW);  // output pin reset
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
    if (client.connect("LED1")) {
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

void setStatus(String pay) {
  if(pay.equals("ON")) {
      digitalWrite(CONTROL_PIN, HIGH);  // LED ON
  } else if(pay.equals("OFF")) {
      digitalWrite(CONTROL_PIN, LOW);   // LED OFF
  }
  commandOn = ONLINE;    // command on online
  delay(50);           // delay for stably reading status

  getStatus(ONLINE);
}

void getStatus(boolean online) {
  if(digitalRead(STATUS_PIN)) {         // LED ON
    if(lastStatus == OFF) {
      changedStatus = true;
    }
    lastStatus = ON;
  } else {                              // LED OFF
    if(lastStatus == ON) {
      changedStatus = true;
    }
    lastStatus = OFF;
  }
//  Serial.println(ledStatus);

  if(changedStatus) {
    if(!online) {
      commandOn = OFFLINE;    // command on offline
    }

    strcpy(ledStatus,lastStatus?"{\"DEVICE\":\"LED1\",\"STATUS\":\"ON\"":"{\"DEVICE\":\"LED1\",\"STATUS\":\"OFF\"");
    strcat(ledStatus,commandOn?",\"COMMANDON\":\"ONLINE\"}":",\"COMMANDON\":\"OFFLINE\"}");
    client.publish(pubTopic,ledStatus);
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

