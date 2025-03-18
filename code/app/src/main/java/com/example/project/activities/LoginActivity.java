package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Reuse or rename "activity_main.xml" as your login layout
        showLoginRegisterDialog();
    }

    /**
     * Show a dialog for login or register.
     */
    private void showLoginRegisterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.login_register_dialog, null);

        LinearLayout loginLayout = dialogView.findViewById(R.id.login_layout);
        LinearLayout registerLayout = dialogView.findViewById(R.id.register_layout);

        EditText etUsername = dialogView.findViewById(R.id.et_login_username);
        EditText etPassword = dialogView.findViewById(R.id.et_login_password);
        Button btnLogin = dialogView.findViewById(R.id.btn_login);
        Button btnGoToRegister = dialogView.findViewById(R.id.btn_go_to_register);

        EditText etRegisterUsername = dialogView.findViewById(R.id.et_register_username);
        EditText etRegisterPassword = dialogView.findViewById(R.id.et_register_password);
        EditText etRegisterConfirmPassword = dialogView.findViewById(R.id.et_register_confirm_password);
        Button btnRegister = dialogView.findViewById(R.id.btn_register);
        Button btnBackToLogin = dialogView.findViewById(R.id.btn_back_to_login);

        Builder builder = new Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Switch to register layout
        btnGoToRegister.setOnClickListener(v -> {
            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        });

        // Switch back to login layout
        btnBackToLogin.setOnClickListener(v -> {
            registerLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });

        // Login
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            validateLogin(username, password, new LoginCallback() {
                @Override
                public void onSuccess() {
                    // Store user profile by using SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.putString("username", username); // store username (unique)
                    editor.putString("password", password); // store the password
                    editor.apply(); // save changes

                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Launch the home page: MoodHistoryActivity
                    Intent intent = new Intent(LoginActivity.this, MoodHistoryActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Register
        btnRegister.setOnClickListener(v -> {
            String newUsername = etRegisterUsername.getText().toString().trim();
            String newPassword = etRegisterPassword.getText().toString().trim();
            String confirmPassword = etRegisterConfirmPassword.getText().toString().trim();

            if (!newUsername.isEmpty() && newPassword.equals(confirmPassword)) {
                registerUser(newUsername, newPassword, new RegisterCallback() {
                    @Override
                    public void onSuccess(String docId) {
                        Toast.makeText(LoginActivity.this, "Registered user: " + docId, Toast.LENGTH_SHORT).show();
                        // Switch back to login
                        registerLayout.setVisibility(View.GONE);
                        loginLayout.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Password mismatch or empty username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateLogin(String username, String password, LoginCallback callback) {
        if (username.equals("admin") && password.equals("1234")) {
            callback.onSuccess();
            return;
        }
        userRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        String storedPassword = snap.getDocuments().get(0).getString("password");
                        if (storedPassword != null && storedPassword.equals(password)) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Wrong password");
                        }
                    } else {
                        callback.onFailure("Username does not exist");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Database error: " + e.getMessage()));
    }

    private void registerUser(String username, String password, RegisterCallback callback) {
        userRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        callback.onFailure("Username already exists");
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("username", username);
                        data.put("password", password);
                        userRef.add(data)
                                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                                .addOnFailureListener(e -> callback.onFailure("Register Failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Database error: " + e.getMessage()));
    }

    interface LoginCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    interface RegisterCallback {
        void onSuccess(String docId);
        void onFailure(String errorMessage);
    }
}