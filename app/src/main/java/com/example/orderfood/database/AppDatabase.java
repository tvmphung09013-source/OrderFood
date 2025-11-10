package com.example.orderfood.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.orderfood.dao.CartDao;
import com.example.orderfood.dao.ChatDao;
import com.example.orderfood.dao.OrderDao;
import com.example.orderfood.dao.OrderItemDao;
import com.example.orderfood.dao.ProductDao;
import com.example.orderfood.dao.ReviewDao;
import com.example.orderfood.dao.UserDao;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.ChatMessage;
import com.example.orderfood.model.Order;
import com.example.orderfood.model.OrderItem;
import com.example.orderfood.model.Product;
import com.example.orderfood.model.Review;
import com.example.orderfood.model.User;

@Database(entities = {User.class, Product.class, CartItem.class, ChatMessage.class, Order.class, OrderItem.class, Review.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();
    public abstract ChatDao chatDao();
	public abstract OrderDao orderDao();
	public abstract OrderItemDao orderItemDao();
	public abstract ReviewDao reviewDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "order_food_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
