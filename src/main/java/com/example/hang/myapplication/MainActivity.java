package com.example.hang.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import overlayutil.BusLineOverlay;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public static final int  SUCCESS_LOADING_BUSINFORM = 2;//加载公交信息成功
    public static  EditText cityEt;
    private EditText buslineEt;
    private Button searchBtn;
    private Button listlineBtn;
    private Button cancleBtn;
    private Boolean bus_list_boolean = false;//判断是否公交信息加载完毕
    private String city;// 城市
    private String busline;// 公交路线
    private List<String> buslineIdList;// 存储公交线路的uid
    private int buslineIndex = 0;// 标记第几个站点
    private BusLineOverlay buslineOverlay; //用于传输busLinereult
    private PoiSearch poiSearch;
    private BusLineSearch busLineSearch;
    public static MapView mMapView = null; //声明MapView对象
    public static BaiduMap mBaiduMap;

    private LinearLayout linearLayout; //被隐藏的布局，公交查询布局。
    private static Intent intent;//传递消息
    static String Mycity;//所在的城市；

    //定位
    public LocationClient mLocClient = null;
    InitLocation initLocation = new InitLocation();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /*在使用SDK各组件之前初始化context信息，传入ApplicationContext
        注意该方法要再setContentView方法之前实现
        在SDK各功能组件使用之前都需要调用SDKInitializer.initialize(getApplicationContext());，因此该方法放在Application的初始化方法中
        */
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //获取linearlayout和其他布局
        linearLayout = (LinearLayout) findViewById(R.id.Bus_linearLayout);
        busLineinit();//公交查询布局
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        LocationStart();//开启定位

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //左侧滑动栏按钮图标
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //左侧滑动栏 菜单选项监听
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    /*onDestroy到 onPause()方法
    * 管理地图生命周期；
    * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        poiSearch.destroy();// 释放检索对象资源
        busLineSearch.destroy();// 释放检索对象资源
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_busSearch) {
            //改布局显示
            linearLayout.setVisibility(View.VISIBLE);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    * 开启定位
    * */
    private void LocationStart() {
        //定位，开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(getApplicationContext());
        //注册定位监听函数
        mLocClient.registerLocationListener(new MyLocationListener());
        initLocation.initLocation(mLocClient);
        //开启定位
        mLocClient.start();
    }

    /**
     * 初始化操作
     */
    private void busLineinit() {
        cityEt = (EditText) findViewById(R.id.city_et);
        buslineEt = (EditText) findViewById(R.id.searchkey_et);
        searchBtn = (Button) findViewById(R.id.busline_search_btn);
        listlineBtn = (Button) findViewById(R.id.listline_btn);
        cancleBtn = (Button) findViewById(R.id.cancle_btn);
        //注册监听
        searchBtn.setOnClickListener(this);
        listlineBtn.setOnClickListener(this);
        cancleBtn.setOnClickListener(this);

        //用存储公交线路的uid
        buslineIdList = new ArrayList<String>();
        // 创建POI检索实例
        poiSearch = PoiSearch.newInstance();
        //设置POI检索监听者；
        poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        //如下是bus的检索监听
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch
                .setOnGetBusLineSearchResultListener(busLineSearchResultListener);
        buslineOverlay = new BusLineOverlay(mBaiduMap);//初始化result


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.busline_search_btn:
                //获取用户输入的city
                city = cityEt.getText().toString();
                busline = buslineEt.getText().toString();
                //发起POI检索，获取相应线路的UID；
                poiSearch.searchInCity((new PoiCitySearchOption()).city(city)
                        .keyword(busline));
                break;
            case R.id.listline_btn:
                //获取用户输入的city
                city = cityEt.getText().toString();
                busline = buslineEt.getText().toString();
                //发起POI检索，获取相应线路的UID；
                poiSearch.searchInCity((new PoiCitySearchOption()).city(city)
                        .keyword(busline));
                intent = new Intent(MainActivity.this,ListBusActivity.class);
                //设置为可以执行发送Intent消息
                bus_list_boolean = true;
                break;
            case R.id.cancle_btn:
                //改布局显示
                linearLayout.setVisibility(View.GONE);
                break;

        }
    }

    private void searchBusline() {
        //公交的站点大于，该路线的长度。
        if (buslineIndex >= buslineIdList.size()) {
            buslineIndex = 0;
        }
        //公交站点 小于这个路线的长度
        if (buslineIndex >= 0 && buslineIndex < buslineIdList.size()
                && buslineIdList.size() > 0) {
            //定义并设置公交信息结果监听者（与POI类似），并发起公交详情检索；
            //如下代码为发起检索代码，定义监听者和设置监听器的方法与POI中的类似
            //参数city和这个keyword路线的UID
            // 公交检索入口
            boolean flag = busLineSearch
                    .searchBusLine((new BusLineSearchOption().city(city)
                            .uid(buslineIdList.get(buslineIndex))));
            //是否成功检索
            if (flag) {
                //把布局隐藏
                linearLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "检索成功~", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "检索失败~", Toast.LENGTH_LONG)
                        .show();
            }
            buslineIndex++;
        }
    }

    /**
     * 创建POI检索结果监听器 poiSearch
     */
    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null
                    || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                Toast.makeText(MainActivity.this, "未找到结果",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
                // 遍历所有poi，找到类型为公交线路的poi
                buslineIdList.clear();
                for (PoiInfo poi : poiResult.getAllPoi()) {
                    if (poi.type == PoiInfo.POITYPE.BUS_LINE
                            || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                        buslineIdList.add(poi.uid);
                    }
                }
                searchBusline();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult arg0) {

        }
    };

    /**
     * 公交信息查询结果监听器， busLineSearch
     */
    OnGetBusLineSearchResultListener busLineSearchResultListener = new OnGetBusLineSearchResultListener() {
        /* 公交信息查询结果回调函数
         * 参数:
         *result - 公交信息查询结果
         */
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult) {
            if (busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {
                mBaiduMap.clear();
                // 用于显示一条公交详情结果的Overlay
                BusLineOverlay overlay = new BusLineOverlay(mBaiduMap);
                overlay.setData(busLineResult, MainActivity.this);//设置公交线数据
                overlay.addToMap();// 将overlay添加到地图上
                overlay.zoomToSpan();// 缩放地图，使所有overlay都在合适的视野范围内
                mBaiduMap.setOnMarkerClickListener(overlay);
                //传递参数到MyBuslineOverlay,该方法继承自 BusLineOverlay
                buslineOverlay.setData(busLineResult, MainActivity.this);
                // 公交线路名称
                Toast.makeText(MainActivity.this,
                        busLineResult.getBusLineName(), Toast.LENGTH_SHORT);

                //把其转化为String类型的数组保存，用于传递给ListBusActivity。
                //并把时间格式获取为相应的时间各式
                String bus_ame = busLineResult.getBusLineName();
                SimpleDateFormat sdf = new SimpleDateFormat("hh：mm"); //设置格式
                String start_time = sdf.format(busLineResult.getStartTime());
                String end_time = sdf.format(busLineResult.getEndTime());
                String busim_arr[] = new String[busLineResult.getStations().size()];
                for(int i = 0; i<busim_arr.length; i++){
                    busim_arr[i] = busLineResult.getStations().get(i).getTitle();
                }
                //
                if(bus_list_boolean) {
                    intent.putExtra("bus_name",bus_ame);
                    intent.putExtra("star_ttime", start_time);
                    intent.putExtra("end_time", end_time);
                    intent.putExtra("bus_list", busim_arr);
                    startActivity(intent);
                }
            }
        }
    };
    static Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg){
            switch (msg.what){
                case MyLocationListener.SUCCESS_LOADING_MYCITY:
                    cityEt.setText(Mycity);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.hang.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.hang.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

