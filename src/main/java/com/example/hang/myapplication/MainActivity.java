package com.example.hang.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import overlayutil.BusLineOverlay;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {
    private EditText cityEt;
    private EditText buslineEt;
    private Button searchBtn;
    private Button nextlineBtn;

    private String city;// 城市
    private String busline;// 公交路线
    private List<String> buslineIdList;// 存储公交线路的uid
    private int buslineIndex = 0;// 标记第几个站点
    private BusLineOverlay buslineOverlay; //用于传输busLinereult
    private PoiSearch poiSearch;
    private BusLineSearch busLineSearch;
    public static MapView mMapView = null; //声明MapView对象
    public static BaiduMap mBaiduMap;

    LinearLayout linearLayout; //被隐藏的布局，公交查询布局
    public static String Mycity;//所在的城市；
    //定位
    public LocationClient mLocClient = null;
    InitLocation initLocation = new InitLocation();

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
        //这个地方是右下角的邮件图标按钮，app_bar_main.xml
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //左侧滑动栏按钮图标
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //左侧滑动栏 菜单选项监听
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        nextlineBtn = (Button) findViewById(R.id.nextline_btn);
        searchBtn.setOnClickListener(this);
        nextlineBtn.setOnClickListener(this);
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
            case R.id.nextline_btn:
                //再次查询
                searchBusline();
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
                overlay.setData(busLineResult,MainActivity.this);//设置公交线数据
                overlay.addToMap();// 将overlay添加到地图上
                overlay.zoomToSpan();// 缩放地图，使所有overlay都在合适的视野范围内
                mBaiduMap.setOnMarkerClickListener(overlay);
                //传递参数到MyBuslineOverlay,该方法继承自 BusLineOverlay
                buslineOverlay.setData(busLineResult,MainActivity.this);
                // 公交线路名称
                Toast.makeText(MainActivity.this,
                        busLineResult.getBusLineName(), Toast.LENGTH_SHORT);
                Log.d("hello", "名字" + busLineResult.getBusLineName());
                Log.d("hello", "首班车" + busLineResult.getStartTime());
                Log.d("hello", "末班车" + busLineResult.getEndTime());
                Log.d("hello", "分段信息" + busLineResult.getSteps());
              //  java.util.List<BusLineResult.BusStation>	getStations()
              //  获取所有公交站点信息
                for(int i = 0; i< busLineResult.getStations().size();i++){
                    Log.d("hello", "公交信息"+ busLineResult.getStations().get(i).getTitle());
                }
            }
        }
    };
}

