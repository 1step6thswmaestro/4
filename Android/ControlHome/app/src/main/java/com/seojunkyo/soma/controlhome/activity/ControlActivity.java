package com.seojunkyo.soma.controlhome.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.dialog.DialogDeviceChangeActivity;
import com.seojunkyo.soma.controlhome.dialog.DialogLoadingActivity;
import com.seojunkyo.soma.controlhome.fragment.FanItemFragment;
import com.seojunkyo.soma.controlhome.fragment.HUMIItemFragment;
import com.seojunkyo.soma.controlhome.fragment.LEDItemFragment;
import com.seojunkyo.soma.controlhome.fragment.RadioItemFragment;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.AddressList;
import com.seojunkyo.soma.controlhome.util.AddressListAdapter;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class ControlActivity extends CONTROLHOMEActivity {

    public ConnectivityManager mConnMan;
    private Context anoactivity;
    private Thread thread;

    private static final String TAG = "MQTTService";

    private int NUM_PAGES = 4;        // 최대 페이지의 수
    private int SUB_MAX_SIZE = 6;

    public static String deviceId;
    public static String server;
    public static String pubTOPIC;

    public static volatile IMqttAsyncClient mqttClient;
    private boolean hasWifi = false;
    private boolean hasMmobile = false;

    private String[] subTOPIC = new String[SUB_MAX_SIZE];

    /* Fragment numbering */
    public final static int FRAGMENT_PAGE1 = 0;
    public final static int FRAGMENT_PAGE2 = 1;
    public final static int FRAGMENT_PAGE3 = 2;
    public final static int FRAGMENT_PAGE4 = 3;

    ViewPager mViewPager;            // View pager를 지칭할 변수

    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;

    LEDItemFragment ledFragment;
    HUMIItemFragment humiFragment;
    RadioItemFragment radioFragment;
    FanItemFragment fanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        IntentFilter intentf = new IntentFilter();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        initAcitivy();
        setLayout();
    }

    private Button mBtnMoveDash;
    private ImageButton mBtnMoveChange;
    public static EditText mEdtNoti;
    public static ImageView mImgCircle1;
    public static ImageView mImgCircle2;
    public static ImageView mImgCircle3;
    public static ImageView mImgCircle4;
    public static ImageView mImgNotiIcon;


    @Override
    public void initAcitivy() {
        /*String firstMsg = String.format("{\"CONTROLLER\":\"ANDROID\"}");
        MqttMessage message = new MqttMessage(firstMsg.getBytes());
        try {
            mqttClient.publish("WHOLESYNC", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

        setClientID("HOME");
        pubTOPIC = "CONTROL";

        mEdtNoti = (EditText) findViewById(R.id.current_noti);
        mImgNotiIcon = (ImageView) findViewById(R.id.img_current_noti);
        mBtnMoveChange = (ImageButton) findViewById(R.id.btn_devcie_change);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        ledFragment = new LEDItemFragment();
        humiFragment = new HUMIItemFragment();
        radioFragment = new RadioItemFragment();
        fanFragment = new FanItemFragment();

        mBtnMoveDash = (Button) findViewById(R.id.move_dashboard);
        mImgCircle1 = (ImageView) findViewById(R.id.control_circle1);
        mImgCircle2 = (ImageView) findViewById(R.id.control_circle2);
        mImgCircle3 = (ImageView) findViewById(R.id.control_circle3);
        mImgCircle4 = (ImageView) findViewById(R.id.control_circle4);

        subTOPIC = new String[]{"SYNCDEVICE", "SYNCCONTROL", "SENSOR", "SYNC", "KINECT:RES", "KINECT:REGISTERED"};
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    @Override
    public void setLayout() {
        fragmentTransaction.add(ledFragment, "LED_FRAGMENT");
        fragmentTransaction.add(fanFragment, "FAN_FRAGMENT");
        fragmentTransaction.add(humiFragment, "HUMI_FRAGMENT");
        fragmentTransaction.add(radioFragment, "RADIO_FRAGMENT");

        fragmentTransaction.commit();

        mViewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(FRAGMENT_PAGE1);

        mBtnMoveDash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ControlActivity.this, DashboardActivity.class));
            }
        });
        mBtnMoveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kinectTopic = "KINECT:MODE";
                String PAYLOAD = "REGISTER";
                publish(kinectTopic, PAYLOAD);
                Toast.makeText(ControlActivity.this, "잠시만 기다려주십시요", Toast.LENGTH_SHORT).show();
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public static void publish(String pubTOPIC, String PAYLOAD) {
        MqttMessage message = new MqttMessage(PAYLOAD.getBytes());
        try {
            mqttClient.publish(pubTOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private class pagerAdapter extends FragmentPagerAdapter {

        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        // 특정 위치에 있는 Fragment를 반환해준다.
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LEDItemFragment();
                case 1:
                    return new HUMIItemFragment();
                case 2:
                    return new FanItemFragment();
                case 3:
                    return new RadioItemFragment();
                default:
                    return null;
            }
        }

        // 생성 가능한 페이지 개수를 반환해준다.
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return NUM_PAGES;
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    public class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMqttToken token;
            /*Bundle bundle = intent.getExtras();
            String server = bundle.getString("server");
            String server = "192.168.56.1";*/
            //Log.d("Server: ", server);
            boolean hasConnectivity = false;
            boolean hasChanged = false;
            anoactivity = context;
            mConnMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo infos[] = mConnMan.getAllNetworkInfo();

            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")) {
                    if ((infos[i].isConnected() != hasMmobile)) {
                        hasChanged = true;
                        hasMmobile = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if (infos[i].getTypeName().equalsIgnoreCase("WIFI")) {
                    if ((infos[i].isConnected() != hasWifi)) {
                        hasChanged = true;
                        hasWifi = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }
            hasConnectivity = hasMmobile || hasWifi;
            Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " + (mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect(server, subTOPIC);
            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                Log.d(TAG, "doDisconnect()");
                try {
                    token = mqttClient.disconnect();
                    token.waitForCompletion(1000);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        private void doConnect(String server, String[] subTOPIC) {
            IMqttToken token;
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            try {
                Log.d(TAG, "doConnect()");
                mqttClient = new MqttAsyncClient("tcp://" + server + ":1883", deviceId, new MemoryPersistence());
                token = mqttClient.connect();
                token.waitForCompletion(3500);
                mqttClient.setCallback(new MqttEventCallback());
                for (int i = 0; i < subTOPIC.length; i++)
                    token = mqttClient.subscribe(subTOPIC[i], 0);
                token.waitForCompletion(5000);
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                switch (e.getReasonCode()) {
                    case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                    case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    case MqttException.REASON_CODE_CONNECTION_LOST:
                    case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                        Log.v(TAG, "c" + e.getMessage());
                        e.printStackTrace();
                        break;
                    case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                        Intent i = new Intent("RAISEALLARM");
                        i.putExtra("ALLARM", e);
                        Log.e(TAG, "b" + e.getMessage());
                        break;
                    default:
                        Log.e(TAG, "a" + e.getMessage());
                }
            }
        }

        private class MqttEventCallback implements MqttCallback {
            @Override
            public void connectionLost(Throwable arg0) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {

            }

            @Override
            @SuppressLint("NewApi")
            public void messageArrived(final String subTOPIC, final MqttMessage msg) throws Exception {
                Log.i(TAG, "Message arrived from subTOPIC" + subTOPIC);

                Handler h = new Handler(getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        String payload = new String(msg.getPayload());
                        if (subTOPIC.equals("KINECT:RES")) {
                            startActivity(new Intent(ControlActivity.this, DeviceChangeActivity.class));
                        } else if (subTOPIC.equals("KINECT:REGISTERED")) {
                            DeviceChangeActivity.changeStatus();
                            DialogLoadingActivity dialActivity = (DialogLoadingActivity) DialogLoadingActivity.dialLoadingActivity;
                            dialActivity.finish();
                        } else if (subTOPIC.equals("SENSOR")) {
                            try {
                                DashboardActivity.changeStatus(payload);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                JSONObject subObject = new JSONObject(payload);
                                setCurrentNotification(subObject);
                                String DEVICE = subObject.getString("TARGET");
                                switch (DEVICE) {
                                    case "LED1":
                                    case "LED2":
                                    case "LED3":
                                    case "LED4":
                                        LEDItemFragment.changeStatus(DEVICE, subObject);
                                        break;
                                    case "HUMI":
                                        HUMIItemFragment.changeStatus(DEVICE, subObject);
                                        break;
                                    case "RADIO":
                                        RadioItemFragment.changeStatus(DEVICE, subObject);
                                        break;
                                    case "FAN":
                                        FanItemFragment.changeStatus(DEVICE, subObject);
                                        break;
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "예외 발생 =" + e);
                            }
                        }
                    }
                });
            }
        }

        public void setCurrentNotification(JSONObject subJSON) throws Exception {
            Log.d("set test: ", "SUCCESS");
            String VALUE = "SUCCESS";
            String currentCONTROLLER = subJSON.getString("CONTROLLER");
            String currentDevice = subJSON.getString("TARGET");
            String currentStatus = subJSON.getString("COMMAND");

            if (currentCONTROLLER.equals("ANDROID")) {
                currentCONTROLLER = "모바일";
            } else if (currentCONTROLLER.equals("KINECT")) {
                currentCONTROLLER = "키넥트";
            } else {
                currentCONTROLLER = "웹";
            }
            VALUE = currentCONTROLLER + "에서 " + currentDevice + "을(를) " + currentStatus + "하였습니다";

            SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(VALUE);

            mSpannableStringBuilder.setSpan(android.R.color.holo_orange_light, 0,
                    currentCONTROLLER.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mImgNotiIcon.setBackgroundResource(R.drawable.img_noti_icon);
            mEdtNoti.setText(mSpannableStringBuilder);
        }

        public void onConfigurationChanged(Configuration newConfig) {
            Log.d(TAG, "onConfigurationChanged()");
            android.os.Debug.waitForDebugger();
            onConfigurationChanged(newConfig);
        }

        public String getThread() {
            return Long.valueOf(thread.getId()).toString();
        }

        public IBinder onBind(Intent intent) {
            Log.i(TAG, "onBind called");
            return null;
        }
    }

    public static void setClientID(String id) {
        deviceId = id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


