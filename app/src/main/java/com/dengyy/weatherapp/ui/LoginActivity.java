package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository(this);
        if (userRepository.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        EditText usernameInput = findViewById(R.id.input_username);
        EditText passwordInput = findViewById(R.id.input_password);
        Button loginButton = findViewById(R.id.button_login);
        TextView registerEntry = findViewById(R.id.text_to_register);
        TextView resetEntry = findViewById(R.id.text_to_reset_password);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                ToastUtils.showShort(this, "请输入用户名和密码");
                return;
            }
            if (userRepository.login(username, password)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                ToastUtils.showShort(this, "用户名或密码错误");
            }
        });

        registerEntry.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        resetEntry.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));
    }
}
