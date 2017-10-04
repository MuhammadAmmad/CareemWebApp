package com.careemwebapp.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.careemwebapp.R;

/**
 * Created by yuliya on 9/22/15.
 * Base activity for activities with toolbar where HomeAsUpIndicator is "x" - R.drawable.ic_close_white_24dp
 */
public abstract class BaseToolbarActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());

            if (getToolbarId() == 0) {
                return;
            }
            mToolbar = (Toolbar) findViewById(getToolbarId());

            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
                this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                setHomeAsUpIndicator(getHomeIconResourceId());
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                if (Build.VERSION.SDK_INT
                        <= Build.VERSION_CODES.JELLY_BEAN_MR2) { //http://stackoverflow.com/questions/26859841/homeasup-button-has-no-effect-in-android-4-2-2-with-appcompat-21-0-0
                    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    protected void setHomeAsUpIndicator(int resourceId) {
        this.getSupportActionBar().setHomeAsUpIndicator(resourceId);
    }

    protected int getHomeIconResourceId() {
        return R.mipmap.ic_close_white_24dp;
    }

    public abstract int getLayoutId();
    public abstract int getToolbarId();
}
