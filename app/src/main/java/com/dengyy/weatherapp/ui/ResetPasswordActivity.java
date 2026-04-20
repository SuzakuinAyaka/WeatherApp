package com.dengyy.weatherapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.FormUiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends BaseActivity {

    private UserRepository userRepository;
    private TextInputLayout accountLayout;
    private TextInputLayout identityLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText accountInput;
    private TextInputEditText identityInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        setupPageBehavior(R.id.root_container);
        userRepository = new UserRepository(this);

        initViews();
    }

    private void initViews() {
        View rootView = findViewById(R.id.root_container);
        accountLayout = findViewById(R.id.layout_account);
        identityLayout = findViewById(R.id.layout_identity);
        passwordLayout = findViewById(R.id.layout_password);
        confirmPasswordLayout = findViewById(R.id.layout_confirm_password);
        accountInput = findViewById(R.id.input_account);
        identityInput = findViewById(R.id.input_identity);
        passwordInput = findViewById(R.id.input_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_password);
        MaterialButton resetButton = findViewById(R.id.button_reset_password);
        MaterialButton backToLoginButton = findViewById(R.id.button_back_to_login);

        FormUiUtils.bindFieldBehavior(
                accountLayout,
                accountInput,
                getString(R.string.label_account),
                getString(R.string.hint_account_input)
        );
        FormUiUtils.bindFieldBehavior(
                identityLayout,
                identityInput,
                getString(R.string.label_identity),
                getString(R.string.hint_identity_input)
        );
        FormUiUtils.bindFieldBehavior(
                passwordLayout,
                passwordInput,
                getString(R.string.label_new_password),
                getString(R.string.hint_password_input)
        );
        FormUiUtils.bindFieldBehavior(
                confirmPasswordLayout,
                confirmPasswordInput,
                getString(R.string.label_confirm_password),
                getString(R.string.hint_confirm_password_input)
        );
        FormUiUtils.moveFocusOnEditorAction(accountInput, identityInput);
        FormUiUtils.moveFocusOnEditorAction(identityInput, passwordInput);
        FormUiUtils.moveFocusOnEditorAction(passwordInput, confirmPasswordInput);
        FormUiUtils.submitOnEditorAction(confirmPasswordInput, this::attemptResetPassword);
        FormUiUtils.clearFocusWhenTapOutside(rootView, accountInput, identityInput, passwordInput, confirmPasswordInput);

        resetButton.setOnClickListener(v -> attemptResetPassword());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void attemptResetPassword() {
        clearErrors();

        String account = getText(accountInput);
        String identity = getText(identityInput);
        String password = getText(passwordInput);
        String confirmPassword = getText(confirmPasswordInput);

        boolean hasError = false;
        if (TextUtils.isEmpty(account)) {
            accountLayout.setError(getString(R.string.error_account_required));
            hasError = true;
        }
        if (TextUtils.isEmpty(identity)) {
            identityLayout.setError(getString(R.string.error_identity_required_single));
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

        if (hasError) {
            return;
        }

        UserRepository.ResetPasswordResult result = userRepository.resetPassword(account, identity, password);
        if (result == UserRepository.ResetPasswordResult.SUCCESS) {
            showMessage(getString(R.string.message_reset_password_success));
            finish();
            return;
        }
        if (result == UserRepository.ResetPasswordResult.USER_NOT_FOUND) {
            accountLayout.setError(getString(R.string.error_user_not_found));
            showMessage(getString(R.string.error_user_not_found));
        } else {
            identityLayout.setError(getString(R.string.error_identity_not_match));
            showMessage(getString(R.string.error_identity_not_match));
        }
    }

    private void clearErrors() {
        accountLayout.setError(null);
        identityLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
