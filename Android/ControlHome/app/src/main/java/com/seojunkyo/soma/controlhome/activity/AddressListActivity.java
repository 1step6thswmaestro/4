package com.seojunkyo.soma.controlhome.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.dialog.DialogAddressActivity;
import com.seojunkyo.soma.controlhome.dialog.DialogMqttConnectActivity;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.AddressList;
import com.seojunkyo.soma.controlhome.util.AddressListAdapter;

import java.util.ArrayList;

public class AddressListActivity extends CONTROLHOMEActivity {

    public static ArrayList<AddressList> items;
    public static AddressListAdapter adapter;
    public static ListView mListView;

    private static final String TAG = AddressListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        initAcitivy();
        setLayout();
    }

    private Button mFloatingButton;

    @Override
    public void initAcitivy() {
        mListView = (ListView) findViewById(R.id.mListView);
        mFloatingButton = (Button) findViewById(R.id.mFloatingActionButton);
        items = new ArrayList<AddressList>();

        items.add(new AddressList("MY HOME", "swhomegateway.dyndns.org"));
        items.add(new AddressList("SW_MAESTRO", "172.16.100.62"));
        items.add(new AddressList("MAC", "192.168.0.72"));
        adapter = new AddressListAdapter(getLayoutInflater(), items);
        mListView.setAdapter(adapter);
    }

    @Override
    public void setLayout() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogMqttConnectActivity.address = items.get(position).getAddress();
                startActivity(new Intent(AddressListActivity.this, DialogMqttConnectActivity.class));
            }
        });
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddressListActivity.this, DialogAddressActivity.class));
            }
        });
    }
}