package com.example.orderfood.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductListAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private SearchView searchView;
    private ImageButton cartButton;
    private ImageButton chatButton;
    private ImageButton historyButton;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        setupRecyclerView();
        loadProductsFromDatabase();
        setupSearchView();
        setupCartButton();
        setupChatButton();
        setupHistoryButton();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.productListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductListAdapter(this, productList);
        recyclerView.setAdapter(adapter);
    }

    private void loadProductsFromDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            if (appDatabase.productDao().getProductCount() == 0) {
                List<Product> initialProducts = new ArrayList<>();
                initialProducts.add(new Product(1, "Pizza", "Fast Food", 10.99, 4.5f, "A classic pizza with rich tomato sauce, mozzarella cheese, and pepperoni topping, baked to perfection.", R.drawable.pizza));
                initialProducts.add(new Product(2, "Burger", "Fast Food", 5.99, 4.2f, "A juicy beef patty with fresh lettuce, tomatoes, onions, and our special sauce, all in a toasted sesame seed bun.", R.drawable.burger));
                initialProducts.add(new Product(3, "Salad", "Healthy", 7.99, 4.8f, "A mix of fresh greens, cherry tomatoes, cucumbers, and bell peppers, tossed in a light vinaigrette dressing.", R.drawable.salad));
                appDatabase.productDao().insertAll(initialProducts);
            }

            List<Product> productsFromDb = appDatabase.productDao().getAllProducts();

            handler.post(() -> {
                productList.clear();
                productList.addAll(productsFromDb);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void setupCartButton() {
        cartButton = findViewById(R.id.cartButton);
        cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void setupChatButton() {
        chatButton = findViewById(R.id.chatButton);
        if (chatButton != null) {
            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProductListActivity.this, ChatActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupHistoryButton() {
        historyButton = findViewById(R.id.historyButton);
        if (historyButton != null) {
            historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProductListActivity.this, OrderHistoryActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product item : productList) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.filterList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning to this activity, reload products to reflect any changes
        // This is a simple approach. For more complex apps, consider using ViewModel and LiveData.
        loadProductsFromDatabase();
    }
}
