package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.FormUiUtils;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends BaseActivity {

    public static final String EXTRA_USERNAME = "extra_username";

    private UserRepository userRepository;
    private EditText accountInput;
    private EditText passwordInput;
    private TextView accountErrorView;
    private TextView passwordErrorView;
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
        accountInput = findViewById(R.id.input_account);
        passwordInput = findViewById(R.id.input_password);
        accountErrorView = findViewById(R.id.text_error_account);
        passwordErrorView = findViewById(R.id.text_error_password);
        View loginButton = findViewById(R.id.button_login);
        TextView registerButton = findViewById(R.id.button_to_register);
        TextView resetPasswordButton = findViewById(R.id.button_to_reset_password);

        FormUiUtils.moveFocusOnEditorAction(accountInput, passwordInput);
        FormUiUtils.submitOnEditorAction(passwordInput, this::attemptLogin);
        FormUiUtils.clearFocusWhenTapOutside(rootView, accountInput, passwordInput);
        addErrorClearWatcher(accountInput);
        addErrorClearWatcher(passwordInput);

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
            showFieldError(accountInput, accountErrorView, getString(R.string.error_account_required));
            hasError = true;
        }
        if (TextUtils.isEmpty(password)) {
            showFieldError(passwordInput, passwordErrorView, getString(R.string.error_password_required));
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
            showFieldError(accountInput, accountErrorView, getString(R.string.error_user_not_found));
        } else {
            showFieldError(passwordInput, passwordErrorView, getString(R.string.error_password_incorrect));
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
        hideFieldError(accountInput, accountErrorView);
        hideFieldError(passwordInput, passwordErrorView);
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void addErrorClearWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText == accountInput) {
                    hideFieldError(accountInput, accountErrorView);
                } else if (editText == passwordInput) {
                    hideFieldError(passwordInput, passwordErrorView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showFieldError(EditText editText, TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
        editText.setActivated(true);
    }

    private void hideFieldError(EditText editText, TextView errorView) {
        errorView.setText(null);
        errorView.setVisibility(View.GONE);
        editText.setActivated(false);
    }
}
