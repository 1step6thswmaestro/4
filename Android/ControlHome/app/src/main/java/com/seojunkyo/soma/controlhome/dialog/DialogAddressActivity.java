package com.seojunkyo.soma.controlhome.dialog;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.AddressListActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.AddressList;
import com.seojunkyo.soma.controlhome.util.AddressListAdapter;

public class DialogAddressActivity extends CONTROLHOMEActivity {

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

        setContentView(R.layout.dialog_address_space);

        TranslateAnimation anim = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, -1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(300);
        anim.setFillAfter(true);
        findViewById(R.id.dialog_add_space).setAnimation(anim);
        anim.start();

        initAcitivy();
        setLayout();
    }

    private Button btn_cancel;
    private Button btn_ok;
    private EditText hubSpace;
    private EditText hubAddress;

    @Override
    public void initAcitivy() {
        hubSpace = (EditText) findViewById(R.id.popup_title_space);
        hubAddress = (EditText) findViewById(R.id.popup_address);
        btn_cancel = (Button) findViewById(R.id.popup_reminder_btn_close);
        btn_ok = (Button) findViewById(R.id.popup_reminder_btn_ok);
    }

    @Override
    public void setLayout() {
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AddressListActivity.items.add(new AddressList(hubSpace.getText().toString(), hubAddress.getText().toString()));
                } catch (Exception e) {
                    AddressListActivity.items.add(new AddressList("MY HOME", "swhomegateway.dyndns.org"));
                }
                AddressListActivity.adapter = new AddressListAdapter(getLayoutInflater(), AddressListActivity.items);
                AddressListActivity.mListView.setAdapter(AddressListActivity.adapter);
                finish();
            }
        });
    }
}