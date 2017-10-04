package com.careemwebapp.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by talseren on 03/07/2017.
 */

public class RandomString {

    private static final char[] symbols;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ch++) {
            tmp.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ch++) {
            tmp.append(ch);
        }
        symbols = tmp.toString().toCharArray();
    }

    private final Random random = new Random();

    private final char[] buf;

    public RandomString(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        buf = new char[length];
    }

    public String nextString() {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

    public static String generateString(int length)
    {
        SecureRandom random = new SecureRandom();
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(text);
    }
}