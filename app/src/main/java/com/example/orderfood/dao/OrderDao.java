package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.orderfood.model.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    void insert(Order order);

    @Query("SELECT * FROM orders")
    List<Order> getAllOrders();

    @Query("DELETE FROM orders")
    void deleteAll();
}
