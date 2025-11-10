package com.example.orderfood.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.CartItem;
import com.example.orderfood.model.Order;
import com.example.orderfood.model.ShippingAddress;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutActivity extends AppCompatActivity {
    private AppDatabase appDatabase;
    private List<CartItem> cartItems = new ArrayList<>();
    private ShippingAddress shippingAddress;
    private RecyclerView orderRecyclerView;
    private TextView productFeeText, shippingFeeText, totalFeeText;
    private LinearLayout shippingFormLayout, shippingInfoLayout;
    private EditText editName, editPhone, editAddress;
    private Button saveShippingButton, editShippingButton, confirmPaymentButton;
    private TextView nameInfo, phoneInfo, addressInfo;

    private static final double SHIPPING_FEE = 2.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        appDatabase = AppDatabase.getDatabase(getApplicationContext());
        initViews();
        loadCartItems();
        loadShippingAddress();
        setupListeners();
    }

    private void initViews() {
        orderRecyclerView = findViewById(R.id.checkoutOrderRecyclerView);
        productFeeText = findViewById(R.id.checkoutProductFee);
        shippingFeeText = findViewById(R.id.checkoutShippingFee);
        totalFeeText = findViewById(R.id.checkoutTotalFee);
        shippingFormLayout = findViewById(R.id.shippingAddressForm);
        shippingInfoLayout = findViewById(R.id.shippingAddressInfo);
        editName = findViewById(R.id.editShippingName);
        editPhone = findViewById(R.id.editShippingPhone);
        editAddress = findViewById(R.id.editShippingAddress);
        saveShippingButton = findViewById(R.id.saveShippingButton);
        editShippingButton = findViewById(R.id.editShippingButton);
        confirmPaymentButton = findViewById(R.id.confirmPaymentButton);
        nameInfo = findViewById(R.id.shippingNameInfo);
        phoneInfo = findViewById(R.id.shippingPhoneInfo);
        addressInfo = findViewById(R.id.shippingAddressInfoText);
    }

    private void loadCartItems() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<CartItem> items = appDatabase.cartDao().getAllCartItems();
            handler.post(() -> {
                cartItems.clear();
                cartItems.addAll(items);
                orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                orderRecyclerView.setAdapter(new CheckoutOrderAdapter(cartItems));
                updateFees();
            });
        });
    }

    private void updateFees() {
        double productFee = 0;
        for (CartItem item : cartItems) {
            productFee += item.getProduct().getPrice() * item.getQuantity();
        }
        productFeeText.setText(String.format(Locale.getDefault(), "$%.2f", productFee));
        shippingFeeText.setText(String.format(Locale.getDefault(), "$%.2f", SHIPPING_FEE));
        totalFeeText.setText(String.format(Locale.getDefault(), "$%.2f", productFee + SHIPPING_FEE));
    }

    private void loadShippingAddress() {
        SharedPreferences prefs = getSharedPreferences("checkout_prefs", MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String phone = prefs.getString("phone", "");
        String address = prefs.getString("address", "");
        if (!name.isEmpty() && !phone.isEmpty() && !address.isEmpty()) {
            shippingAddress = new ShippingAddress(name, phone, address);
            showShippingInfo();
        } else {
            showShippingForm();
        }
    }

    private void showShippingForm() {
        shippingFormLayout.setVisibility(View.VISIBLE);
        shippingInfoLayout.setVisibility(View.GONE);
        if (shippingAddress != null) {
            editName.setText(shippingAddress.getName());
            editPhone.setText(shippingAddress.getPhone());
            editAddress.setText(shippingAddress.getAddress());
        } else {
            editName.setText("");
            editPhone.setText("");
            editAddress.setText("");
        }
    }

    private void showShippingInfo() {
        shippingFormLayout.setVisibility(View.GONE);
        shippingInfoLayout.setVisibility(View.VISIBLE);
        nameInfo.setText(String.format(getString(R.string.name_info) + "%s", shippingAddress.getName()));
        phoneInfo.setText(String.format(getString(R.string.phone_info) + "%s", shippingAddress.getPhone()));
        addressInfo.setText(String.format(getString(R.string.address_info) + "%s", shippingAddress.getAddress()));
    }

    private void setupListeners() {
        saveShippingButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            shippingAddress = new ShippingAddress(name, phone, address);
            SharedPreferences.Editor editor = getSharedPreferences("checkout_prefs", MODE_PRIVATE).edit();
            editor.putString("name", name);
            editor.putString("phone", phone);
            editor.putString("address", address);
            editor.apply();
            showShippingInfo();
        });
        editShippingButton.setOnClickListener(v -> showShippingForm());
        confirmPaymentButton.setOnClickListener(v -> confirmOrder());
    }

    private void confirmOrder() {
        if (shippingAddress == null) {
            Toast.makeText(this, "Please enter shipping address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        double productFee = 0;
        StringBuilder itemsStr = new StringBuilder();
        for (CartItem item : cartItems) {
            productFee += item.getProduct().getPrice() * item.getQuantity();
            itemsStr.append(item.getProductId()).append(":").append(item.getQuantity()).append(",");
        }
        if (itemsStr.length() > 0) itemsStr.setLength(itemsStr.length() - 1);
        double total = productFee + SHIPPING_FEE;
        Order order = new Order(0, total, "Pending", new Date(), shippingAddress, itemsStr.toString());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            appDatabase.orderDao().insert(order);
            appDatabase.cartDao().deleteAll();
            handler.post(() -> {
                Intent intent = new Intent(CheckoutActivity.this, FinalOrderActivity.class);
                intent.putExtra("order_info", order);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
