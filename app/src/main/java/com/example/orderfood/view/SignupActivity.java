package com.example.orderfood.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfood.R;
import com.example.orderfood.database.AppDatabase;
import com.example.orderfood.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, emailEditText, fullNameEditText;
    private Button signUpButton;
    private ImageButton backButton;
    private AppDatabase appDatabase;

    // Name validation pattern (allows letters and spaces)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        signUpButton = findViewById(R.id.signUpButton);
        backButton = findViewById(R.id.backButton);

        appDatabase = AppDatabase.getDatabase(getApplicationContext());

        signUpButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Check if username or email already exists
            User existingUserByUsername = appDatabase.userDao().getUserByUsername(username);
            User existingUserByEmail = appDatabase.userDao().getUserByEmail(email);

            handler.post(() -> {
                if (existingUserByUsername != null) {
                    showAlertDialog("Sign Up Failed", "Username already exists.");
                    return;
                }

                if (existingUserByEmail != null) {
                    showAlertDialog("Sign Up Failed", "Email already exists.");
                    return;
                }

                // Insert new user
                User newUser = new User(username, password, email, fullName);
                executor.execute(() -> {
                    appDatabase.userDao().insert(newUser);
                    handler.post(() -> {
                        showAlertDialog("Sign Up Successful", "Your account has been created.", (dialog, which) -> finish());
                    });
                });
            });
        });
    }


    private boolean validateInput() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();

        if (username.isEmpty()) {
            showAlertDialog("Invalid Input", "Username is required");
            return false;
        }

        if (password.isEmpty()) {
            showAlertDialog("Invalid Input", "Password is required");
            return false;
        }

        if (password.length() < 6) {
            showAlertDialog("Invalid Input", "Password must be at least 6 characters long");
            return false;
        }

        if (email.isEmpty()) {
            showAlertDialog("Invalid Input", "Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlertDialog("Invalid Input", "Please enter a valid email address");
            return false;
        }

        if (fullName.isEmpty()) {
            showAlertDialog("Invalid Input", "Full name is required");
            return false;
        }

        if (!NAME_PATTERN.matcher(fullName).matches()) {
            showAlertDialog("Invalid Input", "Please enter a valid name (letters and spaces only)");
            return false;
        }

        return true;
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
