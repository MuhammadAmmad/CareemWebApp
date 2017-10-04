package com.webapplibrary;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by talseren on 03/10/2017.
 */

public class ViaWebApp {

    public static void openViaWebApp(Activity activity, Double pickupLat, Double pickupLng,
                                     Double dropoffLat, Double dropoffLng, long riderId,
                                     String firstName, String lastName, int passengetCount) {

        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_PICKUP_LAT, pickupLat);
        intent.putExtra(WebViewActivity.EXTRA_PICKUP_LNG, pickupLng);
        intent.putExtra(WebViewActivity.EXTRA_DROPOFF_LAT, dropoffLat);
        intent.putExtra(WebViewActivity.EXTRA_DROPOFF_LNG, dropoffLng);
        intent.putExtra(WebViewActivity.EXTRA_RIDER_ID, riderId);
        intent.putExtra(WebViewActivity.EXTRA_FIRST_NAME, firstName);
        intent.putExtra(WebViewActivity.EXTRA_LAST_NAME, lastName);
        intent.putExtra(WebViewActivity.EXTRA_PASSENGER_COUNT, passengetCount);

        activity.startActivity(intent);

    }
}
