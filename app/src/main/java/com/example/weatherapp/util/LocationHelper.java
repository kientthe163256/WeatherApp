package com.example.weatherapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.weatherapp.MainActivity;
import com.example.weatherapp.model.AppLocation;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationHelper {

    private final Context context;
    private final Activity activity;

    public LocationHelper(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    public void checkGPS(OnSuccessListener<Location> successListener,
        OnFailureListener failureListener) {
        long timeInterval = 1000;
        LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, timeInterval).setWaitForAccurateLocation(true).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(
            locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
            .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                // when GPS is already on
                task.getResult(ApiException.class);
                getCurrentLocation(successListener, failureListener);
            } catch (ApiException e) {
                if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        // try to ask user turn on GPS
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(activity,
                            DefaultConfig.REQUEST_CHECK_SETTING);
                    } catch (SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void getCurrentLocation(OnSuccessListener<Location> successListener,
        OnFailureListener failureListener) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "Please grant location permissions", Toast.LENGTH_SHORT)
                .show();
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            activity);
        // get current location
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnCompleteListener(
                task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        successListener.onSuccess(location);
                    } else {
                        failureListener.onFailure(new Exception("Cannot get current location"));
                    }
                });
    }

    public void checkLocationPermission(OnSuccessListener<Location> successListener,
        OnFailureListener failureListener) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            checkGPS(successListener, failureListener);
        } else {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, DefaultConfig.REQUEST_CODE);
        }
    }

}
