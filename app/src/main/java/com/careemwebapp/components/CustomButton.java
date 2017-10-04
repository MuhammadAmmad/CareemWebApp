package com.careemwebapp.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.careemwebapp.FontsStorage;
import com.careemwebapp.R;

/**
 * Created by alex on 11.9.15.
 */
public class CustomButton extends AppCompatButton {

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        String font = null;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomFontView);
            font = (String) a.getText(R.styleable.CustomFontView_customFont);

            a.recycle();
        }
//        if (TextUtils.isEmpty(font)) {
//            font = FontsStorage.FONT_ACCORD_EXTRA_LIGHT;
//        }
//        applyFont(font);
    }

    public CustomButton(Context context) {
        this(context, null, android.R.attr.buttonStyle);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public void applyFont(String fontName) {
        if (fontName != null) {
            if (!isInEditMode()) {
                FontsStorage.applyFont(getContext(), fontName, this);
            }
        }
    }
}
