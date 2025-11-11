package com.example.orderfood.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;

public class BillingActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView totalAmountTextView;
    private TextView itemCountTextView;
    private RadioGroup paymentMethodGroup;
    private Button confirmPaymentButton;
    
    private double totalAmount;
    private int itemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        backButton = findViewById(R.id.billingBackButton);
        totalAmountTextView = findViewById(R.id.billingTotalAmountTextView);
        itemCountTextView = findViewById(R.id.billingItemCountTextView);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

        // Get data from intent
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        itemCount = intent.getIntExtra("item_count", 0);

        setupUI();
        setupBackButton();
        setupConfirmButton();
    }

    private void setupUI() {
        totalAmountTextView.setText(String.format("$%.2f", totalAmount));
        itemCountTextView.setText(itemCount + " item(s)");
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupConfirmButton() {
        confirmPaymentButton.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        int selectedPaymentId = paymentMethodGroup.getCheckedRadioButtonId();
        
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedPaymentMethod = findViewById(selectedPaymentId);
        String paymentMethod = selectedPaymentMethod.getText().toString();

        // Show success message
        Toast.makeText(this, "Payment processed successfully via " + paymentMethod, Toast.LENGTH_LONG).show();

        // Navigate back to product list after a short delay
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(BillingActivity.this, ProductListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
