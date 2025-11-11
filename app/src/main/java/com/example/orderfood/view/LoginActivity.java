package com.example.orderfood.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlertDialog("Login Failed", "Please fill in all fields");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            User user = appDatabase.userDao().login(username, password);
            handler.post(() -> {
                if (user != null) {
                    // Save user session to SharedPreferences
                    getSharedPreferences("orderfood_prefs", MODE_PRIVATE)
                            .edit()
                            .putInt("current_user_id", user.getId())
                            .apply();
                    
                    showAlertDialog("Login Successful", "Welcome " + user.getFullName(), (dialog, which) -> {
                        Intent intent = new Intent(LoginActivity.this, ProductListActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    showAlertDialog("Login Failed", "Invalid username or password.");
                }
            });
        });
    }

    private void showAlertDialog(String title, String message) {
        showAlertDialog(title, message, null);
    }

    private void showAlertDialog(String title, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .show();
    }
}
