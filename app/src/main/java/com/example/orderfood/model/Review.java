package com.example.orderfood.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.io.Serializable;

@Entity(tableName = "reviews")
public class Review implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int id;

	@ColumnInfo(name = "product_id")
	private int productId;

	@ColumnInfo(name = "user_id")
	private int userId;

	private float rating;

	private String comment;

	@ColumnInfo(name = "created_at")
	private long createdAt;

	// Room requires a no-arg constructor
	public Review() {}

	@Ignore
	public Review(int productId, int userId, float rating, String comment, long createdAt) {
		this.productId = productId;
		this.userId = userId;
		this.rating = rating;
		this.comment = comment;
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
}


