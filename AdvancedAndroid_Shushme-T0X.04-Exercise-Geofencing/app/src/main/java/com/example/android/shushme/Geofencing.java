package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    // Constants
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;
    private GoogleApiClient mClient;

    public Geofencing(Context context, GoogleApiClient client) {
        mContext = context;
        mClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    // Registers the list of Geofences specified in mGeofenceList with Google Place Service
    public void registerAllGeofences() {
        // Check that the API client is connected and that the list has Geofences in it
        if (mClient == null || !mClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {

        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    //Unregister all the Geofences created by the app from Google Place Services
    public void unRegisterAllGeofences() {
        if (mClient == null || !mClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(mClient, getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            Log.e(TAG, securityException.getMessage());
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        //Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@Nullable Result result) {
        Log.e(TAG, String.format("Error adding/removing genfence : %s", result.getStatus().toString()));
    }

    // Completed (2) Inside Geofencing, implement a public method called updateGeofencesList that
    public void updateGeofencesList(PlaceBuffer places) {
        mGeofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) return;
        for (Place place : places) {
            //Read the place information from the DB cursor
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder().setRequestId(placeUID).setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build();
            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest GeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}
