package com.example.pirathap.login;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private Marker marker;
    private double longitude;
    private double latitude;
    GoogleApiClient client;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int RESULT_OK = 1;
    private VideoView mVideoView;
    private double radius = 100;
    private CircleOptions circleOptions;

    private EditText searchText;
    private LocationRequest request;
    private Marker geofenceMarker;
    private GeofencingRequest geoRequest;
    private PendingIntent geofencingPendingIntent;
    private  Circle geofenceLimit;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        searchText = (EditText) view.findViewById(R.id.search_place);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Button btnCamera = (Button) view.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

        Button btnzoom = (Button) view.findViewById(R.id.btn_Zoomin);
        btnzoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (geofenceLimit != null){
                    geofenceLimit.remove();
                }

                radius = radius + 20 ;
                LatLng position = new LatLng(latitude, longitude);

                circleOptions
                        .center(position)
                        .radius(radius);

                geofenceLimit = mMap.addCircle(circleOptions);
            }
        });

        Button btnZout = (Button) view.findViewById(R.id.btn_Zoomout);
        btnZout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (geofenceLimit != null){
                    geofenceLimit.remove();
                }

                radius = radius - 20 ;
                LatLng position = new LatLng(latitude, longitude);

                circleOptions
                        .center(position)
                        .radius(radius);

                geofenceLimit = mMap.addCircle(circleOptions);
            }
        });

        return view;
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            mVideoView.setVideoURI(videoUri);
        }
    }

    private void drawGeofence() {
        if (geofenceLimit != null){
            geofenceLimit.remove();
        }

        LatLng position = new LatLng(latitude, longitude);

        circleOptions = new CircleOptions()
                .center(position)
                .strokeColor(Color.argb(50,255,10,10))
                .fillColor(Color.argb(50,10,255,10))
                .radius(radius);

        geofenceLimit = mMap.addCircle(circleOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng defaultLac = new LatLng(6.915175, 79.972937);
        marker = mMap.addMarker(new MarkerOptions()
                .position(defaultLac)
                .title("Destination")
                .draggable(true));

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLac));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Toast.makeText(getContext(), longitude+ " : "+latitude, Toast.LENGTH_LONG).show();
        }
        drawGeofence();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onStart() {
        client.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        client.disconnect();
        super.onStop();
    }

}