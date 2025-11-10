package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.Product;
import com.example.orderfood.model.Review;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";
    private AppDatabase appDatabase;
    private Product currentProduct;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private java.util.List<Review> reviewList = new java.util.ArrayList<>();

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
        android.widget.RatingBar ratingBar = findViewById(R.id.reviewRatingBar);
        EditText reviewCommentEditText = findViewById(R.id.reviewCommentEditText);
        Button submitReviewButton = findViewById(R.id.submitReviewButton);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        if (currentProduct != null) {
            productImageView.setImageResource(currentProduct.getImageId());
            productNameTextView.setText(currentProduct.getName());
            productPriceTextView.setText(String.format("$%.2f", currentProduct.getPrice()));
            productCategoryTextView.setText(currentProduct.getCategory());
            productDescriptionTextView.setText(currentProduct.getDescription());
        }

        backButton.setOnClickListener(v -> finish());
        addToCartButton.setOnClickListener(v -> addToCart());
        submitReviewButton.setOnClickListener(v -> submitReview(Math.round(ratingBar.getRating()), reviewCommentEditText.getText().toString().trim()));

        loadReviews();
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

    private void loadReviews() {
        if (currentProduct == null) return;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            java.util.List<Review> reviews = appDatabase.reviewDao().getByProduct(currentProduct.getId());
            handler.post(() -> {
                reviewList.clear();
                reviewList.addAll(reviews);
                reviewAdapter.notifyDataSetChanged();
            });
        });
    }

    private void submitReview(int rating, String comment) {
        if (currentProduct == null) return;
        if (rating <= 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = getSharedPreferences("orderfood_prefs", MODE_PRIVATE).getInt("current_user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please login to submit a review", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Review review = new Review(currentProduct.getId(), userId, (float) rating, comment, System.currentTimeMillis());
            appDatabase.reviewDao().insert(review);
            Float avg = appDatabase.reviewDao().getAverageRatingForProduct(currentProduct.getId());
            if (avg != null) {
                appDatabase.productDao().updateRating(currentProduct.getId(), avg);
            }
            handler.post(() -> {
                Toast.makeText(ProductDetailActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                loadReviews();
            });
        });
    }
}
