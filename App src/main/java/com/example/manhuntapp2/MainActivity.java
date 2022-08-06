package com.example.manhuntapp2;


/**
 * Main page which displays connectivity of google play and location services
 * Also contains list of friends that can be located
 * <p>
 * based on GoogleMapsJavaAndroidApp
 *
 * @author Devin Grant-Miles
 */

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    // values to identify permission request
    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final int PLAY_REQUEST_CODE = 2;
    private TextView playServicesStatusView;
    private TextView locationServicesStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playServicesStatusView = findViewById(R.id.play_services_status);
        locationServicesStatusView = findViewById
                (R.id.location_services_status);

        //get extra message i.e. username sent with intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        //start async task for user list
        TextView userList = findViewById(R.id.userView);
        RestfulUserSubmitTask task = new RestfulUserSubmitTask(userList, userName);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://10.0.2.2:8080/ManhuntWebServiceFinal/webresources/manhunt/" + userName);
    }

    private boolean hasLocationPermission() {
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.request_permission_title)
                        .setMessage(R.string.request_permission_text)
                        .setPositiveButton(
                                R.string.request_permission_positive,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick
                                            (DialogInterface dialogInterface, int i) {
                                        ActivityCompat.requestPermissions
                                                (MainActivity.this, new String[]
                                                                {Manifest.permission.
                                                                        ACCESS_FINE_LOCATION},
                                                        PERMISSION_REQUEST_CODE);
                                    }
                                })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
            return false;
        }
    }

    public void onResume() {
        super.onResume();
        // check whether the device has Google Play services enabled
        GoogleApiAvailability apiAvailability
                = GoogleApiAvailability.getInstance();
        int resultCode
                = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            playServicesStatusView.setText(R.string.label_available);
            Log.i(MainActivity.class.getName(),
                    "Google Play API available");
        } else {
            playServicesStatusView.setText(R.string.label_unavailable);
            Log.w(MainActivity.class.getName(),
                    "Google Play API not available");
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_REQUEST_CODE).show();
            } else
                finish();
        }
        // check whether location services are available
        if (hasLocationPermission()) {
            locationServicesStatusView.setText(R.string.label_available);
            Log.i(MainActivity.class.getName(), "Location API available");
        } else {
            locationServicesStatusView.setText
                    (R.string.label_unavailable);
            Log.w(MainActivity.class.getName(),
                    "Location API not available");
        }
    }

    //called when user clicks a name of a person they want to locate
    public void showMap(View view) {
        //check permissions are granted before starting maps activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else {
            Log.w(MainActivity.class.getName(),
                    "Can't show map without location permission");
        }
    }
}
