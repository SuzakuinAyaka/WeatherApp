package com.dengyy.weatherapp.ui;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public abstract class BaseActivity extends AppCompatActivity {

    private static final long INPUT_FOCUS_GRACE_PERIOD_MS = 250L;

    private Runnable pendingClearFocusRunnable;
    private long lastInputFocusChangedAt;

    protected void setupPageBehavior(int rootViewId) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View rootView = findViewById(rootViewId);
        if (rootView == null) {
            return;
        }

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(
                new ViewTreeObserver.OnGlobalFocusChangeListener() {
                    @Override
                    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                        if (newFocus instanceof TextInputEditText) {
                            lastInputFocusChangedAt = System.currentTimeMillis();
                            cancelPendingClearFocus(rootView);
                        }
                    }
                }
        );

        final int baseLeftPadding = rootView.getPaddingLeft();
        final int baseTopPadding = rootView.getPaddingTop();
        final int baseRightPadding = rootView.getPaddingRight();
        final int baseBottomPadding = rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    baseLeftPadding + systemBars.left,
                    baseTopPadding + systemBars.top,
                    baseRightPadding + systemBars.right,
                    baseBottomPadding + systemBars.bottom
            );

            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            View currentFocus = getCurrentFocus();
            if (imeVisible) {
                cancelPendingClearFocus(view);
            } else if (currentFocus instanceof TextInputEditText) {
                scheduleClearFocus(view, currentFocus);
            }
            return insets;
        });
    }

    private void scheduleClearFocus(View rootView, View focusedView) {
        cancelPendingClearFocus(rootView);
        pendingClearFocusRunnable = () -> {
            View latestFocus = getCurrentFocus();
            WindowInsetsCompat latestInsets = ViewCompat.getRootWindowInsets(rootView);
            boolean imeStillHidden = latestInsets == null || !latestInsets.isVisible(WindowInsetsCompat.Type.ime());
            boolean withinGracePeriod =
                    System.currentTimeMillis() - lastInputFocusChangedAt < INPUT_FOCUS_GRACE_PERIOD_MS;
            if (imeStillHidden
                    && !withinGracePeriod
                    && latestFocus == focusedView
                    && latestFocus instanceof TextInputEditText) {
                latestFocus.clearFocus();
            }
        };
        rootView.postDelayed(pendingClearFocusRunnable, 120);
    }

    private void cancelPendingClearFocus(View rootView) {
        if (pendingClearFocusRunnable != null) {
            rootView.removeCallbacks(pendingClearFocusRunnable);
            pendingClearFocusRunnable = null;
        }
    }
}
