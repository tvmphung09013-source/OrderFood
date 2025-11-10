package com.example.orderfood.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.orderfood.model.Order;
import com.example.orderfood.model.OrderItem;

import java.util.List;

@Dao
public interface OrderDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(Order order);

	// CartActivity calls orderDao().insertItems(...)
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertItems(List<OrderItem> items);

	@Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
	List<Order> getOrdersForUser(int userId);
}


