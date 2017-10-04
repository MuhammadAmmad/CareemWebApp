package com.webapplibrary;

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

/**
 * Created by talseren on 02/07/2017.
 */

public class WebViewActivity extends AppCompatActivity {

    private String OLD_BASE_URL = "http://rider-webapp-demo.ridewithvia.com/admin/demo/external_riderapp/?" ; //origin_lat=40.780422&origin_lng=-73.946846&dest_lat=40.774275&dest_lng=-73.951260&auth=e";
    private String BASE_URL = "http://rider-webapp-demo.ridewithvia.com/admin/demo/rider/app/?";
    private static String MACHINE_URL = "http://10.0.4.124:8720/admin/demo/rider/app/?" ; //origin_lat=40.780422&origin_lng=-73.946846&dest_lat=40.774275&dest_lng=-73.951260&auth=e";

    public static final String EXTRA_PICKUP_LAT = "extraPickupLat";
    public static final String EXTRA_PICKUP_LNG = "extraPickupLng";
    public static final String EXTRA_DROPOFF_LAT = "extraDropoffLat";
    public static final String EXTRA_DROPOFF_LNG = "extraDropoffLng";
    public static final String EXTRA_RIDER_ID= "extrariderId";
    public static final String EXTRA_FIRST_NAME = "extraFirstName";
    public static final String EXTRA_LAST_NAME = "extraLastName";
    public static final String EXTRA_PASSENGER_COUNT = "extraPassengerCount";

    public static final String EXTRA_PICKUP_ADDRESS = "extraPickupAddress";
    public static final String EXTRA_DROPOFF_ADDRESS = "extraDropoffAddress";

    private final String SP_AUTH_KEY = "auth_key";
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView)findViewById(R.id.webView);

        SerializableAddress pickupAddress;
        SerializableAddress dropoffAddress;

        Bundle extras = getIntent().getExtras();
        String url = getURL(extras, BASE_URL);

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

    private String getURL(Bundle extras, String baseUrl) {

        String url = baseUrl;

        if (extras != null) {

            if (extras.containsKey(EXTRA_PICKUP_LAT) && extras.containsKey(EXTRA_PICKUP_LNG)) {
                //pickupAddress = (SerializableAddress) extras.getSerializable(EXTRA_PICKUP_ADDRESS);
                double pickupLat = extras.getDouble(EXTRA_PICKUP_LAT);
                double pickupLng = extras.getDouble(EXTRA_PICKUP_LNG);
                url += "origin_lat=" + pickupLat + "&origin_lng=" + pickupLng;
            }

            if (extras.containsKey(EXTRA_DROPOFF_LAT) && extras.containsKey(EXTRA_DROPOFF_LNG)) {
                //pickupAddress = (SerializableAddress) extras.getSerializable(EXTRA_PICKUP_ADDRESS);
                double dropoffLat = extras.getDouble(EXTRA_DROPOFF_LAT);
                double dropoffLng = extras.getDouble(EXTRA_DROPOFF_LNG);
                url += "&dest_lat=" + dropoffLat + "&dest_lng=" + dropoffLng;
            }

            if (extras.containsKey(EXTRA_RIDER_ID)) {

                double riderId = extras.getDouble(EXTRA_RIDER_ID);
                url += "&rider_id=" + riderId;
            }

            if (extras.containsKey(EXTRA_FIRST_NAME)) {

                String firstName = extras.getString(EXTRA_FIRST_NAME);
                url += "&first_name=" + firstName;
            }

            if (extras.containsKey(EXTRA_LAST_NAME)) {

                String lastName = extras.getString(EXTRA_LAST_NAME);
                url += "&last_name=" + lastName;
            }

            if (extras.containsKey(EXTRA_PASSENGER_COUNT)) {

                int passengerCount = extras.getInt(EXTRA_PASSENGER_COUNT);
                url += "&passenger_count=" + passengerCount;
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String authString = sp.getString(SP_AUTH_KEY, "");
            if (TextUtils.isEmpty(authString)) {
                authString = RandomString.generateString(32);
                sp.edit().putString(SP_AUTH_KEY, authString).commit();
            }

            url += "&auth=" + authString;

            /*
            if (extras.containsKey(EXTRA_PICKUP_ADDRESS)) {
                pickupAddress = (SerializableAddress) extras.getSerializable(EXTRA_PICKUP_ADDRESS);
                url += "origin_lat=" + pickupAddress.getLatitude() + "&origin_lng=" + pickupAddress.getLongitude();
            }

            if (extras.containsKey(EXTRA_DROPOFF_ADDRESS)) {
                dropoffAddress = (SerializableAddress) extras.getSerializable(EXTRA_DROPOFF_ADDRESS);
                url += "&dest_lat=" + dropoffAddress.getLatitude() + "&dest_lng=" + dropoffAddress.getLongitude();
            }
            */
        }

        return url;
    }

    class JavaScriptInterface {
        @JavascriptInterface
        public void onRideFinished() {
            Log.d("JavaScriptInterface", "Ride finished");
            finish();
        }
    }

}