package com.example.photomap;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        Button.OnClickListener, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private GoogleMap mMap;
    FusedLocationProviderClient client;
    FloatingActionButton button;
    int request;
    Location lastLocation;
    LatLng markerPos;
    ImageView display;
    Button close;

    private LocationCallback locationCallback;
    LocationRequest locationRequest;
    LocationSettingsRequest.Builder builder;


    public MapsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        button = findViewById(R.id.photo);
        button.setOnClickListener(this);

        close = findViewById(R.id.close);
        close.setOnClickListener(this);
        close.setVisibility(View.INVISIBLE);

        display = findViewById(R.id.display);
        display.setVisibility(View.INVISIBLE);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.wtf("Location Update", "The location has been updated");
                    lastLocation = location;
                }
            };
        };

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, request);
        }

        client = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        startLocationUpdates();

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            //checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            finish();
        }
        return;
    }

    @Override
    public void onClick(View v) {
        //getLastLocation();
        final String TAG = "Map Activity";
        //create an intent to have the default camera app take a picture and return the picture.
        if (v == button) {

            if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                markerPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            }


            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        } else if (v == close) {
            close.setVisibility(View.INVISIBLE);
            display.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //the picture is stored in the intent in the data key.
        //get the picture and show it in an the imagview.
        //Note the picture is not stored on the filesystem, so this is the only "copy" of the picture.
        Bundle extras = data.getExtras();
        if (extras != null) {
            //if you know for a fact there will be a bundle, you can use  data.getExtras().get("Data");  but we don't know.
            Bitmap bp = (Bitmap) extras.get("data");

            Marker temp = mMap.addMarker(new MarkerOptions()
                    .position(markerPos)
                    .icon(BitmapDescriptorFactory.fromBitmap(bp)));
            temp.setTag(bp);
            mMap.setOnMarkerClickListener(this);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPos));
            Toast.makeText(this, "We got a picture", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No picture was returned", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        display.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
        display.setImageBitmap((Bitmap)marker.getTag());

        return true;
    }

    private void startLocationUpdates(){
        client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lastLocation = location;
                    markerPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                } else {
                    Log.wtf("Location Updates", "location is null!!!");
                }
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Location Updates", "getLastLocation:onFailure", e);
                    }
                });


        client.requestLocationUpdates(locationRequest, locationCallback,null);
    }

    private void createLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(7000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}

