package com.example.collpasingtest.GPS;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GPSLocation implements LocationListener{

    private Context context;
    private LocationManager locMgr;
    private long minTime = 1000 * 1; // ms * s
    private float minDist = 1f; // m
    public final static int GPS_ACCESS_LOCATION = 1000;
    public final static int GPS_ACCESS_COARSE_LOCATION = 1001;
    private LocationObserver observer = null;

    public GPSLocation(Context context, LocationObserver observer) {
        this.context = context;
        locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.observer = observer;
    }

    public void listenLocation(){
        try{
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist, this);
            locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDist, this);
        } catch ( SecurityException se){
            se.printStackTrace();
        }

    }

    public void denyLocation(){
        locMgr.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        float accuracy = location.getAccuracy();

        String msg = lng + "\n" + lat + "\n" + accuracy;
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        observer.update(lng, lat);
        Log.d("GPSLocation", "Location - Lng : " + lng + " Lat : " +  lat);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extra) {
        Log.d("GPSLocation", "Status Change : " + provider + " status : " + status + " etc : " + extra);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("GPSLocation", "Provider Enabled : " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("GPSLocation", "Provider Disabled : " + s);
    }
}
