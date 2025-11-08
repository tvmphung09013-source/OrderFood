package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.ChatMessage;
import com.example.orderfood.service.GeminiChatService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private List<ChatMessage> messages = new ArrayList<>();
    private AppDatabase appDatabase;
    private ExecutorService executor;
    private Handler handler;
    private GeminiChatService geminiChatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        geminiChatService = new GeminiChatService(getApplicationContext());

        initViews();
        setupRecyclerView();
        loadMessages();
        setupListeners();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadMessages() {
        executor.execute(() -> {
            List<ChatMessage> messagesFromDb = appDatabase.chatDao().getAllMessages();
            
            handler.post(() -> {
                messages.clear();
                messages.addAll(messagesFromDb);
                chatAdapter.updateMessages(messages);
                scrollToBottom();
            });
        });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
    }

    private void sendMessage(String messageText) {
        long timestamp = System.currentTimeMillis();
        ChatMessage newMessage = new ChatMessage(messageText, timestamp, true, "You");

        executor.execute(() -> {
            appDatabase.chatDao().insert(newMessage);
            
            handler.post(() -> {
                messageInput.setText("");
                loadMessages();
                
                // Generate AI response after a short delay
                handler.postDelayed(() -> generateAIResponse(messageText), 1000);
            });
        });
    }

    private void generateAIResponse(String userMessage) {
        geminiChatService.generateResponse(userMessage, new GeminiChatService.ResponseCallback() {
            @Override
            public void onResponse(String response) {
                long timestamp = System.currentTimeMillis();
                ChatMessage storeMessage = new ChatMessage(response, timestamp, false, "AI Assistant");

                executor.execute(() -> {
                    appDatabase.chatDao().insert(storeMessage);
                    
                    handler.post(() -> {
                        loadMessages();
                    });
                });
            }

            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(ChatActivity.this, "Error generating response. Please try again.", Toast.LENGTH_SHORT).show();
                    // Fallback to simple response
                    generateFallbackResponse(userMessage);
                });
            }
        });
    }

    private void generateFallbackResponse(String userMessage) {
        String response = "Thank you for your message! I'd be happy to help you with information about our menu. What would you like to know?";
        long timestamp = System.currentTimeMillis();
        ChatMessage storeMessage = new ChatMessage(response, timestamp, false, "Store");

        executor.execute(() -> {
            appDatabase.chatDao().insert(storeMessage);
            
            handler.post(() -> {
                loadMessages();
            });
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
