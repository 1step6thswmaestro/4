
var mqtt = require('mqtt');
var redis = require('redis');
var mosca = require('mosca');


var ELEC = ['LED1','LED2','LED3','LED4','HUMI', 'PLUG1', 'RADIO','PROJECTOR','FAN'];

var LED = ['LED1', 'LED2', 'LED3', 'LED4'];



var net = require('net');
var HOST_PLUG1 = '192.168.0.4';
var HOST_PLUG2 = '192.168.0.11';
var PORT = 5000;


//시리얼포트
var serialport = require('serialport');
var SerialPort = serialport.SerialPort
var serialPort = new SerialPort("/dev/ttyUSB0", {
  baudRate: 9600,
  parser: serialport.parsers.readline("\r\n")
});



//SAMI
var webSocketUrl = "wss://api.samsungsami.io/v1.1/websocket?ack=true";

var SAMI = {
	LED1 : {
		ID : "fda60bd85d5d4c39b9d749bfb406c8a0",
		TOKEN : "8cad374b379f49a6a8efd8b3a734a8ee"
	},
	LED2 : {
		ID : "5765ccf159f84398b49540a4bce76eae",
		TOKEN : "cb66b6a5b6144e0eb4c008eed26d3df0"
	},
	LED3 : {
		ID : "93046319dd2540a38d666eeb0947bab8",
		TOKEN : "e557c89647c84f1d9fd28faf7d3232bb"
	},
	LED4 : {
		ID : "7540fc3d2a2043b6bad3441589dc7a15",
		TOKEN : "1a47c4a06b4543bebe40944485d26513"
	},
	PLUG1 : {
		ID : "70e78700170c498da13c515093692d66",
		TOKEN : "5baf964773f94f0488d38a6501dfb0f9"
	},
	HUMI : {
		ID: "6c8a0e6493354733a7f0bbd1c8813b2c",
		TOKEN : "66375723f4524f56b4776443d93d7382"
	},
	TEMP : {
		ID:"8757dd6e03c244519d51ec2126cc28ac",
		TOKEN : "6333e9712ed347668103fb06503f30ff"
	},
	HUM : {
		ID : "9c2eaf174dda43289d5fc33cba589cc1",
		TOKEN : "d0bf524f3495407a94c44d852b81a1f3"
	},
	FAN : {
		ID : "c49a495e4b5b4b01a5f0371d4901e909",
		TOKEN : "fc5a69034c30429e896eb8ea89abc4c2"
	}


}


var isWebSocketReady = false;
var wss = null;
var WebSocket = require('ws');



//스마트 플러그 ON/OFF 패킷
var PACKET_PLUG_ON = new Buffer([0xfa, 0x31, 0x38, 0x32, 0x43, 0x45, 0x42, 0x57, 0x01, 0x00, 0x00, 0xfb, 0x28, 0x0d]); 
var PACKET_PLUG_OFF = new Buffer([0xfa, 0x31, 0x38, 0x32, 0x43, 0x45, 0x42, 0x57, 0xa4, 0x00, 0x00, 0x00, 0xfb, 0x8d, 0x0d]); 




var PLUG1_STATUS = false;
var RADIO_STATUS = "OFF";


 // var redis_client = redis.createClient(6379,'172.16.100.62');
var redis_client = redis.createClient(6379,'localhost');

var CONTROLLER = 'CONTROLLER';
var TARGET = 'TARGET';
var COMMAND = 'COMMAND';

var motion_counter = 0;

//레디스 연결
redis_client.on('connect', function(){
  console.log('Conneted to Redis');
});



//redis
var backjack = {
  type: 'redis',
  db: 12,
  port: 6379,
  return_buffers: true,
  host: "localhost"
};
 
//mosca
var moscaSettings = {
  port: 1883,
  backend: backjack,
  persistence: {
    factory: mosca.persistence.Redis
  }
};


//for mosca 
var server = new mosca.Server(moscaSettings);
server.on('ready', 	function () {
  	console.log('Mosca server is up and running');
});

 


