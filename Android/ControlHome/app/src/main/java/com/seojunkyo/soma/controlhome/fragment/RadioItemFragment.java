package com.seojunkyo.soma.controlhome.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.ControlActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEFragment;

import org.json.JSONObject;

public class RadioItemFragment extends CONTROLHOMEFragment {

    public static String radioTOPIC;
    private String deviceId;

    public RadioItemFragment() {
        // Required empty public constructor
    }

    public static RadioItemFragment newInstance() {
        RadioItemFragment fragment = new RadioItemFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radio_list, container, false);

        initFragment(view);
        setLayout(view);


        return view;
    }

    public static ToggleButton mImgTogContolRADIO;
    public static ToggleButton mBtnPlay;
    public static ImageButton mBtnPre;
    public static ImageButton mBtnMinus;
    public static ImageButton mBtnPlus;
    public static ImageButton mBtnNext;

    @Override
    public void initFragment(View view) {
        radioTOPIC = "CONTROL";
        mImgTogContolRADIO = (ToggleButton) view.findViewById(R.id.toggle_radio);

        mBtnPlay = (ToggleButton) view.findViewById(R.id.btn_sound_play);
        mBtnPre = (ImageButton) view.findViewById(R.id.btn_sound_prev);
        mBtnMinus = (ImageButton) view.findViewById(R.id.btn_sound_minus);
        mBtnPlus = (ImageButton) view.findViewById(R.id.btn_sound_plus);
        mBtnNext = (ImageButton) view.findViewById(R.id.btn_sound_next);

        ControlActivity.mImgCircle1.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle2.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle4.setBackgroundResource(R.drawable.img_circle_draw);
    }

    @Override
    public void setLayout(View view) {
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishRADIO_ICON(mBtnPlay, "PLAYPAUSE");
            }
        });
        mBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishRADIO("PREV");
            }
        });
        mBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishRADIO("VOLUMEDOWN");
            }
        });
        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishRADIO("VOLUMEUP");
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishRADIO("NEXT");
            }
        });
    }

    public static void publishRADIO(String status) {
        String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"RADIO\", \"COMMAND\":\"%s\"}", status);
        ControlActivity.publish(radioTOPIC, PAYLOAD);
    }

    public static void publishRADIO_ICON(ToggleButton BTN, String status) {
        if (BTN.isChecked()) {
            BTN.setBackgroundResource(R.drawable.btn_sound_play);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"RADIO\", \"COMMAND\":\"%s\"}", status);
            ControlActivity.publish(radioTOPIC, PAYLOAD);

        } else if (!BTN.isChecked()) {
            BTN.setBackgroundResource(R.drawable.btn_sound_pause);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"RADIO\", \"COMMAND\":\"%s\"}", status);
            ControlActivity.publish(radioTOPIC, PAYLOAD);
        }
    }

    public static void changeStatus(String DEVICE, JSONObject subObject) throws Exception {
        String STATUS = subObject.getString("COMMAND");

        switch (STATUS) {
            case "on":
                mImgTogContolRADIO.setBackgroundResource(R.drawable.btn_radio_on);
                break;
            case "OFF":
                mImgTogContolRADIO.setBackgroundResource(R.drawable.btn_radio_off);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
//        mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }


}
