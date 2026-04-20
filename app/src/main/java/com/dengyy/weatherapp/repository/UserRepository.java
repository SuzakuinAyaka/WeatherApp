package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.UserDao;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.utils.MD5Utils;
import com.dengyy.weatherapp.utils.SPUtils;

public class UserRepository {

    public enum LoginResult {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD
    }

    public enum RegisterResult {
        SUCCESS,
        USERNAME_EXISTS,
        INVALID_INPUT
    }

    public enum ResetPasswordResult {
        SUCCESS,
        USER_NOT_FOUND,
        IDENTITY_MISMATCH
    }

    private final UserDao userDao;
    private final Context appContext;

    public UserRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.userDao = new UserDao(appContext);
    }

    public RegisterResult register(String username, String password, String email, String phone) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return RegisterResult.INVALID_INPUT;
        }
        if (userDao.existsByUsername(username)) {
            return RegisterResult.USERNAME_EXISTS;
        }

        long now = System.currentTimeMillis();
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(MD5Utils.hash(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        long rowId = userDao.insert(user);
        return rowId > 0 ? RegisterResult.SUCCESS : RegisterResult.INVALID_INPUT;
    }

    public LoginResult login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return LoginResult.USER_NOT_FOUND;
        }

        String passwordHash = MD5Utils.hash(password);
        if (!passwordHash.equals(user.getPassword())) {
            return LoginResult.WRONG_PASSWORD;
        }

        SPUtils.saveLoginUser(appContext, user.getId(), user.getUsername());
        return LoginResult.SUCCESS;
    }

    public ResetPasswordResult resetPassword(String username, String identity, String newPassword) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return ResetPasswordResult.USER_NOT_FOUND;
        }

        boolean matched = TextUtils.equals(identity, user.getEmail()) || TextUtils.equals(identity, user.getPhone());
        if (!matched) {
            return ResetPasswordResult.IDENTITY_MISMATCH;
        }

        boolean success = userDao.updatePassword(user.getId(), MD5Utils.hash(newPassword));
        return success ? ResetPasswordResult.SUCCESS : ResetPasswordResult.IDENTITY_MISMATCH;
    }

    public boolean isLoggedIn() {
        return SPUtils.getLoginUserId(appContext) > 0;
    }

    public void logout() {
        SPUtils.clearLoginUser(appContext);
    }

    public long getLoginUserId() {
        return SPUtils.getLoginUserId(appContext);
    }

    @Nullable
    public User getLoginUser() {
        long userId = getLoginUserId();
        if (userId <= 0) {
            return null;
        }
        return userDao.findById(userId);
    }
}
