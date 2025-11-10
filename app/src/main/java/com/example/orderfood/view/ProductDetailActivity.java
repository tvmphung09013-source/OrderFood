package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.Product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";
    private AppDatabase appDatabase;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());
        currentProduct = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);

        ImageView productImageView = findViewById(R.id.productDetailImageView);
        TextView productNameTextView = findViewById(R.id.productDetailNameTextView);
        TextView productPriceTextView = findViewById(R.id.productDetailPriceTextView);
        TextView productCategoryTextView = findViewById(R.id.productDetailCategoryTextView);
        TextView productDescriptionTextView = findViewById(R.id.productDetailDescriptionTextView);
        ImageButton backButton = findViewById(R.id.detailBackButton);
        Button addToCartButton = findViewById(R.id.addToCartButton);

        if (currentProduct != null) {
            int imageId = currentProduct.getImageId();
            try {
                if (imageId > 0) {
                    productImageView.setImageResource(imageId);
                } else {
                    productImageView.setImageResource(R.drawable.default_product_image);
                }
            } catch (Exception e) {
                android.util.Log.e("ProductDetailActivity", "Lỗi setImageResource: " + imageId, e);
                productImageView.setImageResource(R.drawable.default_product_image);
            }
            productNameTextView.setText(currentProduct.getName());
            productPriceTextView.setText(String.format("$%.2f", currentProduct.getPrice()));
            productCategoryTextView.setText(currentProduct.getCategory());
            productDescriptionTextView.setText(currentProduct.getDescription());
        } else {
            Toast.makeText(this, "Product not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton.setOnClickListener(v -> finish());
        addToCartButton.setOnClickListener(v -> addToCart());
    }

    private void addToCart() {
        if (currentProduct == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            CartItem existingItem = appDatabase.cartDao().getCartItemById(currentProduct.getId());

            if (existingItem != null) {
                // Item exists, update quantity
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                appDatabase.cartDao().update(existingItem);
            } else {
                // Item does not exist, insert new
                CartItem newItem = new CartItem(currentProduct, 1);
                appDatabase.cartDao().insert(newItem);
            }

            handler.post(() -> {
                Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                finish(); // Go back to the product list immediately
            });
        });
    }
}
