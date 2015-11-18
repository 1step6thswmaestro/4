package com.seojunkyo.soma.controlhome.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTUtils extends FragmentActivity{

	public static MqttAsyncClient client;
    private String clientHandle = null;
    private Context context = null;

	public static MqttAsyncClient getClient() {
		return client;
	}

	public static boolean connect(String url) {
		try {
			MemoryPersistence persistance = new MemoryPersistence();
			client = new MqttAsyncClient("tcp://" + url + ":1883", "HOME", persistance);
			client.connect();
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

		public static boolean pub(String topic, String payload) {
			MqttMessage message = new MqttMessage(payload.getBytes());
			try {
				Log.d("pub TEXT: ", topic);
				client.publish(topic, message);
				return true;
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
		}
		return false;
	}
}
