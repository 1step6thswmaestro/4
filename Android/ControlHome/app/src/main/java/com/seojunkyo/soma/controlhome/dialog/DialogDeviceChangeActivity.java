package com.seojunkyo.soma.controlhome.dialog;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.ControlActivity;
import com.seojunkyo.soma.controlhome.activity.DeviceChangeActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

public class DialogDeviceChangeActivity extends CONTROLHOMEActivity {

    public static DialogDeviceChangeActivity dialActivity;

    public static Context mContext;
    public static String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialActivity = DialogDeviceChangeActivity.this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 이부분이 화면을 dimming시킴
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.85f;
        //lpWindow.windowAnimations = android.R.anim.accelerate_interpolator | android.R.anim.fade_in | android.R.anim.fade_out;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_device_change);

        initAcitivy();
        setLayout();
    }

    private Button btn_deviceChange_cancel;
    private Button btn_deviceChange_ok;

    @Override
    public void initAcitivy() {
        mContext = this;

        btn_deviceChange_cancel = (Button) findViewById(R.id.device_change__no);
        btn_deviceChange_ok = (Button) findViewById(R.id.device_change__yes);
    }

    @Override
    public void setLayout() {
        btn_deviceChange_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceChangeActivity.publishChange();
                startActivity(new Intent(DialogDeviceChangeActivity.this, DialogLoadingActivity.class));
            }
        });
        btn_deviceChange_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}