serialPort.on("open", function(){
  
    console.log('open');
    serialPort.on('data', function(data) {
      	try{
      		var parsed_string = JSON.parse(data);
	  
  	  		for(var i in parsed_string){
      			var data = {
      				TYPE : i,
      				VALUE : parsed_string[i]
      			}
      			mqtt_client.publish("SENSOR",JSON.stringify(data));
      			console.log("SENSOR " + JSON.stringify(data) + " published");
      			
	  			var time = getEpochTime();
      			
      			var data_for_redis = {
      				TIME : time,
      				VALUE : parsed_string[i]
      			}
				sendData(i, parsed_string[i]);
      			//motion 센서
      			if(i=='MOTION'){

      				console.log("counter : " + motion_counter);	
      				if(motion_counter>=5){

      					if(parsed_string[i]=='OFF'){
							var data = {
								CONTROLLER : "MOTION",
								TARGET : "PLUG1",	
								COMMAND : "OFF"
							}
							mqtt_client.publish('CONTROL',JSON.stringify(data));
      					}
      					motion_counter=0;
      					PLUG1_STATUS = false;
      				}else{
      					//처음 사람 왔고, 카운터0일떄
      					if(motion_counter==0 && parsed_string[i]=='ON'){
							var data = {
								CONTROLLER : "MOTION",
								TARGET : "PLUG1",
								COMMAND : "ON"
							}
							mqtt_client.publish('CONTROL',JSON.stringify(data));
							motion_counter++;
							PLUG1_STATUS = true;
						 //켜져있는상태. 사람 계속 있음
						}else if(motion_counter!=0 && parsed_string[i]=='ON'){
							if(PLUG1_STATUS == false){
								var data = {
									CONTROLLER : "MOTION",
									TARGET : "PLUG1",
									COMMAND : "ON"
								}
								mqtt_client.publish('CONTROL',JSON.stringify(data));
								motion_counter++;
								PLUG1_STATUS = true;
							}						
						}else if(motion_counter!=0 && parsed_string[i]=='OFF'){
							motion_counter++;
						}

      				}
      				redis_client.lpush("SENSOR:"+i, JSON.stringify(data_for_redis));
      			}else{
					redis_client.lpush("SENSOR:"+i, JSON.stringify(data_for_redis));

      			}

			}
		}catch(err){
			console.log(err);		
		}
      
  	
    });
});


var client_PLUG1;

client_PLUG1 = new net.Socket();
//PLUG는 게이트웨이에서 직접 소켓통신 


client_PLUG1.connect(PORT, HOST_PLUG1, function() {
	client_PLUG1.setKeepAlive(true);		   
    console.log('CONNECTED TO: ' + HOST_PLUG1 + ':' + PORT);
    client_PLUG1.write(PACKET_PLUG_OFF,function(err){				    	
    	if(err){
    		console.log(err);
    	}
    });
				   
});

var client_PLUG2;


var mqtt_client = mqtt.connect('mqtt://localhost');


