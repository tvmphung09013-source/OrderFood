package com.example.orderfood.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.Product;
import com.example.orderfood.util.StoreInfo;

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

    private ImageButton locationButton;

    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        setupRecyclerView();
        loadProductsFromDatabase();
        setupSearchView();
        setupLocationButton();
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

    private void setupLocationButton() {
        locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(v -> {
            openGoogleMaps();
        });
    }

    private void openGoogleMaps() {
        // Create a geo URI with the store location
        String geoUriString = "geo:" + StoreInfo.STORE_LAT + "," + StoreInfo.STORE_LNG 
                + "?q=" + StoreInfo.STORE_LAT + "," + StoreInfo.STORE_LNG 
                + "(" + Uri.encode(StoreInfo.STORE_LABEL) + ")";
        Uri geoUri = Uri.parse(geoUriString);
        
        // Create intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        
        try {
            // Try to open Google Maps app
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // Fallback to web browser if Google Maps app is not installed
            String webUrl = "https://www.google.com/maps/search/?api=1&query=" 
                    + StoreInfo.STORE_LAT + "," + StoreInfo.STORE_LNG;
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            startActivity(webIntent);
        }
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
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }

	private void setupHistoryButton() {
		historyButton = findViewById(R.id.historyButton);
		historyButton.setOnClickListener(v -> {
			Intent intent = new Intent(ProductListActivity.this, OrderHistoryActivity.class);
			startActivity(intent);
		});
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
