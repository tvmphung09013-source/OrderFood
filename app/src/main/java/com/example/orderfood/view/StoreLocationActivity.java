package com.example.orderfood.view;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;

public class StoreLocationActivity extends AppCompatActivity {

    private WebView mapWebView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_location);

        mapWebView = findViewById(R.id.mapWebView);
        backButton = findViewById(R.id.storeLocationBackButton);

        setupWebView();
        setupBackButton();
    }

    private void setupWebView() {
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mapWebView.setWebViewClient(new WebViewClient());

        // Example location: Google Headquarters (you can change this to actual store location)
        // Using embedded Google Maps with a marker
        String latitude = "37.7749";  // San Francisco as example
        String longitude = "-122.4194";
        String storeName = "Our Food Store";
        
        String mapUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude + "&z=15&output=embed";
        mapWebView.loadUrl(mapUrl);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }
}
