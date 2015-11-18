package com.seojunkyo.soma.controlhome.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;

import org.json.JSONObject;


/**
 * Created by seojunkyo on 15. 10. 27..
 */
public class DashboardActivity extends CONTROLHOMEActivity {

    private String result;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initAcitivy();
        setLayout();
    }

    public static EditText mEditTemp;
    public static EditText mEditHumi;

    @Override
    public void initAcitivy() {
        result = "";
        mEditTemp = (EditText) findViewById(R.id.edit_temprature);
        mEditHumi = (EditText) findViewById(R.id.edit_humidity);
    }

    @Override
    public void setLayout() {

    }

    public static void changeStatus(String payload) throws Exception {
        JSONObject subObject = new JSONObject(payload);
        String TYPE = subObject.getString("TYPE");
        String VALUE = "";
        switch (TYPE) {
            case "TEMP":
                VALUE = subObject.getString("VALUE");
                mEditTemp.setText(VALUE);
                break;
            case "HUM":
                VALUE = subObject.getString("VALUE");
                mEditHumi.setText(VALUE);
                break;
        }
    }
}
