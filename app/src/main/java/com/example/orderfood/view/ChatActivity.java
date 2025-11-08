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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

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
                
                // Simulate a store response after 2 seconds
                handler.postDelayed(() -> simulateStoreResponse(), 2000);
            });
        });
    }

    private void simulateStoreResponse() {
        String[] responses = {
            "Thank you for your message! How can we help you?",
            "We've received your message. Our team will respond shortly.",
            "Hello! What would you like to order today?",
            "Thank you for contacting us!",
            "We're here to help. What do you need?"
        };
        
        int randomIndex = (int) (Math.random() * responses.length);
        String responseText = responses[randomIndex];
        long timestamp = System.currentTimeMillis();
        ChatMessage storeMessage = new ChatMessage(responseText, timestamp, false, "Store");

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
