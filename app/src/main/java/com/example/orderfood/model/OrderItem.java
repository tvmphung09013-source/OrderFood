package com.example.orderfood.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "order_items")
public class OrderItem {

	@PrimaryKey(autoGenerate = true)
	private int id;

	@ColumnInfo(name = "order_id")
	private int orderId;

	@ColumnInfo(name = "product_id")
	private int productId;

	private int quantity;

	@ColumnInfo(name = "unit_price")
	private double unitPrice;

	// Room requires a no-arg constructor
	public OrderItem() {}

	@Ignore
	public OrderItem(int orderId, int productId, int quantity, double unitPrice) {
		this.orderId = orderId;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
}


