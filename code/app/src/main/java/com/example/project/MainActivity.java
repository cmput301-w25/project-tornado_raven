package com.example.project;

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

public class MainActivity extends AppCompatActivity {

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
            if (validateLogin(username, password)) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 关闭弹窗
            } else {
                Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
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
                    Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    // return to login interface
                    registerLayout.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
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
    private boolean validateLogin(String username, String password) {
        return username.equals("admin") && password.equals("1234");
    }
}