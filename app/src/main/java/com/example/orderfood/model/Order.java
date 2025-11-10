package com.example.orderfood.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.io.Serializable;

@Entity(tableName = "orders")
public class Order implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int id;

	@ColumnInfo(name = "user_id")
	private int userId;

	@ColumnInfo(name = "total_amount")
	private double totalAmount;

	@ColumnInfo(name = "created_at")
	private long createdAt;

	// Room requires a no-arg constructor
	public Order() {}

	@Ignore
	public Order(int userId, double totalAmount, long createdAt) {
		this.userId = userId;
		this.totalAmount = totalAmount;
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
}


