package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        // show login/register dialog
        showLoginRegisterDialog();

    }

    /**
     * Pop up dialog enforcing the users to login or register
     */
    private void showLoginRegisterDialog() {
        // load xml
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.login_register_dialog, null);

        // get interface controls
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

        // create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);       // do not allow clicking outside to cancel
        AlertDialog dialog = builder.create();
        dialog.show();

        // set click listeners
        // login button
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            validateLogin(username,password, new LoginCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    Intent intent = new Intent(LoginActivity.this, MoodHistoryActivity.class);
                    startActivity(intent); // invoke new activity
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // switch to register interface
        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginLayout.setVisibility(View.GONE);
                registerLayout.setVisibility(View.VISIBLE);
            }
        });

        // register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = etRegisterUsername.getText().toString();
                String newPassword = etRegisterPassword.getText().toString();
                String confirmPassword = etRegisterConfirmPassword.getText().toString();

                if (newPassword.equals(confirmPassword) && !newUsername.isEmpty()) {
                    registerUser(newUsername, newPassword, new RegisterCallback() {
                        @Override
                        public void onSuccess(String username) {
                            Toast.makeText(LoginActivity.this, "Register successful: ( " + username + ")", Toast.LENGTH_SHORT).show();
                            // switch back to login
                            registerLayout.setVisibility(View.GONE);
                            loginLayout.setVisibility(View.VISIBLE);
                            etUsername.setText("");
                            etPassword.setText("");
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "password does not match or username is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // switch back to login interface
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);

            }
        });


    }

    /**
     * Verify user login msg is valid
     * @param username username the user input
     * @param password password the user input
     * @return true if both username matched the database and password matched the username, false otherwise
     */
    private void validateLogin(String username, String password, LoginCallback callback) {
        if (username.equals("admin") && password.equals("1234")) {
            callback.onSuccess();
        }
        userRef.whereEqualTo("username",username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        String storedPassword = queryDocumentSnapshots.getDocuments().get(0).getString("password");
                        if (storedPassword != null && storedPassword.equals(password)) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Wrong password");
                        }
                    } else {
                        callback.onFailure("Username does not exist");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Database error"));
    }

    private void registerUser(String username, String password, RegisterCallback callback) {
        userRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure("username already exists");
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("username", username);
                        data.put("password", password);
                        userRef.add(data)
                                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                                .addOnFailureListener(e -> callback.onFailure("Register Failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Database Error: " + e.getMessage()));
    }

    /**
     * login callback interface to check the callback result
     */
    interface LoginCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface RegisterCallback {
        void onSuccess(String username);
        void onFailure(String errorMessage);
    }

}