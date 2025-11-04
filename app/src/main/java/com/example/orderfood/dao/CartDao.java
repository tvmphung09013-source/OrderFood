package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.orderfood.model.CartItem;

import java.util.List;

@Dao
public interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItem cartItem);

    @Update
    void update(CartItem cartItem);

    @Query("SELECT * FROM cart_items")
    List<CartItem> getAllCartItems();

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    CartItem getCartItemById(int productId);

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    void deleteById(int productId);

    @Query("DELETE FROM cart_items")
    void deleteAll();
}
