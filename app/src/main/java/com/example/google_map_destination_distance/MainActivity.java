package com.example.google_map_destination_distance;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTING = 1001;
    private LocationRequest locationRequest;

    EditText Destination_Edittext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Destination_Edittext = findViewById(R.id.Destination_Edittext);
        Button Location_Button = findViewById(R.id.Location_Btn);

        locationRequest = LocationRequest.create(); // create a location request
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // set how accurately the location will be provided
        locationRequest.setInterval(5000); // interval at with the location is set
        locationRequest.setFastestInterval(2000);

        Location_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) // checks if the gps is on or not
                    {
                        if(isGPSEnable())
                        {
                            LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);

                                    if(locationResult != null && locationResult.getLocations().size() > 0)
                                    {
                                        int index =  locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        double Latitude = (double)latitude;
                                        double Longitude = (double) longitude;

                                        String source = Double.toString(Latitude) + "," + Double.toString(Longitude);
                                        String destination = Destination_Edittext.getText().toString();
                                        if(TextUtils.isEmpty(destination))
                                        {
                                            Toast.makeText(MainActivity.this, "Please enter your destination!!", Toast.LENGTH_SHORT).show();
                                        }

                                        else
                                        {
                                            get_Destination_Location_on_Map(source, destination);
                                        }
                                    }
                                }
                            }, Looper.getMainLooper());
                        }

                        else // when gps is off
                        {
                            turnOnGPS();
                        }
                    }

                    else
                    {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 44);
                    }
                }
            }
        });
    }

    private void turnOnGPS()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addAllLocationRequests(Collections.singleton(locationRequest)); //used to get the response as gps location of client
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is On!!", Toast.LENGTH_LONG).show();

                }
                catch (ApiException e) // e here is the api exception error
                {
                    switch (e.getStatusCode())
                    {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED : //location setting status code used to get the location setting status(wether its turn on, or the device have location access or not etc)
                            //Resolution Required is used to check the location being turned on

                            try
                            {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e; // used to get the api exception error
                                resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTING);
                            }
                            catch (IntentSender.SendIntentException ex)
                            {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE :
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnable() // this method returns true is gps is turned on and returns false if gps is turned off
    {
        LocationManager locationManager = null;
        boolean isEnable = false;

        if(locationManager == null)
        {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        }

        isEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnable;
    }

    private void get_Destination_Location_on_Map(String source, String destination)
    {
        Uri uri = Uri.parse("https://www.google.com/maps/dir/" + source + "/" + destination);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}