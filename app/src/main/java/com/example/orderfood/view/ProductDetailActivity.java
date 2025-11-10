package com.example.orderfood.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.Product;
import com.example.orderfood.model.Review;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";
    private AppDatabase appDatabase;
    private Product currentProduct;

    private RatingBar reviewRatingBar;
    private EditText reviewCommentEditText;
    private Button submitReviewButton;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();

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
        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        reviewCommentEditText = findViewById(R.id.reviewCommentEditText);
        submitReviewButton = findViewById(R.id.submitReviewButton);
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
            loadReviews();
        }

        backButton.setOnClickListener(v -> finish());
        addToCartButton.setOnClickListener(v -> addToCart());

        reviewRatingBar.setIsIndicator(false);
        reviewRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // No-op: keeping selection is enough; submission handles persistence
        });

        submitReviewButton.setOnClickListener(v -> submitReview());
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
            List<Review> reviews = appDatabase.reviewDao().getByProduct(currentProduct.getId());
            Float avg = appDatabase.reviewDao().getAverageRatingForProduct(currentProduct.getId());
            handler.post(() -> {
                reviewList.clear();
                if (reviews != null) {
                    reviewList.addAll(reviews);
                }
                reviewAdapter.notifyDataSetChanged();
                if (avg != null) {
                    // Optionally show average somewhere; here we update the product in-memory
                    currentProduct.setRating(avg);
                }
            });
        });
    }

    private void submitReview() {
        if (currentProduct == null) return;
        final float rating = reviewRatingBar.getRating();
        final String comment = reviewCommentEditText.getText() == null ? "" : reviewCommentEditText.getText().toString().trim();
        if (rating <= 0f) {
            Toast.makeText(this, "Vui lòng chọn số sao.", Toast.LENGTH_SHORT).show();
            return;
        }
        final int userId = getSharedPreferences("orderfood_prefs", MODE_PRIVATE).getInt("current_user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Bạn cần đăng nhập để đánh giá.", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Review review = new Review(currentProduct.getId(), userId, rating, comment, System.currentTimeMillis());
            appDatabase.reviewDao().insert(review);
            handler.post(() -> {
                Toast.makeText(ProductDetailActivity.this, "Đã gửi đánh giá.", Toast.LENGTH_SHORT).show();
                reviewRatingBar.setRating(0f);
                reviewCommentEditText.setText("");
                loadReviews();
            });
        });
    }
}
