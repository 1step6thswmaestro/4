package com.seojunkyo.soma.controlhome.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.DeviceChangeActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.GifView;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

public class DialogLoadingActivity extends CONTROLHOMEActivity {

    public static DialogLoadingActivity dialLoadingActivity;

    public static Context mContext;
    public static String address;

    private GifView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogDeviceChangeActivity dialActivity = (DialogDeviceChangeActivity)DialogDeviceChangeActivity.dialActivity;
        dialActivity.finish();

        dialLoadingActivity = DialogLoadingActivity.this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 이부분이 화면을 dimming시킴
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.85f;
        //lpWindow.windowAnimations = android.R.anim.accelerate_interpolator | android.R.anim.fade_in | android.R.anim.fade_out;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_loading);

        initAcitivy();
        setLayout();
    }
    @Override
    public void initAcitivy() {
        gifView = (GifView) findViewById(R.id.gifView);

    }

    @Override
    public void setLayout() {
        gifView.setGif(R.drawable.img_loading);
        gifView.play();
    }
}