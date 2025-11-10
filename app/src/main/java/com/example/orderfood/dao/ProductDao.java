package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

    @Query("DELETE FROM products")
    void deleteAll();
}
