package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.orderfood.model.Review;

import java.util.List;

@Dao
public interface ReviewDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Review review);

	@Query("SELECT * FROM reviews WHERE product_id = :productId ORDER BY created_at DESC")
	List<Review> getByProduct(int productId);

	@Query("SELECT AVG(rating) FROM reviews WHERE product_id = :productId")
	Float getAverageRatingForProduct(int productId);
}


