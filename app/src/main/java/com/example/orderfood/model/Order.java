package com.example.orderfood.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Embedded;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "orders")
@TypeConverters(DateConverter.class)
public class Order implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private double totalPrice;
    private String status;
    private Date orderDate;

    @Embedded
    private ShippingAddress shippingAddress;

    // For simplicity, store product ids and quantities as a String (e.g. "1:2,2:1")
    // In production, use a relation table or TypeConverter for List<CartItem>
    private String items;

    public Order() {}

    public Order(int userId, double totalPrice, String status, Date orderDate, ShippingAddress shippingAddress, String items) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
}
