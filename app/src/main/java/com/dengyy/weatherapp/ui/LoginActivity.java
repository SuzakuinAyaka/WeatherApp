package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.FormUiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    public static final String EXTRA_USERNAME = "extra_username";

    private UserRepository userRepository;
    private TextInputLayout accountLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText accountInput;
    private TextInputEditText passwordInput;
    private ActivityResultLauncher<Intent> registerLauncher;

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
        setupPageBehavior(R.id.root_container);
        initRegisterLauncher();
        initViews();
        fillAccountFromIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fillAccountFromIntent(intent);
    }

    private void initRegisterLauncher() {
        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String account = result.getData().getStringExtra(EXTRA_USERNAME);
                        if (!TextUtils.isEmpty(account)) {
                            accountInput.setText(account);
                            passwordInput.requestFocus();
                            showMessage(getString(R.string.message_register_success));
                        }
                    }
                }
        );
    }

    private void initViews() {
        View rootView = findViewById(R.id.root_container);
        accountLayout = findViewById(R.id.layout_account);
        passwordLayout = findViewById(R.id.layout_password);
        accountInput = findViewById(R.id.input_account);
        passwordInput = findViewById(R.id.input_password);
        MaterialButton loginButton = findViewById(R.id.button_login);
        MaterialButton registerButton = findViewById(R.id.button_to_register);
        MaterialButton resetPasswordButton = findViewById(R.id.button_to_reset_password);

        FormUiUtils.bindFieldBehavior(
                accountLayout,
                accountInput,
                getString(R.string.label_account),
                getString(R.string.hint_account_input)
        );
        FormUiUtils.bindFieldBehavior(
                passwordLayout,
                passwordInput,
                getString(R.string.label_password),
                getString(R.string.hint_password_input)
        );
        FormUiUtils.moveFocusOnEditorAction(accountInput, passwordInput);
        FormUiUtils.submitOnEditorAction(passwordInput, this::attemptLogin);
        FormUiUtils.clearFocusWhenTapOutside(rootView, accountInput, passwordInput);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> registerLauncher.launch(new Intent(this, RegisterActivity.class)));
        resetPasswordButton.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));
    }

    private void attemptLogin() {
        clearErrors();
        String account = getText(accountInput);
        String password = getText(passwordInput);

        boolean hasError = false;
        if (TextUtils.isEmpty(account)) {
            accountLayout.setError(getString(R.string.error_account_required));
            hasError = true;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_required));
            hasError = true;
        }
        if (hasError) {
            return;
        }

        UserRepository.LoginResult result = userRepository.login(account, password);
        if (result == UserRepository.LoginResult.SUCCESS) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        if (result == UserRepository.LoginResult.USER_NOT_FOUND) {
            accountLayout.setError(getString(R.string.error_user_not_found));
        } else {
            passwordLayout.setError(getString(R.string.error_password_incorrect));
        }
        showMessage(getString(R.string.message_login_failed));
    }

    private void fillAccountFromIntent(Intent intent) {
        if (intent == null || accountInput == null) {
            return;
        }
        String account = intent.getStringExtra(EXTRA_USERNAME);
        if (!TextUtils.isEmpty(account)) {
            accountInput.setText(account);
        }
    }

    private void clearErrors() {
        accountLayout.setError(null);
        passwordLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
