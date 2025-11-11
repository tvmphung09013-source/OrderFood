package com.example.orderfood.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.util.StoreInfo;

public class StoreLocationActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button openMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_location);

        backButton = findViewById(R.id.storeLocationBackButton);
        openMapButton = findViewById(R.id.openMapButton);

        setupBackButton();
        setupOpenMapButton();
    }

    private void setupOpenMapButton() {
        openMapButton.setOnClickListener(v -> openInGoogleMaps());
    }

    private void openInGoogleMaps() {
        // Create URI for Google Maps intent with the store's address
        String address = StoreInfo.STORE_ADDRESS;
        String mapUri = "geo:0,0?q=" + Uri.encode(address);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
        mapIntent.setPackage("com.google.android.apps.maps");

        // Verify that Google Maps is installed before starting the intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // If not installed, open in browser
            String browserUri = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }
}
