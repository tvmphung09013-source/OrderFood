package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.orderfood.model.ChatMessage;

import java.util.List;

@Dao
public interface ChatDao {
    
    @Insert
    void insert(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessages();

    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();
}
