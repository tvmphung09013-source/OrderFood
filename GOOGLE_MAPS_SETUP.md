# Google Maps Setup Guide

This document explains how to configure Google Maps in the OrderFood application.

## Prerequisites

1. A Google Cloud Platform (GCP) account
2. Google Maps SDK for Android enabled

## Setup Instructions

### 1. Get a Google Maps API Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Maps SDK for Android** API:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Maps SDK for Android"
   - Click "Enable"
4. Create an API key:
   - Navigate to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
   - Copy your API key

### 2. Configure the API Key in Your Project

#### Option 1: Using local.properties (Recommended for development)

1. Open or create `local.properties` file in the project root directory
2. Add the following line:
   ```
   MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```
3. Replace `YOUR_ACTUAL_API_KEY_HERE` with your actual API key

#### Option 2: Direct configuration in build.gradle

Edit `app/build.gradle` and replace the placeholder in the `manifestPlaceholders` line:
```gradle
manifestPlaceholders = [MAPS_API_KEY: 'YOUR_ACTUAL_API_KEY_HERE']
```

### 3. Restrict Your API Key (Recommended)

For security, restrict your API key to your app:

1. In Google Cloud Console, go to "APIs & Services" > "Credentials"
2. Click on your API key
3. Under "Application restrictions", select "Android apps"
4. Click "Add an item" and enter:
   - Package name: `com.example.orderfood`
   - SHA-1 certificate fingerprint (get it by running):
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

## Features

The StoreLocationActivity now includes:

- **Native Google Maps View**: Uses the official Google Maps SDK instead of WebView
- **Clickable Map**: Tapping anywhere on the map opens the location in the Google Maps app
- **Marker Click**: Tapping the store marker also opens Google Maps app
- **Fallback**: If Google Maps app is not installed, opens the location in a web browser

## Store Location Configuration

The store location is configured in `app/src/main/java/com/example/orderfood/util/StoreInfo.java`:

```java
public static final double STORE_LATITUDE = 10.776889;
public static final double STORE_LONGITUDE = 106.700806;
public static final String STORE_NAME = "Our Food Store";
```

To change the store location, edit these values in the StoreInfo class.

## Permissions

The app requests the following location-related permissions:
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

These are necessary for Google Maps to function properly.

## Testing

To test the Google Maps integration:

1. Configure your API key as described above
2. Build and run the app
3. Navigate to the Store Location screen
4. Verify that the map loads and displays the store marker
5. Tap on the map or marker to verify it opens in Google Maps app

## Troubleshooting

### Map shows blank or gray screen
- Verify your API key is correctly configured
- Ensure Maps SDK for Android is enabled in Google Cloud Console
- Check that your API key restrictions (if any) include your app's package name and SHA-1

### "Google Maps app not installed" message
- This is expected if the device doesn't have Google Maps installed
- The app will automatically open the location in a web browser instead

### Build errors
- Make sure you have synced Gradle after adding the dependency
- Clean and rebuild the project: `./gradlew clean build`

## Additional Resources

- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation/android-sdk)
- [Maps SDK for Android Guide](https://developers.google.com/maps/documentation/android-sdk/start)
