package com.example.orderfood.util;

/**
 * Centralized store information constants.
 * Used across the app for consistency in location-related features.
 */
public class StoreInfo {
    // Store coordinates (Ho Chi Minh City area)
    public static final double STORE_LAT = 10.776889;
    public static final double STORE_LNG = 106.700806;
    
    // Store identification
    public static final String STORE_LABEL = "Our Store";
    public static final String STORE_ADDRESS = "123 Food Street, Delicious City";
    
    // Private constructor to prevent instantiation
    private StoreInfo() {
        throw new AssertionError("StoreInfo is a utility class and should not be instantiated");
    }
}
