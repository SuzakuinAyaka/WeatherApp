package com.dengyy.weatherapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Utils {

    private MD5Utils() {
    }

    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
