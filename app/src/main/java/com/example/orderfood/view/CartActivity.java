package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
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

    private Button checkoutButton;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        recyclerView = findViewById(R.id.cartRecyclerView);
        backButton = findViewById(R.id.cartBackButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutButton = findViewById(R.id.checkoutButton);
        adapter = new CartAdapter(cartItemList, this);
        recyclerView.setAdapter(adapter);

        loadCartItems();

        backButton.setOnClickListener(v -> finish());
    }

    private void loadCartItems() {
        checkoutButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(CartActivity.this, CheckoutActivity.class));
        });
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
}
