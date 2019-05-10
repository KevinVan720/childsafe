package com.childsafe.auth.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.auth.R;
import com.childsafe.auth.Utils.TimeUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GoogleMapFragment extends MainBaseFragment {
    private GoogleMap googleMap;
    private MapView mMapView;
    private TextView losttime;
    private TextView markertime;
    private ArrayList<String> timestamps;
    private ArrayList<LatLng> points;
    private Handler mHandler;
    private Runnable mHandlerTask;
    private final static int INTERVAL = 15 * 60 * 1000; //update every 15 minutes
    private int mode;
    private int mapCounter;
    private int darkline;
    private int lightline;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, parent, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mode = GoogleMap.MAP_TYPE_NORMAL;
            }
        });

        losttime=v.findViewById(R.id.lost_time);
        markertime=v.findViewById(R.id.marker_time);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        timestamps=new ArrayList<>();
        points=new ArrayList<>();
        darkline= ContextCompat.getColor(getContext(), R.color.textDark);
        lightline= ContextCompat.getColor(getContext(), R.color.colorAccent);

        FloatingActionButton fabTop = getActivity().findViewById(R.id.fabTop);
        fabTop.setOnClickListener((View v) -> {
            if (mode == GoogleMap.MAP_TYPE_NORMAL) {
                mode = GoogleMap.MAP_TYPE_HYBRID;
            } else {
                mode = GoogleMap.MAP_TYPE_NORMAL;
            }
            googleMap.setMapType(mode);
            if(points.size()>0)
            {
                redrawLine(points.size());
            }

        });
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener((View v) -> {
            if (points.size() > 0) {
                mapCounter=1;
                Handler redrawHandler= new Handler();
                Runnable redrawTask = new Runnable() {
                    @Override
                    public void run() {
                        if(getActivity()!=null && isAdded()) {
                            if (mapCounter <= points.size()) {
                                redrawLine(mapCounter);
                                Long delay;
                                if (mapCounter == points.size()) {
                                    delay = new Long(1000);
                                } else {
                                    delay = TimeUtil.getTimeDiff(timestamps.get(mapCounter - 1), timestamps.get(mapCounter));
                                }

                                mapCounter++;
                                redrawHandler.postDelayed(this, delay);
                            } else {
                                redrawHandler.removeCallbacks(this);
                                mapCounter = 0;
                            }
                        }
                    }
                };
                redrawHandler.post(redrawTask);
            }
            else {
                Toast.makeText(getContext(), R.string.no_GPS_available, Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fabBack = getActivity().findViewById(R.id.fabBack);
        fabBack.setOnClickListener((View v) -> {
            onStop();
            getActivity().onBackPressed();

        });

        updateGoogleMapLocations();
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        mHandler.removeCallbacks(mHandlerTask);
    }

    private void updateGoogleMapLocations() {
        mHandler = new Handler();
        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                if(getActivity()!=null && isAdded())
                {
                    Long seconds=TimeUtil.getTimeDiff(currentUser.getLastTime(), TimeUtil.getCurrentTime());
                    Long days=seconds/(3600*24);
                    seconds-=days*(3600*24);
                    Long hours = seconds / 3600;
                    seconds -= (hours * 3600);
                    Long minutes = seconds / 60;
                    String timediff="";
                    Log.i("hour",((Long)hours).toString());
                    Log.i("min",((Long)minutes).toString());
                    if(days>0)
                    {
                        timediff+=days.toString()+getText(R.string.time_day)+" ";
                    }
                    if(hours>0)
                    {
                        timediff+=hours.toString()+getText(R.string.time_hour)+" ";
                    }
                    timediff+=minutes.toString()+getText(R.string.time_minute);
                    losttime.setText(getText(R.string.child_status_lost).toString() +" "+ getText(R.string.for_time).toString()+" "+timediff);
                    mServer.getTimeStampedGPSData(currentUser.getUid()).addOnCompleteListener((@NonNull Task<ArrayList<ArrayList<String>>> task) -> {
                        if (task.isSuccessful()) {
                            ArrayList<ArrayList<String>> rst=task.getResult();
                            points.clear();
                            timestamps.clear();

                            for (int i=0; i<rst.size(); i++)
                            {
                                timestamps.add(rst.get(i).get(0));
                                Double lat = Double.valueOf(rst.get(i).get(1));
                                Double lng = Double.valueOf(rst.get(i).get(2));
                                //Log.i("parent", parent.getEmail());
                                LatLng loc = new LatLng(lat, lng);
                                points.add(loc);
                            }
                            if (points.size() > 0) {
                                redrawLine(points.size());

                            } else {
                                Toast.makeText(getContext(), R.string.no_GPS_available, Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Toast.makeText(getContext(), R.string.no_GPS_available, Toast.LENGTH_LONG).show();
                        }
                    });
                    mHandler.postDelayed(this, INTERVAL);
                }
                else
                {
                    mHandler.removeCallbacks(mHandlerTask);
                }
            }
        };
        mHandler.post(mHandlerTask);

    }

    private void redrawLine(int index) {

        googleMap.clear();  //clears all Markers and Polylines

        PolylineOptions options=new PolylineOptions().width(10).geodesic(true);
        for (int i = 0; i < index; i++) {
            LatLng point = points.get(i);
            options.add(point);
        }

        Polyline line=googleMap.addPolyline(options); //add Polyline
        if (mode == GoogleMap.MAP_TYPE_NORMAL) {
            line.setColor(darkline);
        } else {
            line.setColor(lightline);
        }
        line.setStartCap(new CustomCap(
                BitmapDescriptorFactory.fromResource(R.mipmap.ic_circle), 50));
        line.setJointType(JointType.ROUND);


        MarkerOptions markoptions = new MarkerOptions();
        LatLng latLng = points.get(index-1);
        markoptions.position(latLng);
        markoptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        //markoptions.title(TimeUtil.toReadableTime(TimeUtil.toLocalDateTime(timestamps.get(index-1))));
        Marker marker = googleMap.addMarker(markoptions);
        markertime.setVisibility(View.VISIBLE);
        markertime.setText(getText(R.string.marker_time)+" "+TimeUtil.toReadableTime(TimeUtil.toLocalDateTime(timestamps.get(index-1))));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
    }


}
