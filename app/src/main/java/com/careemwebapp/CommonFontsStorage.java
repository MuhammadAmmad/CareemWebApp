package com.careemwebapp;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 11.9.15.
 */
public class CommonFontsStorage {

    public static final String FONT_ROBO_LIGHT = "assets/Roboto-Light.ttf";
    public static final String FONT_ACCORD_BOLD = "AccordBold.otf";
    public static final String FONT_ACCORD_EXTRA_BOLD = "assets/AccordExtraBold.otf";
    public static final String FONT_ACCORD_EXTRA_LIGHT = "assets/AccordExtraLight.otf";
    public static final String FONT_ACCORD_LIGHT = "assets/AccordLight.otf";
    public static final String FONT_ACCORD_MEDIUM = "assets/AccordMedium.otf";
    public static final String FONT_ACCORD_REG = "assets/AccordReg.otf";
    public static final String FONT_ACCORD_THIN = "assets/AccordThin.otf";

    private static Map<String, Typeface> storage = new HashMap<String, Typeface>();

    public static Typeface getFont(Context context, String fullName) {
        if (storage == null) {
            storage = new HashMap<String, Typeface>();
        }
        synchronized (storage) {
            Typeface font = storage.get(fullName);
            if (font == null) {
                font = Typeface.createFromAsset(context.getAssets(), fullName);
                if (font != null) {
                    storage.put(fullName, font);
                    return font;
                }
            } else {
                return font;
            }
        }
        return Typeface.DEFAULT;
    }

    public static void applyFont(Context context, String fullName, TextView text) {
        Typeface tf = getFont(context, fullName);
        if (tf != null && text != null) {
            text.setTypeface(tf);
        }
    }

    public static void clear() {
        if (storage != null) {
            storage.clear();
        }
    }
}
