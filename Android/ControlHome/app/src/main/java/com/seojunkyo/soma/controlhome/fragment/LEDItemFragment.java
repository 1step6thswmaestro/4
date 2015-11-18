package com.seojunkyo.soma.controlhome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.activity.ControlActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEFragment;

import org.json.JSONObject;

public class LEDItemFragment extends CONTROLHOMEFragment {

    public static String ledTOPIC;
    public static String deviceId;

    public LEDItemFragment() {
        // Required empty public constructor
    }

    public static LEDItemFragment newInstance() {
        LEDItemFragment fragment = new LEDItemFragment();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_light_list, container, false);
        setRetainInstance(true);

        initFragment(view);
        setLayout(view);

        return view;
    }

    public static ToggleButton mImgTogContolLED1;
    public static ToggleButton mImgTogContolLED2;
    public static ToggleButton mImgTogContolLED3;
    public static ToggleButton mImgTogContolLED4;

    @Override
    public void initFragment(View view) {

        ledTOPIC = "CONTROL";

        ControlActivity.mImgCircle1.setBackgroundResource(R.drawable.img_circle_draw);
        ControlActivity.mImgCircle2.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_empty);
        ControlActivity.mImgCircle3.setBackgroundResource(R.drawable.img_circle_empty);

        mImgTogContolLED1 = (ToggleButton) view.findViewById(R.id.toggle_LED1);
        mImgTogContolLED2 = (ToggleButton) view.findViewById(R.id.toggle_LED2);
        mImgTogContolLED3 = (ToggleButton) view.findViewById(R.id.toggle_LED3);
        mImgTogContolLED4 = (ToggleButton) view.findViewById(R.id.toggle_LED4);
    }

    @Override
    public void setLayout(View view) {
        mImgTogContolLED1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishLED(mImgTogContolLED1, "LED1");
            }
        });
        mImgTogContolLED2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishLED(mImgTogContolLED2, "LED2");
            }
        });
        mImgTogContolLED3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishLED(mImgTogContolLED3, "LED3");
            }
        });
        mImgTogContolLED4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                ControlActivity.setClientID(deviceId);
                publishLED(mImgTogContolLED4, "LED4");
            }
        });
    }

    public static void publishLED(ToggleButton BTN, String device) {
        if (BTN.isChecked()) {
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"%s\", \"COMMAND\":\"ON\"}", device);
            ControlActivity.publish(ledTOPIC, PAYLOAD);

        } else if (!BTN.isChecked()) {
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"%s\", \"COMMAND\":\"OFF\"}", device);
            ControlActivity.publish(ledTOPIC, PAYLOAD);
        }
    }

    public static void changeStatus(String DEVICE, JSONObject subObject) throws Exception {
        String STATUS = subObject.getString("COMMAND");

        switch (DEVICE) {
            case "LED1":
                if (STATUS.equals("OFF")) {
                    mImgTogContolLED1.setChecked(false);
                } else if (STATUS.equals("ON")) {
                    mImgTogContolLED1.setChecked(true);
                }
                break;
            case "LED2":
                if (STATUS.equals("OFF")) {
                    mImgTogContolLED2.setChecked(false);
                } else if (STATUS.equals("ON")) {
                    mImgTogContolLED2.setChecked(true);
                }
                break;
            case "LED3":
                if (STATUS.equals("OFF")) {
                    mImgTogContolLED3.setChecked(false);
                } else if (STATUS.equals("ON")) {
                    mImgTogContolLED3.setChecked(true);
                }
                break;
            case "LED4":
                if (STATUS.equals("OFF")) {
                    mImgTogContolLED4.setChecked(false);
                } else if (STATUS.equals("ON")) {
                    mImgTogContolLED4.setChecked(true);
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
