package com.example.orderfood.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "is_from_customer")
    private boolean isFromCustomer;

    @ColumnInfo(name = "sender_name")
    private String senderName;

    public ChatMessage(String message, long timestamp, boolean isFromCustomer, String senderName) {
        this.message = message;
        this.timestamp = timestamp;
        this.isFromCustomer = isFromCustomer;
        this.senderName = senderName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFromCustomer() {
        return isFromCustomer;
    }

    public void setFromCustomer(boolean fromCustomer) {
        isFromCustomer = fromCustomer;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
