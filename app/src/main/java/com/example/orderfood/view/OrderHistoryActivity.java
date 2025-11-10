package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageButton;
import android.widget.TextView;

import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.Order;
import com.example.orderfood.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderHistoryActivity extends AppCompatActivity {

	private AppDatabase appDatabase;
	private RecyclerView recyclerView;
	private OrderHistoryAdapter adapter;
	private final List<OrderHistoryAdapter.OrderHistoryDisplay> displays = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_history);

		ImageButton backButton = findViewById(R.id.historyBackButton);
		TextView titleView = findViewById(R.id.historyTitleTextView);
		recyclerView = findViewById(R.id.historyRecyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		appDatabase = AppDatabase.getDatabase(getApplicationContext());
		adapter = new OrderHistoryAdapter(displays);
		recyclerView.setAdapter(adapter);

		backButton.setOnClickListener(v -> finish());

		loadOrders();
	}

	private void loadOrders() {
		int userId = getSharedPreferences("orderfood_prefs", MODE_PRIVATE).getInt("current_user_id", -1);
		if (userId == -1) {
			return;
		}
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());
		executor.execute(() -> {
			List<Order> result = appDatabase.orderDao().getOrdersForUser(userId);
			List<OrderHistoryAdapter.OrderHistoryDisplay> rows = new ArrayList<>();
			if (result != null) {
				for (Order order : result) {
					List<com.example.orderfood.model.OrderItem> items = appDatabase.orderItemDao().getByOrder(order.getId());
					OrderHistoryAdapter.OrderHistoryDisplay d = new OrderHistoryAdapter.OrderHistoryDisplay();
					d.orderId = order.getId();
					d.totalAmount = order.getTotalAmount();
					d.createdAt = order.getCreatedAt();
					d.itemsCount = items == null ? 0 : items.size();
					if (items != null && !items.isEmpty()) {
						com.example.orderfood.model.OrderItem first = items.get(0);
						com.example.orderfood.model.Product p = appDatabase.productDao().getById(first.getProductId());
						if (p != null) {
							d.productName = p.getName();
							d.productImageId = p.getImageId();
						} else {
							d.productName = "Product";
							d.productImageId = R.mipmap.ic_launcher;
						}
					} else {
						d.productName = "Order Items";
						d.productImageId = R.mipmap.ic_launcher;
					}
					rows.add(d);
				}
			}
			handler.post(() -> {
				displays.clear();
				displays.addAll(rows);
				adapter.notifyDataSetChanged();
			});
		});
	}
}


