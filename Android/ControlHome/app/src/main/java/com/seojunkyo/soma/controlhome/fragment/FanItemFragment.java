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

public class FanItemFragment extends CONTROLHOMEFragment {

    public static String fanTOPIC;
    private String deviceId;

    public FanItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static FanItemFragment newInstance() {
        FanItemFragment fragment = new FanItemFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fan_list, container, false);

        initFragment(view);
        setLayout(view);

        return view;
    }

    public static ToggleButton mImgTogContolFan;
    public static ToggleButton mBtnFanULow;
    public static ToggleButton mBtnFanLow;
    public static ToggleButton mBtnFanMid;
    public static ToggleButton mBtnFanHigh;
    public static ToggleButton mBtnFanSpin;

    @Override
    public void initFragment(View view) {
        fanTOPIC = "CONTROL";
        mBtnFanULow = (ToggleButton) view.findViewById(R.id.btn_fan_ulow);
        mBtnFanLow = (ToggleButton) view.findViewById(R.id.btn_fan_low);
        mBtnFanMid = (ToggleButton) view.findViewById(R.id.btn_fan_mid);
        mBtnFanHigh = (ToggleButton) view.findViewById(R.id.btn_fan_high);
        mBtnFanSpin = (ToggleButton) view.findViewById(R.id.btn_fan_spin);

        ControlActivity.mImgCircle1.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle2.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_draw);
        ControlActivity.mImgCircle4.setBackgroundResource(R.drawable.img_circle_empty);

        mImgTogContolFan = (ToggleButton) view.findViewById(R.id.toggle_fan);
    }

    @Override
    public void setLayout(View view) {
        mImgTogContolFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "Fan";
                ControlActivity.setClientID(deviceId);
                //publish(pubTOPIC);
                if (mImgTogContolFan.isChecked()) {

                    mBtnFanULow.setChecked(false);
                    mBtnFanLow.setChecked(true);
                    mBtnFanMid.setChecked(false);
                    mBtnFanHigh.setChecked(false);

                    String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"FAN\", \"COMMAND\":\"LOW\"}");
                    ControlActivity.publish(fanTOPIC, PAYLOAD);
                } else if (!mImgTogContolFan.isChecked()) {
                    mBtnFanULow.setChecked(false);
                    mBtnFanLow.setChecked(false);
                    mBtnFanMid.setChecked(false);
                    mBtnFanHigh.setChecked(false);
                    mBtnFanSpin.setChecked(false);
                    String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"FAN\", \"COMMAND\":\"OFF\"}");
                    ControlActivity.publish(fanTOPIC, PAYLOAD);
                }
            }
        });

        mBtnFanULow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "FAN";
                ControlActivity.setClientID(deviceId);

                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);

                mImgTogContolFan.setChecked(true);

                publishFan(mBtnFanULow, "ULOW");
            }
        });

        mBtnFanLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "FAN";
                ControlActivity.setClientID(deviceId);

                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);

                mImgTogContolFan.setChecked(true);

                publishFan(mBtnFanLow, "LOW");
            }
        });
        mBtnFanMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "FAN";
                ControlActivity.setClientID(deviceId);

                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);

                mImgTogContolFan.setChecked(true);

                publishFan(mBtnFanMid, "MIDDLE");
            }
        });
        mBtnFanHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "FAN";
                ControlActivity.setClientID(deviceId);

                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);

                mImgTogContolFan.setChecked(true);

                publishFan(mBtnFanHigh, "HIGH");
            }
        });
        mBtnFanSpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "FAN";
                ControlActivity.setClientID(deviceId);

                if (mImgTogContolFan.isChecked()) {
                    if (mBtnFanSpin.isChecked()) {
                        String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"FAN\", \"COMMAND\":\"SWING\"}");
                        ControlActivity.publish(fanTOPIC, PAYLOAD);

                    } else if (!mBtnFanSpin.isChecked()) {
                        String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"FAN\", \"COMMAND\":\"SWING\"}");
                        ControlActivity.publish(fanTOPIC, PAYLOAD);
                    }
                }
            }
        });
    }

    public static void publishFan(ToggleButton BTN, String status) {
        String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"FAN\", \"COMMAND\":\"%s\"}", status);
        ControlActivity.publish(fanTOPIC, PAYLOAD);
    }

    public static void changeStatus(String DEVICE, JSONObject subObject) throws Exception {
        String STATUS = subObject.getString("COMMAND");

        switch (STATUS) {
            case "ULOW":
            case "ULOW/SW":
                mImgTogContolFan.setChecked(true);
                mBtnFanULow.setChecked(true);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);
                break;
            case "LOW":
            case "LOW/SW":
                mImgTogContolFan.setChecked(true);
                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(true);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);
                break;
            case "MIDDLE":
            case "MIDDLE/SW":
                mImgTogContolFan.setChecked(true);
                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(true);
                mBtnFanHigh.setChecked(false);
                break;
            case "HIGH":
            case "HIGH/SW":
                mImgTogContolFan.setChecked(true);
                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(true);
                break;
            case "OFF":
                mImgTogContolFan.setChecked(false);
                mBtnFanULow.setChecked(false);
                mBtnFanLow.setChecked(false);
                mBtnFanMid.setChecked(false);
                mBtnFanHigh.setChecked(false);
                mBtnFanSpin.setChecked(false);
                break;
            case "SWING":
                if (!mBtnFanSpin.isChecked()) {
                    mBtnFanSpin.setChecked(false);
                } else {
                    mBtnFanSpin.setChecked(true);
                }
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
