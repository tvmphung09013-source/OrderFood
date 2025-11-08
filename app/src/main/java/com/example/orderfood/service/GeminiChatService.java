package com.example.orderfood.service;

import android.content.Context;
import android.util.Log;

import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.Product;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiChatService {
    private static final String TAG = "GeminiChatService";
    // Note: In production, this should be stored securely (e.g., in BuildConfig or remote config)
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
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
                // Get product information from database
                List<Product> products = appDatabase.productDao().getAllProducts();
                String menuContext = buildMenuContext(products);
                
                // Build prompt with menu context
                String prompt = buildPrompt(userMessage, menuContext);
                
                // Call Gemini API
                String response = callGeminiAPI(prompt);
                
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
        return "You are a helpful restaurant assistant. Use the following menu information to answer customer questions about food items, ingredients, preparation methods, and food stories.\n\n" +
               menuContext + "\n\n" +
               "Customer question: " + userMessage + "\n\n" +
               "Please provide a helpful, friendly response about the menu items. If asked about ingredients or preparation, provide detailed information. If asked about a dish's story or background, share interesting facts. Keep responses concise (2-3 sentences).";
    }

    private String callGeminiAPI(String prompt) {
        try {
            // Check if API key is set
            if (API_KEY.equals("YOUR_GEMINI_API_KEY_HERE")) {
                Log.w(TAG, "Gemini API key not configured, using fallback response");
                return generateFallbackResponse(prompt);
            }

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Build request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                // Parse response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject contentObj = candidate.getJSONObject("content");
                    JSONArray partsArray = contentObj.getJSONArray("parts");
                    if (partsArray.length() > 0) {
                        JSONObject partObj = partsArray.getJSONObject(0);
                        return partObj.getString("text");
                    }
                }
            } else {
                Log.e(TAG, "API request failed with code: " + responseCode);
                return generateFallbackResponse(prompt);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calling Gemini API", e);
            return generateFallbackResponse(prompt);
        }
        return null;
    }

    private String generateFallbackResponse(String prompt) {
        // Extract keywords from user message to provide contextual fallback responses
        String lowerPrompt = prompt.toLowerCase();
        
        if (lowerPrompt.contains("pizza")) {
            return "Our pizza is made with fresh tomato sauce, mozzarella cheese, and premium toppings. It's baked to perfection in our wood-fired oven!";
        } else if (lowerPrompt.contains("burger")) {
            return "Our burger features a juicy beef patty with fresh vegetables and our special house sauce. It's served in a toasted sesame seed bun!";
        } else if (lowerPrompt.contains("salad")) {
            return "Our fresh salad is made with crisp greens, cherry tomatoes, cucumbers, and a light vinaigrette dressing. Perfect for a healthy meal!";
        } else if (lowerPrompt.contains("ingredient") || lowerPrompt.contains("nguyên liệu")) {
            return "All our dishes are made with fresh, high-quality ingredients sourced daily. Would you like to know about a specific dish?";
        } else if (lowerPrompt.contains("how") || lowerPrompt.contains("prepare") || lowerPrompt.contains("cook") || lowerPrompt.contains("chế biến")) {
            return "Our chefs use traditional cooking methods combined with modern techniques to ensure the best flavors. Which dish would you like to know more about?";
        } else if (lowerPrompt.contains("price") || lowerPrompt.contains("giá") || lowerPrompt.contains("cost")) {
            return "Our menu items range from $5.99 to $10.99. Pizza is $10.99, Burger is $5.99, and Salad is $7.99. What would you like to order?";
        } else if (lowerPrompt.contains("recommend") || lowerPrompt.contains("suggest") || lowerPrompt.contains("best")) {
            return "I recommend our pizza if you want something hearty, our burger for a classic favorite, or our salad for a lighter, healthy option!";
        } else {
            return "I'd be happy to help you with information about our menu, ingredients, preparation methods, or recommendations. What would you like to know?";
        }
    }
}
