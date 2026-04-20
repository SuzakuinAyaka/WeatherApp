package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.UserDao;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.utils.MD5Utils;
import com.dengyy.weatherapp.utils.SPUtils;

public class UserRepository {

    private final UserDao userDao;
    private final Context appContext;

    public UserRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.userDao = new UserDao(appContext);
    }

    public long register(String username, String password, String email, String phone) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || userDao.existsByUsername(username)) {
            return -1;
        }
        long now = System.currentTimeMillis();
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(MD5Utils.hash(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userDao.insert(user);
    }

    public boolean login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return false;
        }
        String passwordHash = MD5Utils.hash(password);
        if (!passwordHash.equals(user.getPassword())) {
            return false;
        }
        SPUtils.saveLoginUser(appContext, user.getId(), user.getUsername());
        return true;
    }

    public boolean resetPassword(String username, String identity, String newPassword) {
        User user = userDao.findByIdentity(username, identity);
        if (user == null) {
            return false;
        }
        return userDao.updatePassword(user.getId(), MD5Utils.hash(newPassword));
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
