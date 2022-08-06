package com.example.manhuntapp2;

/**
 * Maps page which displays a map centered on the person the user wants to locate
 * Inner async class updates user location and gets most up to date friend location
 * <p>
 * based on GoogleMapsJavaAndroidApp
 *
 * @author Devin Grant-Miles
 */

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        OnSuccessListener<LocationSettingsResponse>, OnFailureListener {
    public static final int RESOLVE_SETTINGS_REQUEST = 1;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient locationClient;
    private GoogleMap map;

    private String userName;
    private LatLng userLatLng;

    private String friendName;
    private String friendLat;
    private String friendLng;
    private LatLng friendLocation;
    private Marker friendMarker;

    private LocationCallbackHandler locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // obtain the SupportMapFragment and get notified when the map
        // is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get the extra variable sent with intent
        Bundle extras = getIntent().getExtras();
        friendName = extras.getString("user");
        friendLat = extras.getString("lat");
        friendLng = extras.getString("lng");
        userName = extras.getString("userName");

        //convert string lat and long to double
        double dLat = Double.parseDouble(friendLat);
        double dLng = Double.parseDouble(friendLng);

        //create friend location
        friendLocation = new LatLng(dLat, dLng);

        // prepare for regular location updates
        locationRequest = LocationRequest.create()
                .setInterval(10000) // desired update every 10s
                .setFastestInterval(5000) // possible every 5s
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder
                = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest settingsRequest = builder.build();
        SettingsClient settingsClient
                = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> checkSettingsTask
                = settingsClient.checkLocationSettings(settingsRequest);
        checkSettingsTask.addOnSuccessListener(this, this);
        checkSettingsTask.addOnFailureListener(this, this);
        locationClient
                = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallbackHandler();
    }

    @Override
    public void onResume() {
        super.onResume();
        locationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        locationClient.removeLocationUpdates(locationCallback);
    }

    //method for moving camera and replacing marker as friend location updates are received
    public void moveCamera() {
        friendMarker.remove();
        friendMarker = map.addMarker(new MarkerOptions().position(friendLocation)
                .title(friendName)
                .icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation,
                16.0f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setTrafficEnabled(true);
        UiSettings settings = map.getUiSettings();
        settings.setZoomGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);

        //set marker and camera on friend location
        friendMarker = map.addMarker(new MarkerOptions().position(friendLocation)
                .title(friendName)
                .icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation,
                16.0f));
    }

    public void onSuccess
            (LocationSettingsResponse locationSettingsResponse) {
        locationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper());
    }

    public void onFailure(@NonNull Exception e) {
        if (e instanceof ResolvableApiException) {
            // Location settings are not satisfied, but this can be
            // fixed by showing the user a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                ResolvableApiException resolvable
                        = (ResolvableApiException) e;
                resolvable.startResolutionForResult(this,
                        RESOLVE_SETTINGS_REQUEST);
            } catch (IntentSender.SendIntentException sie) {
                Log.w(MapsActivity.class.getName(),
                        "Unable to send dialog intent: " + sie);
            }
        }
    }

    //called when location changes
    private class LocationCallbackHandler extends LocationCallback {
        public void onLocationAvailability(LocationAvailability
                                                   locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            Log.i(MapsActivity.class.getName(),
                    "Location availability changed");
        }

        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                Log.w(MapsActivity.class.getName(),
                        "Received null location result");
                return;
            }
            Location mostRecent = locationResult.getLastLocation();
            if (map != null) {
                userLatLng = new LatLng(mostRecent.getLatitude(),
                        mostRecent.getLongitude());

                //call the async task to send the latLng value to server and get a location update from server for friend
                RestfulUpdateLocationsTask2 task = new RestfulUpdateLocationsTask2();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "http://10.0.2.2:8080/ManhuntWebServiceFinal/webresources/manhunt/"
                                + userName + "/" + userLatLng.latitude + "/" + userLatLng.longitude + "/" + friendName);

                //move camera now that friend location is updated
                moveCamera();

                Log.i(MapsActivity.class.getName(),
                        "Received user location at " + userLatLng);

            } else {
                Log.w(MapsActivity.class.getName(),
                        "Receiving locations but maps not yet available");
            }
        }
    }

    //inner async class for updating user location and getting updated friend location
    private class RestfulUpdateLocationsTask2 extends AsyncTask<String, Void, String> {

        //send GET request and parse XML response
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return "No URL provided";
            }
            try {
                URL url = new URL(params[0]);//users DB URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000); // 3000ms
                conn.setConnectTimeout(3000); // 3000ms
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader
                            (new InputStreamReader(conn.getInputStream()));
                    StringBuilder xmlResponse = new StringBuilder();
                    String line = br.readLine();
                    while (line != null) {
                        xmlResponse.append(line);
                        line = br.readLine();
                    }
                    br.close();
                    conn.disconnect();

                    if (xmlResponse.length() == 0) {
                        return "No results found";
                    }

                    int userIndex = xmlResponse.indexOf("<users>");

                    int latStartIndex
                            = xmlResponse.indexOf("<lat>", userIndex) + 5;
                    int latEndIndex
                            = xmlResponse.indexOf("</", latStartIndex);
                    String lat = xmlResponse.substring(latStartIndex,
                                    latEndIndex);

                    int lngStartIndex
                            = xmlResponse.indexOf("<lng>", userIndex) + 5;
                    int lngEndIndex
                            = xmlResponse.indexOf("</", lngStartIndex);
                    String lng = xmlResponse.substring(lngStartIndex,
                                    lngEndIndex);

                    //set lat and long for friend
                    friendLat = lat;
                    friendLng = lng;

                    //parse sting lat long to double
                    double dLat = Double.parseDouble(friendLat);
                    double dLng = Double.parseDouble(friendLng);

                    //set new friend location
                    friendLocation = new LatLng(dLat, dLng);

                } else
                    System.out.println("HTTP Response code " + responseCode);
            } catch (MalformedURLException e) {
                Log.e("RestfulSearchLookupTask", "Malformed URL: " + e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("RestfulSearchLookupTask", "IOException: " + e);
                e.printStackTrace();
            }
            return "Error during HTTP request to url " + params[0];
        }
    }
}