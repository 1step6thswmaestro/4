package com.seojunkyo.soma.controlhome.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;

import java.net.URI;

/**
 * Created by seojunkyo on 15. 10. 27..
 */
public class LoginActivity extends CONTROLHOMEActivity {

    public static Activity sActivityReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initAcitivy();
        setLayout();
    }

    private EditText mEditEmail;
    private EditText mEditPW;
    private ImageButton mBtnLogin;
    private ImageButton mBtnAccount;

    @Override
    public void initAcitivy() {
        mEditEmail = (EditText) findViewById(R.id.login_email);
        mEditPW = (EditText) findViewById(R.id.login_password);
        mBtnLogin = (ImageButton) findViewById(R.id.login_connect);
        mBtnAccount =(ImageButton) findViewById(R.id.btn_create_account);
    }

    @Override
    public void setLayout() {
        mEditEmail.setPadding(200,0,0,90);
        mEditPW.setPadding(200, 0, 0, 70);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AddressListActivity.class));
            }
        });
        mBtnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://ohmyhome.ezupup.cafe24.com:8888/register");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
    }
}
