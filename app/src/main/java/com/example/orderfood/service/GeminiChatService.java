package com.example.orderfood.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.Product;
import com.example.orderfood.util.StoreInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiChatService {
    private static final String TAG = "GeminiChatService";
    // Note: In production, this should be stored securely (e.g., in BuildConfig or remote config)
    private static final String API_KEY = "AIzaSyCSsanU5tDhOonAlF2yBSdisbJ10YhXtOY";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    private Context context;
    private AppDatabase appDatabase;

    public GeminiChatService(Context context) {
        this.context = context;
        this.appDatabase = AppDatabase.getDatabase(context);
    }

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void generateResponse(String userMessage, ResponseCallback callback) {
        new Thread(() -> {
            try {
                List<Product> products = appDatabase.productDao().getAllProducts();
                String menuContext = buildMenuContext(products);
                String prompt = buildPrompt(userMessage, menuContext);
                String response = callGeminiAPI(prompt, userMessage, products);

                if (response != null && !response.isEmpty()) {
                    callback.onResponse(response);
                } else {
                    callback.onError("No response from AI");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error generating response", e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private String buildMenuContext(List<Product> products) {
        StringBuilder context = new StringBuilder("Menu items available:\n");
        for (Product product : products) {
            context.append("- ").append(product.getName())
                    .append(" (").append(product.getCategory()).append("): ")
                    .append(product.getDescription())
                    .append(" - Price: $").append(product.getPrice())
                    .append("\n");
        }
        return context.toString();
    }

    private String buildPrompt(String userMessage, String menuContext) {
        return "You are a helpful restaurant assistant... (prompt text)";
    }

    private String callGeminiAPI(String prompt, String userMessage, List<Product> products) {
        return generateFallbackResponse(userMessage, products);
    }

    private String generateFallbackResponse(String userMessage, List<Product> products) {
        String lowerUserMessage = userMessage.toLowerCase();

        if (lowerUserMessage.startsWith("y ")) {
            String itemName = lowerUserMessage.substring(2).trim();
            for (Product product : products) {
                if (product.getName().toLowerCase().equals(itemName)) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        CartItem existingItem = appDatabase.cartDao().getCartItemById(product.getId());
                        if (existingItem != null) {
                            existingItem.setQuantity(existingItem.getQuantity() + 1);
                            appDatabase.cartDao().update(existingItem);
                        } else {
                            CartItem newItem = new CartItem(product, 1);
                            appDatabase.cartDao().insert(newItem);
                        }
                        showAddToCartNotification(product);
                    });
                    return "Confirmed! " + product.getName() + " has been added to your cart. Would you like anything else?";
                }
            }
        }

        if (lowerUserMessage.contains("menu") || lowerUserMessage.contains("thực đơn")) {
            return buildMenuContext(products) + "\nWhat would you like to order?";
        }

        for (Product product : products) {
            if (lowerUserMessage.contains(product.getName().toLowerCase())) {
                return "A " + product.getName() + " costs $" + product.getPrice() + ". To confirm and add it to your cart, please reply with 'y " + product.getName().toLowerCase() + "'.";
            }
        }

        if (lowerUserMessage.contains("hello") || lowerUserMessage.contains("hi") || lowerUserMessage.contains("chào")) {
            return "Hello! Welcome to our restaurant. How can I help you today? You can ask for the menu to see our offerings.";
        } else if (lowerUserMessage.contains("drink") || lowerUserMessage.contains("đồ uống")) {
            return "We have a variety of drinks including soft drinks, juices, and house-made iced tea. What would you like?";
        } else if (lowerUserMessage.contains("dessert") || lowerUserMessage.contains("tráng miệng")) {
            return "For dessert, we have a delicious chocolate lava cake and a classic New York cheesecake. Both are highly recommended!";
        } else if (lowerUserMessage.contains("ingredient") || lowerUserMessage.contains("nguyên liệu")) {
            return "All our dishes are made with fresh, high-quality ingredients sourced daily. Would you like to know about a specific dish?";
        } else if (lowerUserMessage.contains("how") || lowerUserMessage.contains("prepare") || lowerUserMessage.contains("cook") || lowerUserMessage.contains("chế biến")) {
            return "Our chefs use traditional cooking methods combined with modern techniques to ensure the best flavors. Which dish would you like to know more about?";
        } else if (lowerUserMessage.contains("price") || lowerUserMessage.contains("giá") || lowerUserMessage.contains("cost")) {
            return "You can ask for the menu to see all prices. Which item are you interested in?";
        } else if (lowerUserMessage.contains("recommend") || lowerUserMessage.contains("suggest") || lowerUserMessage.contains("best")) {
            return "I recommend our pizza if you want something hearty, our burger for a classic favorite, or our salad for a lighter, healthy option!";
        } else if (lowerUserMessage.contains("open") || lowerUserMessage.contains("hours") || lowerUserMessage.contains("giờ mở cửa")) {
            return "We are open from 10 AM to 10 PM, Monday to Sunday.";
        } else if (lowerUserMessage.contains("location") || lowerUserMessage.contains("address") || lowerUserMessage.contains("địa chỉ")) {
            String mapsUrl = "https://www.google.com/maps/search/?api=1&query="
                    + StoreInfo.STORE_LAT + "," + StoreInfo.STORE_LNG;
            return "You can find us at " + StoreInfo.STORE_ADDRESS + ". Map: " + mapsUrl;
        } else if (lowerUserMessage.contains("promotion") || lowerUserMessage.contains("discount") || lowerUserMessage.contains("khuyến mãi")) {
            return "We currently have a 'Buy one get one free' offer on all pizzas every Wednesday. Don't miss out!";
        } else if (lowerUserMessage.contains("thank") || lowerUserMessage.contains("cảm ơn")) {
            return "You're welcome! Is there anything else I can help you with?";
        } else if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("tạm biệt")) {
            return "Goodbye! Have a great day!";
        } else {
            return "I can help you with our menu, place an order, or answer questions about our food. What would you like to do?";
        }
    }

    private void showAddToCartNotification(Product product) {
        String channelId = "CART_NOTIFICATION_CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cart Notifications";
            String description = "Shows notifications when an item is added to the cart.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your cart icon if available
                .setContentTitle("Added to Cart")
                .setContentText("1x " + product.getName() + " was added to your cart.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(product.getId(), builder.build());
    }
}
