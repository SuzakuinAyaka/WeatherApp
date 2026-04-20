package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.FormUiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends BaseActivity {

    private UserRepository userRepository;
    private TextInputLayout accountLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputEditText accountInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupPageBehavior(R.id.root_container);
        userRepository = new UserRepository(this);

        initViews();
    }

    private void initViews() {
        View rootView = findViewById(R.id.root_container);
        accountLayout = findViewById(R.id.layout_account);
        phoneLayout = findViewById(R.id.layout_phone);
        passwordLayout = findViewById(R.id.layout_password);
        confirmPasswordLayout = findViewById(R.id.layout_confirm_password);
        emailLayout = findViewById(R.id.layout_email);
        accountInput = findViewById(R.id.input_account);
        phoneInput = findViewById(R.id.input_phone);
        passwordInput = findViewById(R.id.input_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_password);
        emailInput = findViewById(R.id.input_email);
        MaterialButton registerButton = findViewById(R.id.button_register);
        MaterialButton backToLoginButton = findViewById(R.id.button_back_to_login);

        FormUiUtils.bindFieldBehavior(
                accountLayout,
                accountInput,
                getString(R.string.label_account),
                getString(R.string.hint_account_input)
        );
        FormUiUtils.bindFieldBehavior(
                phoneLayout,
                phoneInput,
                getString(R.string.label_phone),
                getString(R.string.hint_phone_input)
        );
        FormUiUtils.bindFieldBehavior(
                passwordLayout,
                passwordInput,
                getString(R.string.label_password),
                getString(R.string.hint_password_input)
        );
        FormUiUtils.bindFieldBehavior(
                confirmPasswordLayout,
                confirmPasswordInput,
                getString(R.string.label_confirm_password),
                getString(R.string.hint_confirm_password_input)
        );
        FormUiUtils.bindFieldBehavior(
                emailLayout,
                emailInput,
                getString(R.string.label_email_optional),
                getString(R.string.hint_email_input)
        );
        FormUiUtils.moveFocusOnEditorAction(accountInput, phoneInput);
        FormUiUtils.moveFocusOnEditorAction(phoneInput, passwordInput);
        FormUiUtils.moveFocusOnEditorAction(passwordInput, confirmPasswordInput);
        FormUiUtils.moveFocusOnEditorAction(confirmPasswordInput, emailInput);
        FormUiUtils.submitOnEditorAction(emailInput, this::attemptRegister);
        FormUiUtils.clearFocusWhenTapOutside(rootView, accountInput, phoneInput, passwordInput, confirmPasswordInput, emailInput);

        registerButton.setOnClickListener(v -> attemptRegister());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        clearErrors();

        String account = getText(accountInput);
        String email = getText(emailInput);
        String phone = getText(phoneInput);
        String password = getText(passwordInput);
        String confirmPassword = getText(confirmPasswordInput);

        boolean hasError = false;
        if (TextUtils.isEmpty(account)) {
            accountLayout.setError(getString(R.string.error_account_required));
            hasError = true;
        } else if (account.length() < 3) {
            accountLayout.setError(getString(R.string.error_account_too_short));
            hasError = true;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneLayout.setError(getString(R.string.error_phone_required));
            hasError = true;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneLayout.setError(getString(R.string.error_invalid_phone));
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_required));
            hasError = true;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_password_too_short));
            hasError = true;
        }

        if (!TextUtils.equals(password, confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_password_not_match));
            hasError = true;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        UserRepository.RegisterResult result = userRepository.register(account, password, email, phone);
        if (result == UserRepository.RegisterResult.SUCCESS) {
            Intent data = new Intent();
            data.putExtra(LoginActivity.EXTRA_USERNAME, account);
            setResult(RESULT_OK, data);
            finish();
            return;
        }
        if (result == UserRepository.RegisterResult.USERNAME_EXISTS) {
            accountLayout.setError(getString(R.string.error_account_exists));
            showMessage(getString(R.string.error_account_exists));
        } else {
            showMessage(getString(R.string.message_register_failed));
        }
    }

    private void clearErrors() {
        accountLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        emailLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
