package com.example.orderfood.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.model.Order;
import com.example.orderfood.model.ShippingAddress;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FinalOrderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_order);

        TextView nameText = findViewById(R.id.finalOrderName);
        TextView addressText = findViewById(R.id.finalOrderAddress);
        TextView phoneText = findViewById(R.id.finalOrderPhone);
        TextView totalText = findViewById(R.id.finalOrderTotal);
        TextView statusText = findViewById(R.id.finalOrderStatus);
        TextView dateText = findViewById(R.id.finalOrderDate);
        Button backButton = findViewById(R.id.backToHomeButton);

        Order order = (Order) getIntent().getSerializableExtra("order_info");
        if (order != null) {
            ShippingAddress address = order.getShippingAddress();
            nameText.setText("Name: " + address.getName());
            addressText.setText("Address: " + address.getAddress());
            phoneText.setText("Phone: " + address.getPhone());
            totalText.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotalPrice()));
            statusText.setText("Status: " + order.getStatus());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            dateText.setText("Date: " + sdf.format(order.getOrderDate()));
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(FinalOrderActivity.this, ProductListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

