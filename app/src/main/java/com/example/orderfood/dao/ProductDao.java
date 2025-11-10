package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.orderfood.model.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Product> products);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    @Query("UPDATE products SET rating = :rating WHERE id = :productId")
    void updateRating(int productId, float rating);

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    Product getById(int productId);
}
