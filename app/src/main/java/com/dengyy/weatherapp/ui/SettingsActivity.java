package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.utils.SPUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends BaseActivity {

    private static final int THEME_MODE_LIGHT = 0;
    private static final int THEME_MODE_DARK = 1;

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private View rootView;
    private LinearLayout themeModeLayout;
    private Spinner themeModeInput;
    private MaterialSwitch debugSampleSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupPageBehavior(R.id.settings_root);

        userRepository = new UserRepository(this);
        cityRepository = new CityRepository(this);

        bindStaticContent();
        bindUserContent();
        bindActions();
    }

    private void bindStaticContent() {
        rootView = findViewById(R.id.settings_root);
        TextView versionView = findViewById(R.id.text_version);
        versionView.setText(getString(R.string.settings_version_label) + " / " + getAppVersionName());

        themeModeLayout = findViewById(R.id.layout_theme_mode);
        themeModeInput = findViewById(R.id.input_theme_mode);
        MaterialSwitch themeSwitch = findViewById(R.id.switch_theme_follow_system);
        debugSampleSwitch = findViewById(R.id.switch_debug_sample_cities);
        boolean followSystem = SPUtils.isThemeFollowSystem(this);
        int savedThemeMode = SPUtils.getThemeMode(this);
        boolean debugSampleEnabled = SPUtils.isDebugSampleCitiesEnabled(this);

        String[] themeOptions = {
                getString(R.string.settings_theme_light),
                getString(R.string.settings_theme_dark)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_theme_mode_option,
                themeOptions
        );
        adapter.setDropDownViewResource(R.layout.item_theme_mode_option);
        themeModeInput.setAdapter(adapter);
        themeModeInput.setSelection(clampThemeMode(savedThemeMode), false);
        themeModeLayout.setVisibility(followSystem ? View.GONE : View.VISIBLE);
        themeModeInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedMode = position == THEME_MODE_DARK ? THEME_MODE_DARK : THEME_MODE_LIGHT;
                if (SPUtils.getThemeMode(SettingsActivity.this) != selectedMode) {
                    SPUtils.setThemeMode(SettingsActivity.this, selectedMode);
                    applyThemeMode(false, selectedMode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                themeModeInput.setSelection(clampThemeMode(SPUtils.getThemeMode(SettingsActivity.this)));
            }
        });

        themeSwitch.setChecked(followSystem);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SPUtils.setThemeFollowSystem(this, isChecked);
            themeModeLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            int selectedThemeMode = clampThemeMode(SPUtils.getThemeMode(this));
            themeModeInput.setSelection(selectedThemeMode, false);
            applyThemeMode(isChecked, selectedThemeMode);
        });

        debugSampleSwitch.setChecked(debugSampleEnabled);
        debugSampleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SPUtils.setDebugSampleCitiesEnabled(this, isChecked);
            long userId = userRepository.getLoginUserId();
            if (userId > 0) {
                cityRepository.syncDebugSampleCities(userId, isChecked);
                bindUserContent();
            }
        });
    }

    private void bindUserContent() {
        User user = userRepository.getLoginUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        City currentCity = cityRepository.getCurrentCity(user.getId());
        String cityName = currentCity != null
                ? currentCity.getCityName()
                : getString(R.string.main_default_city);

        TextView usernameView = findViewById(R.id.text_settings_username);
        TextView cityView = findViewById(R.id.text_settings_current_city);
        TextView phoneView = findViewById(R.id.text_settings_phone);
        TextView emailView = findViewById(R.id.text_settings_email);

        usernameView.setText(user.getUsername());
        cityView.setText(getString(R.string.settings_current_city_label) + ": " + cityName);
        phoneView.setText(getString(R.string.settings_phone_label) + ": " + valueOrFallback(user.getPhone()));
        emailView.setText(getString(R.string.settings_email_label) + ": " + valueOrFallback(user.getEmail()));
    }

    private void bindActions() {
        ImageButton backButton = findViewById(R.id.button_back);
        MaterialButton logoutButton = findViewById(R.id.button_logout);
        MaterialButton editProfileButton = findViewById(R.id.button_edit_profile);
        MaterialButton changePasswordButton = findViewById(R.id.button_change_password);

        backButton.setOnClickListener(v -> finish());
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_logout_confirm_title)
                .setMessage(R.string.settings_logout_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm_logout, (dialog, which) -> {
                    userRepository.logout();
                    navigateToLogin();
                })
                .show());
    }

    private void showEditProfileDialog() {
        User user = userRepository.getLoginUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null, false);
        TextInputLayout usernameLayout = dialogView.findViewById(R.id.layout_edit_username);
        TextInputLayout phoneLayout = dialogView.findViewById(R.id.layout_edit_phone);
        TextInputLayout emailLayout = dialogView.findViewById(R.id.layout_edit_email);
        TextInputEditText usernameInput = dialogView.findViewById(R.id.input_edit_username);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.input_edit_phone);
        TextInputEditText emailInput = dialogView.findViewById(R.id.input_edit_email);

        usernameInput.setText(user.getUsername());
        phoneInput.setText(user.getPhone());
        emailInput.setText(user.getEmail());

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_edit_profile_title)
                .setMessage(R.string.settings_edit_profile_message)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    clearError(usernameLayout, phoneLayout, emailLayout);
                    String username = getText(usernameInput);
                    String phone = getText(phoneInput);
                    String email = getText(emailInput);

                    boolean hasError = false;
                    if (TextUtils.isEmpty(username)) {
                        usernameLayout.setError(getString(R.string.error_account_required));
                        hasError = true;
                    } else if (username.length() < 3) {
                        usernameLayout.setError(getString(R.string.error_account_too_short));
                        hasError = true;
                    }
                    if (TextUtils.isEmpty(phone)) {
                        phoneLayout.setError(getString(R.string.error_phone_required));
                        hasError = true;
                    } else if (!Patterns.PHONE.matcher(phone).matches()) {
                        phoneLayout.setError(getString(R.string.error_invalid_phone));
                        hasError = true;
                    }
                    if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailLayout.setError(getString(R.string.error_invalid_email));
                        hasError = true;
                    }
                    if (hasError) {
                        return;
                    }

                    UserRepository.UpdateProfileResult result =
                            userRepository.updateProfile(user.getId(), username, email, phone);
                    if (result == UserRepository.UpdateProfileResult.USERNAME_EXISTS) {
                        usernameLayout.setError(getString(R.string.error_account_exists));
                        return;
                    }
                    if (result != UserRepository.UpdateProfileResult.SUCCESS) {
                        showMessage(getString(R.string.error_unknown));
                        return;
                    }

                    bindUserContent();
                    showMessage(getString(R.string.message_profile_updated));
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showChangePasswordDialog() {
        User user = userRepository.getLoginUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false);
        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.layout_current_password);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.layout_new_password);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.layout_confirm_new_password);
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.input_current_password);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.input_new_password);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.input_confirm_new_password);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_change_password_title)
                .setMessage(R.string.settings_change_password_message)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_change_password, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    clearError(currentPasswordLayout, newPasswordLayout, confirmPasswordLayout);
                    String currentPassword = getText(currentPasswordInput);
                    String newPassword = getText(newPasswordInput);
                    String confirmPassword = getText(confirmPasswordInput);

                    boolean hasError = false;
                    if (TextUtils.isEmpty(currentPassword)) {
                        currentPasswordLayout.setError(getString(R.string.error_current_password_required));
                        hasError = true;
                    }
                    if (TextUtils.isEmpty(newPassword)) {
                        newPasswordLayout.setError(getString(R.string.error_password_required));
                        hasError = true;
                    } else if (newPassword.length() < 6) {
                        newPasswordLayout.setError(getString(R.string.error_password_too_short));
                        hasError = true;
                    }
                    if (!TextUtils.equals(newPassword, confirmPassword)) {
                        confirmPasswordLayout.setError(getString(R.string.error_password_not_match));
                        hasError = true;
                    }
                    if (hasError) {
                        return;
                    }

                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.settings_change_password_confirm_title)
                            .setMessage(R.string.settings_change_password_confirm_message)
                            .setNegativeButton(R.string.action_cancel, null)
                            .setPositiveButton(R.string.action_confirm, (confirmDialog, which) -> {
                                UserRepository.ChangePasswordResult result =
                                        userRepository.changePassword(user.getId(), currentPassword, newPassword);
                                if (result == UserRepository.ChangePasswordResult.WRONG_PASSWORD) {
                                    currentPasswordLayout.setError(getString(R.string.error_password_incorrect));
                                    return;
                                }
                                if (result != UserRepository.ChangePasswordResult.SUCCESS) {
                                    showMessage(getString(R.string.error_unknown));
                                    return;
                                }
                                showMessage(getString(R.string.message_password_changed));
                                dialog.dismiss();
                            })
                            .show();
                }));
        dialog.show();
    }

    private void clearError(TextInputLayout... layouts) {
        if (layouts == null) {
            return;
        }
        for (TextInputLayout layout : layouts) {
            if (layout != null) {
                layout.setError(null);
            }
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showMessage(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    private String valueOrFallback(String value) {
        return value == null || value.trim().isEmpty()
                ? getString(R.string.settings_not_set)
                : value.trim();
    }

    private String getAppVersionName() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (Exception exception) {
            return "1.0";
        }
    }

    private void applyThemeMode(boolean followSystem, int themeMode) {
        if (followSystem) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            return;
        }
        AppCompatDelegate.setDefaultNightMode(
                clampThemeMode(themeMode) == THEME_MODE_DARK
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private int clampThemeMode(int themeMode) {
        return themeMode == THEME_MODE_DARK ? THEME_MODE_DARK : THEME_MODE_LIGHT;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
