package com.childsafe.auth.Workers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.Utils.TimeUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ChildStatusMonitorWorker extends Worker {
    private static final String TAG = ChildStatusMonitorWorker.class.getSimpleName();
    private ServerAdapter mServer;
    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public ChildStatusMonitorWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mServer=new ServerAdapter();

    }

    @NonNull
    @Override
    public Worker.Result doWork() {
        try{
            mServer.getCurrentUser().addOnCompleteListener((@NonNull Task<User> task) -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult();
                        if (user.getRole().equals("Child") && user.getStatus().equals("Lost"))
                        {
                            Log.i(TAG, "User is lost!");
                            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                throw new RuntimeException("location access denied");
                            }
                            else
                            {
                                SettingsClient mSettingsClient = LocationServices.getSettingsClient(getApplicationContext());
                                LocationCallback mLocationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult result) {
                                        super.onLocationResult(result);
                                        Location location = result.getLastLocation();
                                        if(location!=null)
                                        {

                                            String formattedDate = TimeUtil.getCurrentTime();
                                            mServer.addGPSLocation(user.getUid(), formattedDate, Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                                        }

                                    }

                                    //Location Meaning that all relevant information is available
                                    @Override
                                    public void onLocationAvailability(LocationAvailability availability) {
                                        //boolean isLocation = availability.isLocationAvailable();
                                    }
                                };

                                //build one time location request
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setNumUpdates(1);
                                mLocationRequest.setInterval(1000);
                                mLocationRequest.setFastestInterval(1000);
                                mLocationRequest.setSmallestDisplacement(0.0F);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                                builder.addLocationRequest(mLocationRequest);

                                LocationSettingsRequest mLocationSettingsRequest = builder.build();

                                Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
                                locationResponse.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                                    @Override
                                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                        Log.e("Response", "Successful acquisition of location information!!");
                                        //
                                        if (getApplicationContext().checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                                    }
                                });
                                //When the location information is not set and acquired, callback
                                locationResponse.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        int statusCode = ((ApiException) e).getStatusCode();
                                        switch (statusCode) {
                                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                Log.e("onFailure", "Location environment check");
                                                break;
                                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                String errorMessage = "Check location setting";
                                                Log.e("onFailure", errorMessage);
                                        }
                                    }
                                });

                            }
                        }
                        else
                        {
                            Log.i(TAG, "User is safe!");
                        }

                    }
                    else
                    {
                        throw new RuntimeException("failed to connect to server");
                    }
            });
            return Result.success();
        }
        catch(Throwable throwable)
        {
            Log.e(TAG,"failed to connect to server");
            return Result.failure();
        }
    }

}
