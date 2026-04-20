package com.dengyy.weatherapp.ui;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

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
                        if (isInputField(newFocus)) {
                            lastInputFocusChangedAt = System.currentTimeMillis();
                            cancelPendingClearFocus(rootView);
                            scrollToFocusedInput(rootView, newFocus);
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
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int bottomInset = imeVisible
                    ? Math.max(systemBars.bottom, imeInsets.bottom)
                    : systemBars.bottom;
            view.setPadding(
                    baseLeftPadding + systemBars.left,
                    baseTopPadding + systemBars.top,
                    baseRightPadding + systemBars.right,
                    baseBottomPadding + bottomInset
            );

            View currentFocus = getCurrentFocus();
            if (imeVisible) {
                cancelPendingClearFocus(view);
                scrollToFocusedInput(view, currentFocus);
            } else if (isInputField(currentFocus)) {
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
                    && isInputField(latestFocus)) {
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

    private boolean isInputField(View view) {
        return view instanceof EditText;
    }

    private void scrollToFocusedInput(View rootView, View focusedView) {
        if (!isInputField(focusedView)) {
            return;
        }
        ScrollView scrollView = findScrollView(rootView);
        if (scrollView == null) {
            return;
        }
        scrollView.post(() -> {
            Rect focusedRect = new Rect();
            focusedView.getDrawingRect(focusedRect);
            scrollView.offsetDescendantRectToMyCoords(focusedView, focusedRect);

            int bottomSafeInset = dpToPx(24);
            int visibleBottom = scrollView.getHeight() - bottomSafeInset;
            int targetScrollY = scrollView.getScrollY();
            if (focusedRect.bottom > visibleBottom) {
                targetScrollY += focusedRect.bottom - visibleBottom;
                scrollView.smoothScrollTo(0, Math.max(targetScrollY, 0));
            }
        });
    }

    private ScrollView findScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (!(view instanceof android.view.ViewGroup)) {
            return null;
        }
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            ScrollView scrollView = findScrollView(group.getChildAt(i));
            if (scrollView != null) {
                return scrollView;
            }
        }
        return null;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
