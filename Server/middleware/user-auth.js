var crypto = require('crypto');
var redis = require('redis');

function registerUser (name, userId, userPwd, homeId) {
	var dbClient = redis.createClient();
	dbClient.on('connect', function() {
  		console.log('RedisDB-register connected');

		dbClient.lpush('LIST:USERS', userId);				// input userID to whole user list
		dbClient.lpush('LIST:USERS:' + homeId, userId);		// input userID to HOME user list
		
		// set key
		var key = 'USER:' + userId;

		// generate salt
		var salt = crypto.randomBytes(128).toString('base64');
		console.log('salt: ', salt);

		// create hash
		var hashedPw;
		crypto.pbkdf2( userPwd, salt, 10000, 512, function(err, encodedPassword) {
			if (err) throw err;
			hashedPw = encodedPassword.toString('hex');	
			// input salt and hashed password
			dbClient.hmset(key, {
				"SALT": salt,
				"PASSWORD": hashedPw,
				"NAME": name,
				"HOMEID": homeId
			}, redis.print);
		});
	});
	
}

function validateUser (userId, userPwd) {
	var dbClient = redis.createClient();
	dbClient.on('connect', function() {
		var key = 'USER:' + userId;
		console.log(key);
		
		dbClient.hgetall(key, function(err, res) {
			if (err) {
				console.log(err);
			}
			var userInfo = res;
			if (userInfo == null) return false;
			crypto.pbkdf2( userPwd, userInfo['SALT'], 10000, 512, function(err, encodedPassword) { 
				if (err) throw err;
				var pwd = encodedPassword.toString('hex');
				if (pwd == userInfo['PASSWORD']) {
					console.log('same same');
					return true;	
				}
				else {
					console.log('not same oh my');
					return false;
				}
			});
		});
	});
}

function encryptPassword (password, salt) {
	crypto.pbkdf2(userPwd, userInfo['SALT'], 10000, 512, function(err, encodedPassword) {

		return encodedPassword;
	});
}

function getUserInfo (userId) {}

exports.register = registerUser;
exports.validate = validateUser;