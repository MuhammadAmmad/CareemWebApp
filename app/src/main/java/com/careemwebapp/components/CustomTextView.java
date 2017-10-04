package com.careemwebapp.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.careemwebapp.FontsStorage;
import com.careemwebapp.R;

public class CustomTextView extends AppCompatTextView {

    private int mDrawableWidth;
    private int mDrawableHeight;

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle, 0);
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

    public CustomTextView(Context context) {
        this(context, null, 0);
        init(context, null, 0, 0);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs, 0, 0);
    }
/*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }*/

    public void applyFont(String fontName) {
        if (fontName != null) {
            if (!isInEditMode()) {
                FontsStorage.applyFont(getContext(), fontName, this);
            }
        }
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TextViewDrawableSize, defStyleAttr, defStyleRes);
        try {
            mDrawableWidth = array.getDimensionPixelSize(R.styleable.TextViewDrawableSize_compoundDrawableWidth, -1);
            mDrawableHeight = array.getDimensionPixelSize(R.styleable.TextViewDrawableSize_compoundDrawableHeight, -1);
        } finally {
            array.recycle();
        }
        if (mDrawableWidth > 0 || mDrawableHeight > 0) {
            initCompoundDrawableSize();
        }
    }

    private void initCompoundDrawableSize() {
        Drawable[] drawables = getCompoundDrawables();
        for (Drawable drawable : drawables) {
            if (drawable == null) {
                continue;
            }

            Rect realBounds = drawable.getBounds();
            float scaleFactor = realBounds.height() / (float) realBounds.width();

            float drawableWidth = realBounds.width();
            float drawableHeight = realBounds.height();

            if (mDrawableWidth > 0) {
                // save scale factor of image
                if (drawableWidth > mDrawableWidth) {
                    drawableWidth = mDrawableWidth;
                    drawableHeight = drawableWidth * scaleFactor;
                }
            }
            if (mDrawableHeight > 0) {
                // save scale factor of image

                if (drawableHeight > mDrawableHeight) {
                    drawableHeight = mDrawableHeight;
                    drawableWidth = drawableHeight / scaleFactor;
                }
            }

            realBounds.right = realBounds.left + Math.round(drawableWidth);
            realBounds.bottom = realBounds.top + Math.round(drawableHeight);

            drawable.setBounds(realBounds);
        }
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right,
            Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        initCompoundDrawableSize();
    }
}
