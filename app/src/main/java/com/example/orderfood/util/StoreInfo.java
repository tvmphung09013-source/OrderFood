package com.example.orderfood.util;

/**
 * Centralized store information constants.
 * Used across the app for consistency in location-related features.
 */
public class StoreInfo {
    // Store coordinates (Can Tho City area)
    public static final double STORE_LATITUDE = 10.0302;
    public static final double STORE_LONGITUDE = 105.7720;

    // Legacy field names for backward compatibility
    public static final double STORE_LAT = STORE_LATITUDE;
    public static final double STORE_LNG = STORE_LONGITUDE;

    // Store identification
    public static final String STORE_NAME = "Our Food Store";
    public static final String STORE_LABEL = STORE_NAME;
    public static final String STORE_ADDRESS = "600 Nguyễn Văn Cừ Nối Dài, An Bình, Bình Thủy, Cần Thơ";

    // Private constructor to prevent instantiation
    private StoreInfo() {
        throw new AssertionError("StoreInfo is a utility class and should not be instantiated");
    }
}