mqtt_client.on('connect', function(){
	console.log('Connected to MQTT Server');

	//처음할때
	mqtt_client.subscribe('WHOLESYNC');

	//다 키기
	mqtt_client.subscribe('ALLON');

	//다 끄기
	mqtt_client.subscribe('ALLOFF');
	
	//LED만 다 키기
	mqtt_client.subscribe('ALLLEDON');

	//LED만 다 키기
	mqtt_client.subscribe('ALLLEDOFF');

	//컨트롤 정보
	mqtt_client.subscribe('CONTROL');
	
	//상태
	mqtt_client.subscribe('STATUS');

	start();

	mqtt_client.on('message',function(topic,message){
	
		var parsed_message = JSON.parse(message);
		console.log( topic + " : " + message + " arrived");			
		if(topic == 'WHOLESYNC'){

			
				
			ELEC.forEach(function(device){
				console.log(device);
				redis_client.lindex("STATUS:"+device,0,function(err,data){
				
					if(!err){
						var parsed_data = JSON.parse(data);
						var item1 ={
							DEVICE : device,
							STATUS : parsed_data.STATUS
						}
						mqtt_client.publish("SYNC",JSON.stringify(item1));
						console.log("SYNC" + JSON.stringify(item1));
					}

				});
			});
					



			
			

		
			// for(i in control_item){
			// 	console.log(control_item[i].toString());
			// }
			// control_item[2] = redis_client.lindex('CONTROL:PLUG1',0,0);
			// control_item[3] = redis_client.lindex('CONTROL:PLUG2',0,0);
			


			

		}else if(topic == 'ALLON'){
			ELEC.forEach(function(device){
				var data;
				if(device=="HUMI"){
					data = {
						CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "HIGH"
					}
				}else if(device=="FAN"){
					data = {
						CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "LOW"
					}
				}else{
					data = {
						CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "ON"
					}
				}

				
				mqtt_client.publish('CONTROL',JSON.stringify(data));
			})

		}else if(topic == 'ALLOFF'){
			ELEC.forEach(function(device){
				var data = {
						CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "OFF"
					}
				

				
				mqtt_client.publish('CONTROL',JSON.stringify(data));
			})
		}else if(topic == 'ALLLEDON'){
			LED.forEach(function(device){
				var data ={
					CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "ON"
				}
				mqtt_client.publish('CONTROL',JSON.stringify(data));

			})
		}else if(topic == 'ALLLEDOFF'){
			LED.forEach(function(device){
				var data ={
					CONTROLLER : parsed_message.CONTROLLER,					
						TARGET : device,
						COMMAND : "OFF"
				}
				mqtt_client.publish('CONTROL',JSON.stringify(data));

			})
		}else

		 if(topic == 'CONTROL'){
			
			if(parsed_message[TARGET] == 'PROJECTOR'){
				var data = {
					DEVICE : "PROJECTOR",
					STATUS : parsed_message[COMMAND],
					COMMANDON : "OFFLINE"
				}

				if(parsed_message[COMMAND] == 'ONOFF'){
					serialPort.write("0", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}else{
					serialPort.write("ls\n", function(err) {
			     		console.log('err ' + err);			      	
			    	});
				}
				mqtt_client.publish('STATUS', JSON.stringify(data));



			}else if(parsed_message[TARGET] == 'RADIO'){
				var data = {
					DEVICE : "RADIO",
					STATUS : parsed_message[COMMAND],
					COMMANDON : "OFFLINE"
				}
				if(parsed_message[COMMAND] == 'MODE'){
					serialPort.write("2", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}else if(parsed_message[COMMAND] == 'PLAYPAUSE'){
					serialPort.write("1", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}else if(parsed_message[COMMAND] == 'VOLUMEUP'){
					serialPort.write("3", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}if(parsed_message[COMMAND] == 'VOLUMEDOWN'){
					serialPort.write("4", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}if(parsed_message[COMMAND] == 'NEXT'){
					serialPort.write("5", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}if(parsed_message[COMMAND] == 'PREV'){
					serialPort.write("6", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}if(parsed_message[COMMAND] == 'CHANNELUP'){
					serialPort.write("7", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}if(parsed_message[COMMAND] == 'CHANNELDOWN'){
					serialPort.write("8", function(err) {
				    	console.log('err ' + err);				    	
				    });
				}else{
					serialPort.write("ls\n", function(err) {
			     		console.log('err ' + err);			      	
			    	});
				}
				mqtt_client.publish('STATUS', JSON.stringify(data));


			}else if(parsed_message[TARGET] == 'PLUG1'){
				
				var data = {
					DEVICE : "PLUG1",
					STATUS : parsed_message[COMMAND],
					COMMANDON : "OFFLINE"
				}
			    if(parsed_message[COMMAND] == 'ON'){
				    client_PLUG1.write(PACKET_PLUG_ON,function(err){				    	
				    	if(err){
				    		console.log(err);

				    		//키넥트 앞에서 모션카운터 젠다고 가정.
				    }	else{

				   			// mqtt_client.publish('STATUS', JSON.stringify(data));
				    	}
				    });
				   	
				   
				}else if(parsed_message[COMMAND] == 'OFF'){
					
				     client_PLUG1.write(PACKET_PLUG_OFF,function(err){
				    	if(err){
				    		console.log(err);
				    	}else{
				   			// mqtt_client.publish('SYNC', JSON.stringify(data));
				    	}
				    });

				    
				}
				

				mqtt_client.publish('STATUS', JSON.stringify(data));
				

				
				
			}else if(parsed_message[TARGET] == 'PLUG2'){

				var data = {
					DEVICE : "PLUG2",
					STATUS : parsed_message[COMMAND],
					COMMANDON : "OFFLINE"

				}

			
			    if(parsed_message[COMMAND] == 'ON'){
				    
				     client_PLUG2.write(PACKET_PLUG_ON,function(err){
				    	if(err){
				    		console.log(err);
				    	}else{
				   			// mqtt_client.publish('SYNC', JSON.stringify(data));
				    	}
				    });
				   

				}else if(parsed_message[COMMAND] == 'OFF'){
					
				     client_PLUG2.write(PACKET_PLUG_OFF,function(err){
				    	if(err){
				    		console.log(err);
				    	}else{
				   			// mqtt_client.publish('SYNC', JSON.stringify(data));
				    	}
				    });

				    
				}
				mqtt_client.publish('STATUS', JSON.stringify(data));
		
						
					

				
			
			}else{

				//CONTROL 오면 단말로 publish
	 			mqtt_client.publish(parsed_message[TARGET], parsed_message[COMMAND]);
	 			console.log(parsed_message[TARGET] + " : " + parsed_message[COMMAND] + " published");

			}



 			//CONTROL 오면 다른 컨트롤러들에게 동기화 정보
 			mqtt_client.publish('SYNCCONTROL', message);
			console.log("SYNCCONTROL " + message + " published");


			console.log( "CONTROLLER : " + parsed_message[CONTROLLER]);			
			console.log( "TARGET : " + parsed_message[TARGET]);			
			console.log( "COMMAND : " + parsed_message[COMMAND]);		
			


			//CONTROL 레디스에 저장
			var moment = require('moment');
	  		var time = moment().format('YYYYMMDD-HHmmss');

	  		var redis_data = {
				TIME : time,
				CONTROLLER  : parsed_message[CONTROLLER],
				COMMAND  : parsed_message[COMMAND]
			};
			console.log("CONTROL" + ":" + parsed_message[TARGET], JSON.stringify(redis_data));
			redis_client.lpush("CONTROL" + ":" + parsed_message[TARGET], JSON.stringify(redis_data));
				
			
		}else if(topic == 'STATUS'){


			var parsed_message = JSON.parse(message);
			var data = {};

			if(parsed_message.COMMANDON=='OFFLINE'){
				
				data.DEVICE = parsed_message.DEVICE;
				data.STATUS = parsed_message.STATUS;

			}else if(parsed_message.COMMANDON=='ONLINE'){
				data.DEVICE = parsed_message.DEVICE;
				data.STATUS = parsed_message.STATUS;
				
				redis_client.lindex("CONTROL:"+parsed_message.DEVICE, 0,function(err,data1){
					data.CONTROLLER = data1.CONTROLLER;
				});
				
			}
			
			var string_data = JSON.stringify(data)
			mqtt_client.publish('SYNCDEVICE', string_data);
			console.log("SYNCDEVICE " + string_data + " published");
			if (!isWebSocketReady){
            	console.log("Websocket is not ready. Skip sending data to SAMI (data:" + data +")");
            	return;
    		}
			
			
			
	  		var time = getEpochTime();

	  		var redis_data = {
				TIME : time,
				STATUS : parsed_message.STATUS
			};


			redis_client.lpush("STATUS" + ":" + data.DEVICE, JSON.stringify(redis_data));
        	sendData(parsed_message.DEVICE, parsed_message.STATUS);	
			
			

			
            //console.log("Serial port received data:" + data);
            //var flameDigitalValue = parseInt(data);

            // flameDigitalValue = 1 ==> no fire is detected
            // flameDigitalValue = 0 ==> fire is detected
            //레디스에 푸시
			


		}
		
		

	
	});
});




function start() {
    //Create the websocket connection
    isWebSocketReady = false;
    ws = new WebSocket(webSocketUrl);
    ws.on('open', function() {
        console.log("Websocket connection is open ....");
        register();
    });
    ws.on('message', function(data, flags) {
        console.log("Received message: " + data + '\n');
    });
    ws.on('close', function() {
        console.log("Websocket connection is closed ....");
    });
}

function register(){
    console.log("Registering device on the websocket connection");
    

    for(var i in SAMI){
    	var JSON_SAMI = SAMI[i];
    	try{
	        var registerMessage = '{"type":"register", "sdid":"'+JSON_SAMI.ID+'", "Authorization":"bearer '+JSON_SAMI.TOKEN+'", "cid":"'+getEpochTime()+'"}';
	        console.log('Sending register message ' + registerMessage + '\n');
	        ws.send(registerMessage, {mask: true});
	        isWebSocketReady = true;
	    }
	    catch (e) {
	        console.error('Failed to register messages. Error in registering message: ' + e.toString());
	    }  

    }

    
    

}

function sendData(TARGET,VALUE){
    try{
        ts = ', "ts": '+getEpochTime();
        var time = getEpochTime();
        var data;
        if(TARGET == "TEMP"){
         
        	data = {
          		epochtime: time,
           		temperature : VALUE 
			};
        }else if(TARGET == "HUM"){
           	data = {
          		epochtime : time,
           		humidity : VALUE
			};
        }else{
        	data = {
            	epochtime: time,
            	status: VALUE
        	};
        }

        
        if(SAMI.hasOwnProperty(TARGET)){
        	var JSON_SAMI = SAMI[TARGET];
        	var payload = '{"sdid":"'+JSON_SAMI.ID+'"'+ts+', "data": '+JSON.stringify(data)+', "cid":"'+getEpochTime()+'"}';;
	        console.log('Sending payload ' + payload);
	        ws.send(payload, {mask: true});
        }
        
        
    } catch (e) {
        console.error('Error in sending a message: ' + e.toString());
    }   
}


function writeData(socket, data){
    var success = !socket.write(data);
    if(!success){
        (function(socket, data){
            socket.once('drain', function(){
                writeData(socket, data);
            });
        })(socket, data)
    }
}


function getEpochTime() {
	var now = new Date();
	return now.getTime();
}





