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
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

public class DialogMqttConnectActivity extends CONTROLHOMEActivity {

    public static Context mContext;
    public static String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 이부분이 화면을 dimming시킴
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.85f;
        //lpWindow.windowAnimations = android.R.anim.accelerate_interpolator | android.R.anim.fade_in | android.R.anim.fade_out;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_yesno);

        initAcitivy();
        setLayout();
    }

    private Button btn_connect_cancel;
    private Button btn_connect_ok;

    @Override
    public void initAcitivy() {
        mContext = this;

        btn_connect_cancel = (Button) findViewById(R.id.connect_no);
        btn_connect_ok = (Button) findViewById(R.id.connect_yes);
    }

    @Override
    public void setLayout() {
        btn_connect_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MQTTUtils.connect(address)) {
                    Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_SHORT).show();
                    ControlActivity.server = address;
                    startActivity(new Intent(DialogMqttConnectActivity.this, ControlActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        btn_connect_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}