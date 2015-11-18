var express = require('express');
var handlebars = require('express-handlebars').create();
var app = express();
var server = require('http').Server(app);
var logger = require('morgan');
var bodyParser = require('body-parser');

var io = require('socket.io')(server);
var userAuth = require('./middleware/user-auth.js');

var redis = require('redis');
var mqtt = require('mqtt');

var dbClient = redis.createClient();
//var mqttClientSubscribe = mqtt.connect('mqtt://172.16.100.62');
//var mqttClientPublish = mqtt.connect('mqtt://172.16.100.62');
var mqttClientSubscribe = mqtt.connect('mqtt://swhomegateway.dyndns.org');
var mqttClientPublish = mqtt.connect('mqtt://swhomegateway.dyndns.org');

var devices = [
	{ name: 'LED1', status: 'STATUS' },
	{ name: 'LED2', status: 'STATUS' },
	{ name: 'LED3', status: 'STATUS' },
  { name: 'LED4', status: 'STATUS' },
  { name: 'HUMI', status: 'STATUS' },
  { name: 'FAN', status: 'STATUS' },
  { name: 'RADIO', status: 'STATUS' },
  { name: 'PROJECTOR', status: 'STATUS' }
];

var status = [
  { type: 'TEMP', value: 0 },
  { type: 'HUM', value: 0 },
  { type: 'MOTION', value: 'OFF' }
];

app.engine('handlebars', handlebars.engine);

app.set('port', process.env.PORT || 8888);
app.set('view engine', 'handlebars');

app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(__dirname + '/public'));
app.use(express.static('.'));


// Routing

/* LOG IN */

app.get('/', function(req, res) {
  res.redirect('/dashboard');
});

app.get('/login', function(req, res) {
  res.render('layouts/login', {});
});

app.post('/login', function(req, res) {
    var id = req.body.userId;
    var password = req.body.userPwd;
    console.log(id + ':' + password);

    userAuth.validate(id, password);
    res.redirect('/dashboard');
});

/* API - LOGIN */

app.post('/api/login', function(req, res) {
    var id = req.body.userId;
    var password = req.body.userPwd;
    console.log(id + ':' + password);
    var response = { login: 'correct' };
    res.json('200', response);
});

/* REGISTER */

app.get('/register', function(req, res) {
  res.render('layouts/register', {});
});

app.post('/register', function(req, res) {
  if (req.method == 'POST') {
    var id = req.body.userId;
    var password = req.body.userPwd;
    var name = req.body.userName;
    var home = req.body.homeId;

    userAuth.register(name, id, password, home);    

    res.redirect('/login');
  }
});

/* DASHBOARD */

app.get('/dashboard', function(req, res) {
  var initInfo = { 'CONTROLLER': 'WEB'};
  mqttClientPublish.publish('WHOLESYNC', JSON.stringify(initInfo));
  res.render('layouts/dashboard');
});

app.get('/demo', function(req, res) {
  var initInfo = { 'CONTROLLER': 'WEB'};
  mqttClientPublish.publish('WHOLESYNC', JSON.stringify(initInfo));
  res.render('layouts/main', { title: 'HOME\nMY\nHOME', items: devices });
});

app.post('/demo', function(req, res) {
  if (req.method == 'POST') {
    var command = req.body.command;
    var commandArr = command.split(':');
    var controlInfo;
    if (commandArr[0] == 'ALL') {
      controlInfo = { 'CONTROLLER': 'WEB'  };
      mqttClientPublish.publish('ALL' + commandArr[1], JSON.stringify(controlInfo));
    }
    else {
    	controlInfo = { 'CONTROLLER': 'WEB', 'TARGET': commandArr[0], 'COMMAND':commandArr[1] };
    	mqttClientPublish.publish('CONTROL', JSON.stringify(controlInfo));
    }
  //res.local();
  res.render('layouts/main', { title: 'HOME\nMY\nHOME', items: devices });
 }
});

app.get('/statistics', function(req, res) {
  res.render('layouts/statistics');
});


// 404 error handler

app.use(function(req, res) {
  res.type('text/plain');
  res.status(404);
  res.send('404-Not Found');
});

server.listen(app.get('port'), function() {
    console.log('Express started on localhost:' + app.get('port') + '\n press Ctrl-C to terminate.');
});


// db connect

dbClient.on('connect', function() {
  console.log('RedisDB connected');
});

dbClient.on('error', function(error) {
  console.log('RedisDB error');
  console.log(error);
});


// mqtt connect

mqttClientSubscribe.on('connect', function() {
    console.log('mqtt-subscribe connected');
    mqttClientSubscribe.subscribe('SYNC');
    mqttClientSubscribe.subscribe('SYNCCONTROL');
    mqttClientSubscribe.subscribe('SYNCDEVICE');
    mqttClientSubscribe.subscribe('SENSOR');
});

mqttClientSubscribe.on('close', function() {
  console.log('mqtt-subscribe disconnected');
});

mqttClientSubscribe.on('error', function(error) {
  console.log('mqtt-subscribe error');
  console.log(error);
});

// if mqtt recieved message

mqttClientSubscribe.on('message', function (topic, message) {
  console.log(topic.toString() + ':' + message.toString());
  
  var moment = require('moment');
  var time = moment().format('YYYYMMDD-HHmmss');
  var parsedMessage = JSON.parse(message);

  if(topic == 'SYNC' || topic == 'SYNCDEVICE') {
    for (var i in devices) {
      if (devices[i].name == parsedMessage['DEVICE']) {
        devices[i].status = parsedMessage['STATUS'];
        break;
      }
    }
    //dbClient.lpush('STATUS' + ':' + parsedMessage['DEVICE'], time + ':' + parsedMessage['STATUS']);
    io.sockets.emit('deviceUpdate', devices);
  }
  
  else if (topic == 'SYNCCONTROL') {
    //dbClient.lpush('CONTROL' + ':' + parsedMessage['TARGET'], 
//    time + ':' + parsedMessage['CONTROLLER'] + ':' + parsedMessage['COMMAND']);
  }

  else if (topic == 'SENSOR') {
    for (var i in status) {
      if (status[i].type == parsedMessage['TYPE']) {
        status[i].value = parsedMessage['VALUE'];
        break;
      }
    }
    io.sockets.emit('statusUpdate', status);
  }
  
});

mqttClientPublish.on('connect', function() {
	console.log('mqtt-publish connected');
});

mqttClientPublish.on('offline', function() {
  	console.log('mqtt-publish disconnected');
});

mqttClientPublish.on('error', function(error) {
	console.log('mqtt-publish error');
	console.log(error);
});


// Socket.io

io.on('connection', function(socket) {
  console.log('socket.io connected');
  socket.emit('deviceUpdate', devices);
  socket.emit('statusUpdate', status);

  socket.on('front-controlPublish', function(data) {
    console.log(data);
    mqttClientPublish.publish('CONTROL', JSON.stringify(data));
  });

  socket.on('front-LEDALL', function(data) {
    var message = { 'CONTROLLER':'WEB' };
    mqttClientPublish.publish('ALLLED' + data, JSON.stringify(message));
  });

  socket.on('front-ALLOFF', function(data) {
    console.log(data);
    mqttClientPublish.publish('ALLOFF', JSON.stringify(data));
  })

  socket.on('front-ALLON', function(data) {
    mqttClientPublish.publish('ALLON', JSON.stringify(data));
  });

  socket.on('front-display', function(data) {
    //console.log(data);
  });

  socket.on('disconnect', function() {
    console.log('client disconnected');
  });
});



