package com.example.orderfood.service;

import android.content.Context;
import android.util.Log;

import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.ConversationHistory;
import com.example.orderfood.model.FunctionDeclaration;
import com.example.orderfood.model.Product;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiChatService {
    private static final String TAG = "GeminiChatService";
    // Note: In production, this should be stored securely (e.g., in BuildConfig or remote config)
    private static final String API_KEY = "AIzaSyCSsanU5tDhOonAlF2yBSdisbJ10YhXtOY";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    
    private Context context;
    private AppDatabase appDatabase;
    private ConversationHistory conversationHistory;
    private List<FunctionDeclaration> functionDeclarations;

    public GeminiChatService(Context context) {
        this.context = context;
        this.appDatabase = AppDatabase.getDatabase(context);
        this.conversationHistory = new ConversationHistory();
        this.functionDeclarations = new ArrayList<>();
        initializeFunctions();
    }

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    private void initializeFunctions() {
        // Function 1: addToCart - Add product to shopping cart
        FunctionDeclaration addToCart = new FunctionDeclaration(
            "addToCart",
            "Thêm sản phẩm cụ thể và số lượng vào giỏ hàng. Chỉ sử dụng khi người dùng có ý định đặt mua hàng rõ ràng (ví dụ: 'Tôi lấy', 'Cho tôi', 'Thêm vào giỏ', 'Đặt', 'Mua'). Mô hình cần phân tích các từ như 'cái', 'suất', 'phần', 'ly' thành số lượng và chuyển tên gọi thông thường thành tên sản phẩm chính xác."
        );
        addToCart.addParameter(new FunctionDeclaration.Parameter(
            "itemName",
            "string",
            "Tên chính xác của sản phẩm trong menu (Pizza, Burger, Salad, etc.)",
            true
        ));
        addToCart.addParameter(new FunctionDeclaration.Parameter(
            "quantity",
            "integer",
            "Số lượng sản phẩm cần thêm vào giỏ hàng (mặc định là 1 nếu không được chỉ định)",
            true
        ));
        functionDeclarations.add(addToCart);

        // Function 2: getMenu - Get full menu list
        FunctionDeclaration getMenu = new FunctionDeclaration(
            "getMenu",
            "Cung cấp danh sách các món ăn hiện có, tên, và giá tiền. Dùng khi người dùng hỏi 'menu', 'thực đơn', 'có món gì', hay 'tôi có thể gọi gì?'."
        );
        functionDeclarations.add(getMenu);

        // Function 3: getProductInfo - Get detailed product information
        FunctionDeclaration getProductInfo = new FunctionDeclaration(
            "getProductInfo",
            "Tra cứu thông tin chi tiết (giá, mô tả, nguyên liệu) của một món ăn cụ thể trong menu. Rất hữu ích khi người dùng hỏi 'món này giá bao nhiêu?', 'burger có gì?', 'pizza làm từ gì?'."
        );
        getProductInfo.addParameter(new FunctionDeclaration.Parameter(
            "itemName",
            "string",
            "Tên sản phẩm cần tra cứu thông tin",
            true
        ));
        functionDeclarations.add(getProductInfo);
    }

    public void generateResponse(String userMessage, ResponseCallback callback) {
        new Thread(() -> {
            try {
                // Add user message to conversation history
                conversationHistory.addUserMessage(userMessage);
                
                // Call Gemini API with function calling
                String response = callGeminiAPIWithFunctions(callback);
                
                if (response != null && !response.isEmpty()) {
                    // Add AI response to conversation history
                    conversationHistory.addModelMessage(response);
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

    private String callGeminiAPIWithFunctions(ResponseCallback callback) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Build request with function declarations and conversation history
            JSONObject request = new JSONObject();
            request.put("contents", conversationHistory.toJSONArray());
            
            // Add function declarations as tools
            JSONArray tools = new JSONArray();
            JSONObject tool = new JSONObject();
            JSONArray functionDeclarationsArray = new JSONArray();
            
            for (FunctionDeclaration func : functionDeclarations) {
                functionDeclarationsArray.put(func.toJSON());
            }
            
            tool.put("functionDeclarations", functionDeclarationsArray);
            tools.put(tool);
            request.put("tools", tools);

            // Add system instruction
            JSONObject systemInstruction = new JSONObject();
            JSONArray systemParts = new JSONArray();
            JSONObject systemPart = new JSONObject();
            systemPart.put("text", "Bạn là trợ lý nhà hàng thân thiện và hữu ích. Nhiệm vụ của bạn là giúp khách hàng đặt món ăn, trả lời câu hỏi về menu, và cung cấp thông tin chi tiết về các món ăn. Hãy luôn lịch sự, nhiệt tình và cung cấp thông tin chính xác.");
            systemParts.put(systemPart);
            systemInstruction.put("parts", systemParts);
            request.put("systemInstruction", systemInstruction);

            // Send request
            String jsonInputString = request.toString();
            Log.d(TAG, "Request: " + jsonInputString);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();
                
                String responseStr = response.toString();
                Log.d(TAG, "Response: " + responseStr);
                
                return processGeminiResponse(responseStr, callback);
            } else {
                Log.e(TAG, "HTTP error code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calling Gemini API", e);
            return null;
        }
    }

    private String processGeminiResponse(String responseStr, ResponseCallback callback) {
        try {
            JSONObject responseJson = new JSONObject(responseStr);
            JSONArray candidates = responseJson.getJSONArray("candidates");
            
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                
                // Check if response contains function call
                if (parts.length() > 0) {
                    JSONObject part = parts.getJSONObject(0);
                    
                    if (part.has("functionCall")) {
                        // AI wants to call a function
                        JSONObject functionCall = part.getJSONObject("functionCall");
                        String functionName = functionCall.getString("name");
                        JSONObject args = functionCall.getJSONObject("args");
                        
                        Log.d(TAG, "Function call: " + functionName + " with args: " + args.toString());
                        
                        // Add function call to conversation history
                        conversationHistory.addFunctionCall(functionName, args);
                        
                        // Execute the function
                        JSONObject functionResult = executeFunction(functionName, args);
                        
                        // Add function response to conversation history
                        conversationHistory.addFunctionResponse(functionName, functionResult);
                        
                        // Call Gemini again to get natural language response
                        return callGeminiAPIWithFunctions(callback);
                        
                    } else if (part.has("text")) {
                        // AI returned text response
                        return part.getString("text");
                    }
                }
            }
            
            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.";
        } catch (Exception e) {
            Log.e(TAG, "Error processing response", e);
            return null;
        }
    }

    private JSONObject executeFunction(String functionName, JSONObject args) {
        JSONObject result = new JSONObject();
        
        try {
            switch (functionName) {
                case "addToCart":
                    result = executeAddToCart(args);
                    break;
                case "getMenu":
                    result = executeGetMenu();
                    break;
                case "getProductInfo":
                    result = executeGetProductInfo(args);
                    break;
                default:
                    result.put("error", "Unknown function: " + functionName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing function: " + functionName, e);
            try {
                result.put("error", e.getMessage());
            } catch (Exception ex) {
                Log.e(TAG, "Error creating error result", ex);
            }
        }
        
        return result;
    }

    private JSONObject executeAddToCart(JSONObject args) throws Exception {
        String itemName = args.getString("itemName");
        int quantity = args.optInt("quantity", 1);
        
        JSONObject result = new JSONObject();
        
        // Find product in database
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<Product> products = appDatabase.productDao().getAllProducts();
        
        Product foundProduct = null;
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(itemName)) {
                foundProduct = product;
                break;
            }
        }
        
        if (foundProduct != null) {
            final Product productToAdd = foundProduct;
            executor.execute(() -> {
                try {
                    CartItem existingItem = appDatabase.cartDao().getCartItemById(productToAdd.getId());
                    if (existingItem != null) {
                        existingItem.setQuantity(existingItem.getQuantity() + quantity);
                        appDatabase.cartDao().update(existingItem);
                    } else {
                        CartItem newItem = new CartItem(productToAdd, quantity);
                        appDatabase.cartDao().insert(newItem);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error adding to cart", e);
                }
            });
            
            result.put("success", true);
            result.put("message", "Đã thêm " + quantity + " " + foundProduct.getName() + " vào giỏ hàng");
            result.put("productName", foundProduct.getName());
            result.put("quantity", quantity);
            result.put("price", foundProduct.getPrice());
        } else {
            result.put("success", false);
            result.put("error", "Không tìm thấy sản phẩm: " + itemName);
        }
        
        return result;
    }

    private JSONObject executeGetMenu() throws Exception {
        List<Product> products = appDatabase.productDao().getAllProducts();
        
        JSONObject result = new JSONObject();
        JSONArray menuItems = new JSONArray();
        
        for (Product product : products) {
            JSONObject item = new JSONObject();
            item.put("name", product.getName());
            item.put("category", product.getCategory());
            item.put("price", product.getPrice());
            item.put("description", product.getDescription());
            menuItems.put(item);
        }
        
        result.put("menuItems", menuItems);
        result.put("totalItems", products.size());
        
        return result;
    }

    private JSONObject executeGetProductInfo(JSONObject args) throws Exception {
        String itemName = args.getString("itemName");
        
        List<Product> products = appDatabase.productDao().getAllProducts();
        
        JSONObject result = new JSONObject();
        
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(itemName)) {
                result.put("name", product.getName());
                result.put("category", product.getCategory());
                result.put("price", product.getPrice());
                result.put("description", product.getDescription());
                result.put("rating", product.getRating());
                result.put("found", true);
                return result;
            }
        }
        
        result.put("found", false);
        result.put("error", "Không tìm thấy sản phẩm: " + itemName);
        
        return result;
    }
}
