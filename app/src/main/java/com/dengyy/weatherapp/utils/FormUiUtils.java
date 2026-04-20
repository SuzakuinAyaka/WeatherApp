package com.dengyy.weatherapp.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class FormUiUtils {

    private FormUiUtils() {
    }

    public static void bindFieldBehavior(
            TextInputLayout layout,
            TextInputEditText editText,
            String labelText,
            String hintText
    ) {
        layout.setHintEnabled(true);
        layout.setHint(labelText);
        editText.setHint(null);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void clearFocusWhenTapOutside(View rootView, View... focusableViews) {
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                for (View view : focusableViews) {
                    if (view != null && view.hasFocus()) {
                        view.clearFocus();
                    }
                }
            }
            return false;
        });
    }

    public static void moveFocusOnEditorAction(
            TextInputEditText currentEditText,
            TextInputEditText nextEditText
    ) {
        currentEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            boolean isEnterKey =
                    event != null
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN;
            boolean isImeAction =
                    actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE;
            if (!isEnterKey && !isImeAction) {
                return false;
            }
            nextEditText.requestFocus();
            nextEditText.post(() -> showKeyboard(nextEditText));
            return true;
        });
    }

    public static void submitOnEditorAction(TextInputEditText editText, Runnable action) {
        editText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            boolean isEnterKey =
                    event != null
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN;
            boolean isImeAction =
                    actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO;
            if (!isEnterKey && !isImeAction) {
                return false;
            }
            action.run();
            return true;
        });
    }

    public static void showKeyboard(TextInputEditText editText) {
        Context context = editText.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            editText.requestFocus();
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
