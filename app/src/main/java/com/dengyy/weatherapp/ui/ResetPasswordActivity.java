package com.dengyy.weatherapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.ToastUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        UserRepository userRepository = new UserRepository(this);
        EditText usernameInput = findViewById(R.id.input_username);
        EditText identityInput = findViewById(R.id.input_identity);
        EditText passwordInput = findViewById(R.id.input_password);
        Button resetButton = findViewById(R.id.button_reset_password);

        resetButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String identity = identityInput.getText().toString().trim();
            String newPassword = passwordInput.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(identity) || TextUtils.isEmpty(newPassword)) {
                ToastUtils.showShort(this, "请完善重置密码信息");
                return;
            }
            if (userRepository.resetPassword(username, identity, newPassword)) {
                ToastUtils.showShort(this, "密码已重置");
                finish();
            } else {
                ToastUtils.showShort(this, "身份校验失败");
            }
        });
    }
}
