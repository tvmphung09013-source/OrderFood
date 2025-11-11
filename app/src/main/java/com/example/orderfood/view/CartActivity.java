package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private AppDatabase appDatabase;
    private TextView totalPriceTextView;
    private ImageButton backButton;
	private android.widget.Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        recyclerView = findViewById(R.id.cartRecyclerView);
        backButton = findViewById(R.id.cartBackButton);
		checkoutButton = findViewById(R.id.checkoutButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(cartItemList, this);
        recyclerView.setAdapter(adapter);

        loadCartItems();

        backButton.setOnClickListener(v -> finish());
		checkoutButton.setOnClickListener(v -> checkout());
    }

    private void loadCartItems() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<CartItem> items = appDatabase.cartDao().getAllCartItems();
            handler.post(() -> {
                cartItemList.clear();
                cartItemList.addAll(items);
                adapter.notifyDataSetChanged();
                calculateTotalPrice();
            });
        });
    }

    private void calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format("$%.2f", total));
    }

    @Override
    public void onQuantityChange(int productId, int newQuantity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            CartItem item = appDatabase.cartDao().getCartItemById(productId);
            if (item != null) {
                item.setQuantity(newQuantity);
                appDatabase.cartDao().update(item);
                loadCartItems(); // Reload to reflect changes
            }
        });
    }

    @Override
    public void onDelete(int productId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            appDatabase.cartDao().deleteById(productId);
            loadCartItems(); // Reload to reflect changes
        });
    }

	private void checkout() {
		java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());

		executor.execute(() -> {
			List<CartItem> items = appDatabase.cartDao().getAllCartItems();
			if (items == null || items.isEmpty()) {
				handler.post(() -> {
					android.widget.Toast.makeText(CartActivity.this, "Cart is empty", android.widget.Toast.LENGTH_SHORT).show();
				});
				return;
			}

			int userId = getSharedPreferences("orderfood_prefs", MODE_PRIVATE).getInt("current_user_id", -1);
			if (userId == -1) {
				handler.post(() -> {
					android.widget.Toast.makeText(CartActivity.this, "Please login first", android.widget.Toast.LENGTH_SHORT).show();
				});
				return;
			}

			double total = 0;
			int itemCount = 0;
			for (CartItem item : items) {
				total += item.getProduct().getPrice() * item.getQuantity();
				itemCount += item.getQuantity();
			}

			com.example.orderfood.model.Order order = new com.example.orderfood.model.Order(userId, total, System.currentTimeMillis());
			long orderIdLong = appDatabase.orderDao().insert(order);
			int orderId = (int) orderIdLong;

			java.util.List<com.example.orderfood.model.OrderItem> orderItems = new java.util.ArrayList<>();
			for (CartItem ci : items) {
				orderItems.add(new com.example.orderfood.model.OrderItem(orderId, ci.getProductId(), ci.getQuantity(), ci.getProduct().getPrice()));
			}
			appDatabase.orderDao().insertItems(orderItems);

			appDatabase.cartDao().deleteAll();

			final double finalTotal = total;
			final int finalItemCount = itemCount;

			handler.post(() -> {
				// Navigate to Billing Activity
				android.content.Intent intent = new android.content.Intent(CartActivity.this, BillingActivity.class);
				intent.putExtra("total_amount", finalTotal);
				intent.putExtra("item_count", finalItemCount);
				startActivity(intent);
				
				// Clear the cart UI
				cartItemList.clear();
				adapter.notifyDataSetChanged();
				calculateTotalPrice();
				
				// Finish this activity so user can't go back to empty cart
				finish();
			});
		});
	}
}
