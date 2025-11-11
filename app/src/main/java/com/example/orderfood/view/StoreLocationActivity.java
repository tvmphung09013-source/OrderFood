package com.example.orderfood.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.util.StoreInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StoreLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private ImageButton backButton;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_location);

        backButton = findViewById(R.id.storeLocationBackButton);
        mapView = findViewById(R.id.mapView);

        // Initialize MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        setupBackButton();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        
        // Get store location from StoreInfo utility
        LatLng storeLocation = new LatLng(
            StoreInfo.STORE_LATITUDE, 
            StoreInfo.STORE_LONGITUDE
        );
        
        // Add marker for store location
        googleMap.addMarker(new MarkerOptions()
                .position(storeLocation)
                .title(StoreInfo.STORE_NAME)
                .snippet("Tap to open in Google Maps"));
        
        // Move camera to store location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15));
        
        // Enable map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        
        // Set click listener on the map to open Google Maps app
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                openInGoogleMaps();
            }
        });
        
        // Set marker click listener to open Google Maps app
        googleMap.setOnMarkerClickListener(marker -> {
            openInGoogleMaps();
            return true;
        });
    }
    
    private void openInGoogleMaps() {
        // Create intent to open Google Maps app with store location
        String uri = String.format("geo:%f,%f?q=%f,%f(%s)", 
            StoreInfo.STORE_LATITUDE, 
            StoreInfo.STORE_LONGITUDE,
            StoreInfo.STORE_LATITUDE, 
            StoreInfo.STORE_LONGITUDE,
            StoreInfo.STORE_NAME);
        
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        // Check if Google Maps app is installed
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // If Google Maps app is not installed, open in browser
            String browserUri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                StoreInfo.STORE_LATITUDE,
                StoreInfo.STORE_LONGITUDE);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
