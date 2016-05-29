package com.example.hang.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListBusActivity extends AppCompatActivity   implements View.OnClickListener {
    private ListView listView;
    private TextView bustime_view;
    private Button cancle_button;
    //公交信息
    private String s_time, e_time, bus_name;
    private String bus_list[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bus);
        getSupportActionBar().hide();
        acceptInform();//接受数据
        bustime_view = (TextView) findViewById(R.id.Bus_time_view);
        listView = (ListView) findViewById(R.id.Bus_list_view);
        cancle_button = (Button) findViewById(R.id.cancle_betton);
        cancle_button.setOnClickListener(this);
        bustime_view.setText("车  次: " + bus_name + "\n" + "首班车: " + s_time + "\n" + "末班车: " + e_time);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ListBusActivity.this
                , android.R.layout.simple_list_item_1, bus_list);
        listView.setAdapter(arrayAdapter);
    }

    public void acceptInform() {
        Intent intent = getIntent();
        bus_name = intent.getStringExtra("bus_name");
        s_time = intent.getStringExtra("star_ttime");
        e_time = intent.getStringExtra("end_time");
        bus_list = intent.getStringArrayExtra("bus_list");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancle_betton:
                //像按下back按钮一样。
                onBackPressed();
                //如果用户点击BACK键，当前的activity会从栈中弹出并且被销毁。
                break;
        }
    }
}
