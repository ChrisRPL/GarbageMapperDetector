package org.pytorch.demo.objectdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

public class ProviderLocationTracker  implements LocationListener, LocationTracker {
    private static final long MIN_UPDATE_DISTANCE = 1;

    // The minimum time between updates in milliseconds
    private static final long MIN_UPDATE_TIME = 1000;

    private LocationManager lm;

    public enum ProviderType{
        NETWORK,
        GPS
    };
    private String provider;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;

    private LocationUpdateListener listener;

    public ProviderLocationTracker(Context context, ProviderType type) {
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        if(isRunning){
            //Already running, do nothing
            return;
        }

        //The provider is on, so start getting updates.  Update current location
        isRunning = true;
        lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        lastLocation = null;
        lastTime = 0;
    }

    @Override
    public void start(LocationUpdateListener update) {
        start();
        listener = update;
    }

    @Override
    public void stop() {
        if(isRunning){
            lm.removeUpdates(this);
            isRunning = false;
            listener = null;
        }
    }

    @Override
    public boolean hasLocation() {
        if(lastLocation == null){
            return false;
        }
        return System.currentTimeMillis() - lastTime <= 5 * MIN_UPDATE_TIME; //stale
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean hasPossiblyStaleLocation() {
        if(lastLocation != null){
            return true;
        }
        return lm.getLastKnownLocation(provider)!= null;
    }

    @Override
    public Location getLocation() {
        if(lastLocation == null){
            return null;
        }
        if(System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME){
            return null; //stale
        }
        Log.i("location return", lastLocation.getAccuracy() + " accuracy");
        return lastLocation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Location getPossiblyStaleLocation() {
        if(lastLocation != null){
            return lastLocation;
        }
        return lm.getLastKnownLocation(provider);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, location, now);
        }
        lastLocation = location;
        lastTime = now;
        Log.i("location changed", location.getAccuracy() + " accuracy");
    }
}
