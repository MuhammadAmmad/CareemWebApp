package com.careemwebapp.components;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.careemwebapp.R;

public class CustomDialog extends Dialog implements View.OnClickListener {


    private Activity mActivity;

    private OnClickListener mPositiveClickListener = null;
    private OnClickListener mNegativeClickListener = null;

    private String mPositiveBtnText = null;
    private String mNegativeBtnText = null;

    private String mTitle = null;
    private String mMessage = null;

    private boolean mCancelable = false;

    private CustomTextView mTitleTv;
    private CustomTextView mMessageTv;
    private CustomButton mPositiveBtn;
    private CustomButton mNegativeBtn;
    private View mButtonsDelimiter;

    public CustomDialog(@NonNull Activity activity) {
        super(activity, R.style.CustomDialogThemeFloating);
        mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(false);
        setCancelable(mCancelable);

        setContentView(R.layout.custom_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.colorPrimary));
        }

        mTitleTv = (CustomTextView) findViewById(R.id.tvDialogTitle);
        mMessageTv = (CustomTextView) findViewById(R.id.tvDialogMessage);
        mPositiveBtn = (CustomButton) findViewById(R.id.btnPositive);
        mNegativeBtn = (CustomButton) findViewById(R.id.btnNegative);
        mButtonsDelimiter = findViewById(R.id.dialogButtonsDelimiter);
        initViews();
    }

    private void initViews() {
        //init title
        if(TextUtils.isEmpty(mTitle)) {
            mTitleTv.setVisibility(View.GONE);;
        } else {
            mTitleTv.setText(mTitle);
            mTitleTv.setVisibility(View.VISIBLE);
        }

        //init message
        if(TextUtils.isEmpty(mMessage)) {
            mMessageTv.setVisibility(View.GONE);;
        } else {
            mMessageTv.setText(mMessage);
            mMessageTv.setVisibility(View.VISIBLE);
        }

        //init negative button
        mNegativeBtn.setOnClickListener(this);
        if(TextUtils.isEmpty(mNegativeBtnText)) {
            mNegativeBtn.setVisibility(View.GONE);
            mButtonsDelimiter.setVisibility(View.GONE);
        } else {
            mNegativeBtn.setText(mNegativeBtnText);
            mNegativeBtn.setVisibility(View.VISIBLE);
            mButtonsDelimiter.setVisibility(View.VISIBLE);
        }

        //init positive button
        mPositiveBtn.setOnClickListener(this);
        if(TextUtils.isEmpty(mPositiveBtnText)) {
            mPositiveBtn.setText(mPositiveBtnText);
        }

    }

    @NonNull
    public CustomDialog setIsCancelable(final boolean cancelable) {
        mCancelable = cancelable;
        return this;
    }

    @NonNull
    public CustomDialog setPositiveButton(@NonNull final String text, @Nullable final OnClickListener onClickListener) {
        mPositiveBtnText = text;
        mPositiveClickListener = onClickListener;
        return this;
    }

    @NonNull
    public CustomDialog setNegativeButton(@NonNull final String text, @Nullable final OnClickListener onClickListener) {
        mNegativeBtnText = text;
        mNegativeClickListener = onClickListener;
        return this;
    }

    @NonNull
    public CustomDialog setDialogTitle(@Nullable final String title) {
        mTitle = title;
        return this;
    }

    @NonNull
    public CustomDialog setDialogMessage(@NonNull final String message) {
        mMessage = message;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPositive:
                closeDialog();
                if(mPositiveClickListener != null) {
                    mPositiveClickListener.onClick(this, BUTTON_POSITIVE);
                }
                break;
            case R.id.btnNegative:
                closeDialog();
                if(mNegativeClickListener != null) {
                    mNegativeClickListener.onClick(this, BUTTON_NEGATIVE);
                }
                break;
        }
    }

    public void closeDialog() {
        if(mActivity != null && !mActivity.isFinishing() && CustomDialog.this.isShowing()) {
            dismiss();
        }
    }

    public CustomDialog showDialog() {
        if(!mActivity.isFinishing()) {
            show();
        }
        return this;
    }
}
