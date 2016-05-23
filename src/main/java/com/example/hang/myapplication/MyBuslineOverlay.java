package com.example.hang.myapplication;


import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;

import overlayutil.BusLineOverlay;

class MyBuslineOverlay extends BusLineOverlay {

    public MyBuslineOverlay(BaiduMap arg0) {

        super(arg0);
    }

    /**
     * 站点点击事件
     */
    @Override
    public boolean onBusStationClick(int arg0) {
        MarkerOptions options = (MarkerOptions) getOverlayOptions().get(arg0);
        MainActivity.mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(options.getPosition()));
        return true;
    }



}
