package com.seojunkyo.soma.controlhome.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.ControlActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEFragment;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import org.json.JSONObject;

public class HUMIItemFragment extends CONTROLHOMEFragment {

    public static String humiTOPIC;
    private String deviceId;

    public HUMIItemFragment() {
        // Required empty public constructor
    }

    public static HUMIItemFragment newInstance() {
        HUMIItemFragment fragment = new HUMIItemFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_humi_list, container, false);

        initFragment(view);
        setLayout(view);

        return view;
    }

    public static ToggleButton mImgTogContolHUMI;
    public static ToggleButton mBtnHumiLow;
    public static ToggleButton mBtnHumiMid;
    public static ToggleButton mBtnHumiHigh;

    @Override
    public void initFragment(View view) {
        humiTOPIC = "CONTROL";
        mBtnHumiLow = (ToggleButton) view.findViewById(R.id.btn_humi_low);
        mBtnHumiMid = (ToggleButton) view.findViewById(R.id.btn_humi_mid);
        mBtnHumiHigh = (ToggleButton) view.findViewById(R.id.btn_humi_high);

        ControlActivity.mImgCircle1.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle2.setBackgroundResource(R.drawable.img_circle_draw);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_empty);

        mImgTogContolHUMI = (ToggleButton) view.findViewById(R.id.toggle_humi);
    }

    @Override
    public void setLayout(View view) {
        mImgTogContolHUMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "HUMI";
                ControlActivity.setClientID(deviceId);
                //publish(pubTOPIC);
                if (mImgTogContolHUMI.isChecked()) {
                    mImgTogContolHUMI.setChecked(false);
                    mBtnHumiLow.setChecked(true);
                    mBtnHumiMid.setChecked(false);
                    mBtnHumiHigh.setChecked(false);

                    String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"HUMI\", \"COMMAND\":\"PUSH\"}");
                    ControlActivity.publish(humiTOPIC, PAYLOAD);
                } else if (!mImgTogContolHUMI.isChecked()) {
                    mImgTogContolHUMI.setChecked(true);
                    mBtnHumiLow.setChecked(false);
                    mBtnHumiMid.setChecked(false);
                    mBtnHumiHigh.setChecked(false);
                    String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"HUMI\", \"COMMAND\":\"OFF\"}");
                    ControlActivity.publish(humiTOPIC, PAYLOAD);
                }
            }
        });

        mBtnHumiLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "HUMI";
                mImgTogContolHUMI.setChecked(true);

                mBtnHumiLow.setChecked(false);
                mBtnHumiMid.setChecked(false);
                mBtnHumiHigh.setChecked(false);

                ControlActivity.setClientID(deviceId);

                publishHUMI(mBtnHumiLow, "LOW");
            }
        });
        mBtnHumiMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "HUMI";
                mImgTogContolHUMI.setChecked(true);
                mBtnHumiLow.setChecked(false);
                mBtnHumiMid.setChecked(false);
                mBtnHumiHigh.setChecked(false);

                ControlActivity.setClientID(deviceId);

                publishHUMI(mBtnHumiMid, "MIDDLE");
            }
        });
        mBtnHumiHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "HUMI";
                mImgTogContolHUMI.setChecked(true);
                mBtnHumiLow.setChecked(false);
                mBtnHumiMid.setChecked(false);
                mBtnHumiHigh.setChecked(false);

                ControlActivity.setClientID(deviceId);

                publishHUMI(mBtnHumiHigh, "HIGH");
            }
        });
    }

    public static void publishHUMI(ToggleButton BTN, String status) {
        mImgTogContolHUMI.setChecked(true);
        BTN.setChecked(true);
        String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"HUMI\", \"COMMAND\":\"%s\"}", status);
        ControlActivity.publish(humiTOPIC, PAYLOAD);
    }

    public static void changeStatus(String DEVICE, JSONObject subObject) throws Exception {
        String STATUS = subObject.getString("COMMAND");

        switch (STATUS) {
            case "PUSH":
            case "LOW":
                mImgTogContolHUMI.setChecked(true);
                mBtnHumiLow.setChecked(true);
                mBtnHumiMid.setChecked(false);
                mBtnHumiHigh.setChecked(false);
                break;
            case "MIDDLE":
                mImgTogContolHUMI.setChecked(true);
                mBtnHumiMid.setChecked(true);
                mBtnHumiLow.setChecked(false);
                mBtnHumiHigh.setChecked(false);
                break;
            case "HIGH":
                mImgTogContolHUMI.setChecked(true);
                mBtnHumiHigh.setChecked(true);
                mBtnHumiLow.setChecked(false);
                mBtnHumiMid.setChecked(false);
                break;
            case "OFF":
                mImgTogContolHUMI.setChecked(false);
                mBtnHumiHigh.setChecked(false);
                mBtnHumiLow.setChecked(false);
                mBtnHumiMid.setChecked(false);
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
