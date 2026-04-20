package com.dengyy.weatherapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.ToastUtils;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UserRepository userRepository = new UserRepository(this);
        EditText usernameInput = findViewById(R.id.input_username);
        EditText emailInput = findViewById(R.id.input_email);
        EditText phoneInput = findViewById(R.id.input_phone);
        EditText passwordInput = findViewById(R.id.input_password);
        EditText confirmPasswordInput = findViewById(R.id.input_confirm_password);
        Button registerButton = findViewById(R.id.button_register);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                ToastUtils.showShort(this, "用户名和密码不能为空");
                return;
            }
            if (!TextUtils.equals(password, confirmPassword)) {
                ToastUtils.showShort(this, "两次密码输入不一致");
                return;
            }

            long rowId = userRepository.register(username, password, email, phone);
            if (rowId > 0) {
                ToastUtils.showShort(this, "注册成功，请登录");
                finish();
            } else {
                ToastUtils.showShort(this, "注册失败，用户名可能已存在");
            }
        });
    }
}
