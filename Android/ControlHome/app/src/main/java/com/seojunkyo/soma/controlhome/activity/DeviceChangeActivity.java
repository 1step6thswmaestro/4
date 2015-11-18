package com.seojunkyo.soma.controlhome.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.dialog.DialogDeviceChangeActivity;
import com.seojunkyo.soma.controlhome.dialog.DialogMqttConnectActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;

import org.json.JSONObject;


/**
 * Created by seojunkyo on 15. 10. 27..
 */
public class DeviceChangeActivity extends CONTROLHOMEActivity {

    private String result;
    public static String KINECTTOPIC = "";
    public static String DEVICE = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_change);

        initAcitivy();
        setLayout();
    }

    public static ToggleButton mBtnChangeLed1;
    public static ToggleButton mBtnChangeLed2;
    public static ToggleButton mBtnChangeLed3;
    public static ToggleButton mBtnChangeLed4;

    public static ToggleButton mBtnChangeHumi;
    public static ToggleButton mBtnChangeRadio;
    public static ToggleButton mBtnChangeFan;

    ImageButton mBtnChangeDevice;

    @Override
    public void initAcitivy() {

        mBtnChangeLed1 = (ToggleButton) findViewById(R.id.btn_change_led1);
        mBtnChangeLed2 = (ToggleButton) findViewById(R.id.btn_change_led2);
        mBtnChangeLed3 = (ToggleButton) findViewById(R.id.btn_change_led3);
        mBtnChangeLed4 = (ToggleButton) findViewById(R.id.btn_change_led4);

        mBtnChangeHumi = (ToggleButton) findViewById(R.id.btn_change_humi);
        mBtnChangeRadio = (ToggleButton) findViewById(R.id.btn_change_radio);
        mBtnChangeFan = (ToggleButton) findViewById(R.id.btn_change_fan);

        mBtnChangeDevice = (ImageButton) findViewById(R.id.btn_device_register);
        //topic: KINECTSELECT
        //message: (기기 이름)

    }

    @Override
    public void setLayout() {

        mBtnChangeLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeLed1.isChecked()) {
                    DEVICE = "LED1";
                }
            }
        });

        mBtnChangeLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeLed2.isChecked()) {
                    DEVICE = "LED2";
                }
            }
        });

        mBtnChangeLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeLed3.isChecked()) {
                    DEVICE = "LED3";
                }
            }
        });

        mBtnChangeLed4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeLed4.isChecked()) {
                    DEVICE = "LED4";
                }
            }
        });

        mBtnChangeHumi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeHumi.isChecked()) {
                    DEVICE = "HUMI";
                }
            }
        });

        mBtnChangeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeRadio.isChecked()) {
                    DEVICE = "RADIO";
                }
            }
        });
        mBtnChangeFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnChangeFan.isChecked()) {
                    DEVICE = "FAN";
                }
            }
        });

        mBtnChangeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeviceChangeActivity.this, DialogDeviceChangeActivity.class));
            }
        });
    }

    public static void publishChange(){
        KINECTTOPIC = "KINECT:SELECT";
        ControlActivity.publish(KINECTTOPIC, DEVICE);
    }

    public static void changeStatus(){
        mBtnChangeLed1.setChecked(false);
        mBtnChangeLed2.setChecked(false);
        mBtnChangeLed3.setChecked(false);
        mBtnChangeLed4.setChecked(false);

        mBtnChangeHumi.setChecked(false);
        mBtnChangeRadio.setChecked(false);
        mBtnChangeFan.setChecked(false);
    }
}
