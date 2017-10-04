package com.careemwebapp.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.careemwebapp.R;
import com.careemwebapp.SerializableAddress;
import com.careemwebapp.utils.RandomString;

import java.util.logging.Logger;

import static com.careemwebapp.activities.MainActivity.EXTRA_DROPOFF_ADDRESS;
import static com.careemwebapp.activities.MainActivity.EXTRA_PICKUP_ADDRESS;

/**
 * Created by talseren on 02/07/2017.
 */

public class WebViewActivity extends AppCompatActivity {

    private String BASE_URL = "http://rider-webapp-demo.ridewithvia.com/admin/demo/external_riderapp/?" ; //origin_lat=40.780422&origin_lng=-73.946846&dest_lat=40.774275&dest_lng=-73.951260&auth=e";
    private static String MACHINE_URL = "http://10.0.4.206:8720/admin/demo/rider/app/?" ; //origin_lat=40.780422&origin_lng=-73.946846&dest_lat=40.774275&dest_lng=-73.951260&auth=e";

    private final String SP_AUTH_KEY = "auth_key";
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView)findViewById(R.id.webView);

        SerializableAddress pickupAddress;
        SerializableAddress dropoffAddress;

        String url = BASE_URL;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRA_PICKUP_ADDRESS)) {
                pickupAddress = (SerializableAddress) extras.getSerializable(EXTRA_PICKUP_ADDRESS);
                url += "origin_lat=" + pickupAddress.getLatitude() + "&origin_lng=" + pickupAddress.getLongitude();
            }

            if (extras.containsKey(EXTRA_DROPOFF_ADDRESS)) {
                dropoffAddress = (SerializableAddress) extras.getSerializable(EXTRA_DROPOFF_ADDRESS);
                url += "&dest_lat=" + dropoffAddress.getLatitude() + "&dest_lng=" + dropoffAddress.getLongitude();
            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String authString = sp.getString(SP_AUTH_KEY, "");
        if (TextUtils.isEmpty(authString)) {
            authString = RandomString.generateString(32);
            sp.edit().putString(SP_AUTH_KEY, authString).commit();
        }

        url += "&auth=" + authString;

        WebSettings mWebSettings = webView.getSettings();
        mWebSettings.setSupportZoom(false);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        //load the page with cache
        if (Build.VERSION.SDK_INT >= 19) {
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        WebViewClient mWebViewClient = new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("onReceivedError", "error received: " + error.toString());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.e("onReceivedHttpError", "error received: " + errorResponse.getStatusCode() + " - " + errorResponse.getReasonPhrase());
            }
        };
        webView.setWebViewClient(mWebViewClient);
        webView.addJavascriptInterface(new JavaScriptInterface(),"android");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.i("WebViewActivity", "onConsoleMessage: " + cm.message());
                return true;
            }


        });

        webView.loadUrl(url);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    class JavaScriptInterface {
        @JavascriptInterface
        public void onRideFinished() {
            Log.d("JavaScriptInterface", "Ride finished");
            finish();
        }
    }

}