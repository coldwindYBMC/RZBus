package com.example.hang.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import overlayutil.BusLineOverlay;
import overlayutil.OverlayManager;
import overlayutil.TransitRouteOverlay;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public static final int  SUCCESS_LOADING_BUSINFORM = 2;//加载公交信息成功
    private List<String> suggest;
    private ArrayAdapter<String> sugAdapter = null;
    public static  EditText cityEt;
    private Boolean flagcount = true;
    private TextView popupText = null; // 泡泡view
    private EditText buslineEt;
    private Button searchBtn;
    private Button listlineBtn;
    private Button cancleBtn;
    private Button route_all;
    private Button roult_query;
    private AutoCompleteTextView roult_s_et;
    private AutoCompleteTextView roult_e_et;
    private TextView tranlist_text;
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
    private boolean bustranInit_bl = true;
    private boolean busListinit_bl = true;
    private LinearLayout linearLayout, roult_linearLayout; //被隐藏的布局
    private static Intent intent;//传递消息
    static String Mycity;//所在的城市；
    private RoutePlanSearch routePlanSearch;// 路径规划搜索接口
    //定位
    public LocationClient mLocClient = null;
    InitLocation initLocation = new InitLocation();
    RouteLine route = null;
    // 浏览路线节点相关
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
   static EditText buscity; //公交路线规划的城市
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
     OverlayManager routeOverlay = null;
    private SuggestionSearch mSuggestionSearch = null;
    boolean autoEdit;
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
        //获取被隐藏的linearlayout
        linearLayout = (LinearLayout) findViewById(R.id.Bus_linearLayout);
        //获取隐藏的布局，转乘查询布局
        roult_linearLayout = (LinearLayout) findViewById(R.id.bus_roult_lin);
        // busLineinit();//公交查询布局，可以在用的时候再加载
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        cityEt = (EditText) findViewById(R.id.city_et);
        buscity = (EditText) findViewById(R.id.city_bus);
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
    /**
     * 初始化公交线路查询操作
     */
    private void busLineinit() {

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

    /**
     * 转乘线路查询初始化
     */
    private void bustranInit(){
        roult_s_et = (AutoCompleteTextView)findViewById(R.id.start_et);
        roult_e_et = (AutoCompleteTextView)findViewById(R.id.end_et);

        sugAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line);
        roult_e_et.setAdapter(sugAdapter);
        roult_e_et.setThreshold(1);

        roult_query = (Button)findViewById(R.id.roult_list_btn);
        cancleBtn = (Button)findViewById(R.id.roult_cancle_btn);
        route_all = (Button)findViewById(R.id.all);
        tranlist_text= (TextView)findViewById(R.id.trans_text);
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        route_all.setVisibility(View.INVISIBLE);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        cancleBtn.setOnClickListener(this);
        roult_query.setOnClickListener(this);
        //创建公交线路规划检索实例；
        routePlanSearch = RoutePlanSearch.newInstance();
        //设置公交线路规划检索监听者；
        routePlanSearch
                .setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        poisearchinit();
    }

    /**
     * 为AutoCompleteTextView获取建议列表
     */
    private void poisearchinit(){
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestion);
        roult_s_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                if (cs.length() <= 0) {
                    return;
                }
                //使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city("日照"));
                autoEdit = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        roult_e_et.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                autoEdit = false;
                // 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city(Mycity));

            }
        });
    }
    /**
    * 建议监听，设置AutoCompleteTextView的适配器
    */
    OnGetSuggestionResultListener suggestion= new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult res) {
            if (res == null || res.getAllSuggestions() == null) {
                return;
            }
            suggest = new ArrayList<String>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    suggest.add(info.key);
                }
            }
            sugAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, suggest);
           if(autoEdit) {
               roult_s_et.setAdapter(sugAdapter);
           }
          else {
               roult_e_et.setAdapter(sugAdapter);
           }

            sugAdapter.notifyDataSetChanged();
        }
    };
    /**
     *  开启定位
     */
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
     * 左侧滑动菜单栏按钮监听
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            //保证布局不被其他覆盖
            Log.d("hello",""+linearLayout.getVisibility());
            if(linearLayout.getVisibility() != View.GONE){
                linearLayout.setVisibility(View.GONE);
            }
            //加载布局，只在初次加载
            if(bustranInit_bl ) {
                bustranInit();
                bustranInit_bl = false;
            }
            roult_linearLayout.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_busSearch) {
            //保证布局不被其他覆盖
            if(roult_linearLayout.getVisibility()!=View.GONE){
               roult_linearLayout.setVisibility(View.GONE);
            }
            //只在初次加载
            if(busListinit_bl ) {
                busLineinit(); //公交查询布局，用的时候在加载
                busListinit_bl = false;
            }
            //改布局显示
            linearLayout.setVisibility(View.VISIBLE);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
    * 所有的按钮监听
    *
    */
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
            case R.id.roult_list_btn:
                //换乘路线查询
                PlanNode stNode = PlanNode.withCityNameAndPlaceName(Mycity, roult_s_et.getText().toString());
                PlanNode enNode = PlanNode.withCityNameAndPlaceName(Mycity,roult_e_et.getText().toString());
                routePlanSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stNode)
                        .city(Mycity)
                        .to(enNode));
                break;
            case R.id.roult_cancle_btn:
                //隐藏布局
               roult_linearLayout.setVisibility(View.GONE);
                break;
        }
    }
    /**
     * 以下为公交路信息查询
    *及其监听回调函数
    */
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
                Toast.makeText(MainActivity.this, "检索成功~", Toast.LENGTH_SHORT)
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
                    bus_list_boolean = false;
                    startActivity(intent);
                }
            }
        }
    };
    //以上为公交路线信息
    //异步处理消息，为两个edittwxt设置获取的地理城市。
    static Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg){
            switch (msg.what){
                case MyLocationListener.SUCCESS_LOADING_MYCITY:
                    cityEt.setText(Mycity);
                    buscity.setText(Mycity);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     *公交转乘路线查询
     */
    OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult result) {

            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();

            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                result.getSuggestAddrInfo();
                Log.d("hello",   result.getSuggestAddrInfo()+"");
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                nodeIndex = -1;
                route_all.setVisibility(View.VISIBLE);
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
                route = result.getRouteLines().get(0);
                TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            }
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };
    /**
     * 节点浏览，打印线路规划的路线信息
     */
    public void nodeClick(View v) {
        // 获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        if (route == null || route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }
        // 设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }else if(v.getId() == R.id.all){
            if(flagcount) {
                tranlist_text.setVisibility(View.VISIBLE);
                String tran_list = "#" ;
                //遍历所有节点信息
                for (int i = 0; i < route.getAllStep().size(); i++) {
                    Object step1 = route.getAllStep().get(i);
                    nodeTitle = ((TransitRouteLine.TransitStep) step1).getInstructions();
                    tran_list =  tran_list + nodeTitle+"\n"+"#";
                }
                tranlist_text.setText(tran_list);
                route_all.setText("隐藏文字信息");
                flagcount = false;
            }
         else  {
                tranlist_text.setVisibility(View.GONE);
                flagcount = true;
                route_all.setText("显示文字信息");
            }
           return;
        }
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();

        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();

        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();

        } else if (step instanceof BikingRouteLine.BikingStep) {
            nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
            nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();

        }
        if (nodeLocation == null || nodeTitle == null) {

        }
        // 移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(MainActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
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
        routePlanSearch.destroy();
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
}

