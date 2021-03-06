package com.tradesomev4.tradesomev4.BackgroundProcesses;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tradesomev4.tradesomev4.R;
import com.tradesomev4.tradesomev4.m_Helpers.SnackbarWrapper;
import com.tradesomev4.tradesomev4.m_Model.AuctionHistory;

import java.util.ArrayList;

/**
 * Created by Jorge Benigno Pante, Joshua Alarcon, Charles Torrente on 10/2/2016.
 * File Name: LocationService.java
 * File Path: Tradesomev4\app\src\main\java\com\tradesomev4\tradesomev4\BackgroundProcesses\LocationService.java
 * Description: Get the current user's Physical Location in Coordinates every Ten Thousand Milliseconds then Update Firebase Realtime Database..
 */

public class LocationService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final String DEBUG_TAG =  "DEBUG_TAG";
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    ArrayList<AuctionHistory>auctions;


    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            if(firebaseUser != null){
                Log.d(DEBUG_TAG, "LATITUDE_SERVICE: " + mLastLocation.getLatitude());
                Log.d(DEBUG_TAG, "LONGITUDE_SERVICE: " + mLastLocation.getLongitude());
                databaseReference.child("users").child(firebaseUser.getUid()).child("latitude").setValue(mLastLocation.getLatitude());
                databaseReference.child("users").child(firebaseUser.getUid()).child("longitude").setValue(mLastLocation.getLongitude());

                for(int i = 0; i < auctions.size(); i++){
                    databaseReference.child("auction").child(auctions.get(i).getAuctionId()).child("latitude").setValue(mLastLocation.getLatitude());
                    databaseReference.child("auction").child(auctions.get(i).getAuctionId()).child("longitude").setValue(mLastLocation.getLongitude());
                }
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(DEBUG_TAG, "onProviderDisabled: " + provider);
            showGpsDiabledDialog();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(DEBUG_TAG, "onProviderEnabled: " + provider);
            gpsRestored();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(DEBUG_TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(DEBUG_TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    public void gpsRestored(){String str = "@ GPS Active";
        SpannableString tmp = new SpannableString(str);

        int index = str.indexOf('@');
        Drawable icon = getApplicationContext().getResources().getDrawable(R.drawable.ic_location_on_white_24dp);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);

        tmp.setSpan(span, index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);


        SnackbarWrapper.make(getApplicationContext(),
                tmp, 3000).show();
    }

    public void showGpsDiabledDialog(){
        String str = "@ GPS Required";
        SpannableString tmp = new SpannableString(str);

        int index = str.indexOf('@');
        Drawable icon = getApplicationContext().getResources().getDrawable(R.drawable.ic_location_off_white_24dp);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);

        tmp.setSpan(span, index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        SnackbarWrapper.make(getApplicationContext(), tmp, 3000).show();
    }

    public void init(){
        Log.e(DEBUG_TAG, "onCreate");

        try{
            if(FirebaseApp.getInstance() == null){
                Context context = getApplicationContext();
                FirebaseApp.initializeApp(context, FirebaseOptions.fromResource(context));
            }
        }catch (IllegalStateException e){
            Context context = getApplicationContext();
            FirebaseApp.initializeApp(context, FirebaseOptions.fromResource(context));
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        listenToMyAuctions();

        Log.e(DEBUG_TAG, "onCreate2");

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(DEBUG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(DEBUG_TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(DEBUG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(DEBUG_TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    public void listenToMyAuctions(){
        auctions = new ArrayList<>();
        try{
            if(firebaseUser.getUid() != null){
                databaseReference.child("auctionHistory").child(firebaseUser.getUid()).limitToLast(10).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        auctions.add(dataSnapshot.getValue(AuctionHistory.class));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        init();
        //listenToMyAuctions();
    }

    @Override
    public void onDestroy() {
        Log.e(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(DEBUG_TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
