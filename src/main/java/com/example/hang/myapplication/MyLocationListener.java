package com.example.hang.myapplication;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by hang on 2016/5/23.
 */
public class MyLocationListener implements BDLocationListener {
    // 是否首次定位
    private boolean isFirstLoc=true;
    @Override
    public void onReceiveLocation(BDLocation location) {

            // map view 销毁后不在处理新接收的位置
            if (location == null || MainActivity.mMapView == null)
                return;
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            MainActivity.mBaiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                //获取经纬度
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                //这里获得city城市，在MainctityA中使用
              MainActivity.Mycity = location.getCity();
                Log.d("hello",location.getCity()+"Location");
                // 设置地图新中心点
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                //设置缩放度
                MapStatusUpdate u1 = MapStatusUpdateFactory.zoomTo(30);
                MainActivity.mBaiduMap.animateMapStatus(u1);
                //以动画方式更新地图状态，动画耗时 300 ms
                MainActivity.mBaiduMap.animateMapStatus(u);
            }
    }

}