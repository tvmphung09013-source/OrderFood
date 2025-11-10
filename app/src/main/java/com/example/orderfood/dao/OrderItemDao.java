package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.orderfood.model.OrderItem;

import java.util.List;

@Dao
public interface OrderItemDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(OrderItem item);

	@Query("SELECT * FROM order_items WHERE order_id = :orderId")
	List<OrderItem> getByOrder(int orderId);

	@Delete
	void delete(OrderItem item);
}


