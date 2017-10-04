package com.careemwebapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.careemwebapp.AddressSuggestionType;
import com.careemwebapp.R;
import com.careemwebapp.SerializableAddress;
import com.careemwebapp.SerializableFavorite;
import com.careemwebapp.activities.AddressSuggestActivity;

import java.math.BigInteger;
import java.security.SecureRandom;

import static com.careemwebapp.activities.CommonAddressSuggestActivity.ADDRESS_TYPE_EXTRA;

public class MainActivity extends AppCompatActivity {

    private final int ADDRESS_SUGGEST_ACTIVITY_PICKUP_CODE = 1;
    private final int ADDRESS_SUGGEST_ACTIVITY_DROPOFF_CODE = 2;

    public static final String EXTRA_PICKUP_ADDRESS = "extraPickupAddress";
    public static final String EXTRA_DROPOFF_ADDRESS = "extraDropoffAddress";

    private TextView pickupAddressTv;
    private TextView dropoffAddressTv;

    private SerializableAddress pickupAddress;
    private SerializableAddress dropoffAddress;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickupAddressTv = (TextView)findViewById(R.id.pickupAddressTv);
        dropoffAddressTv = (TextView)findViewById(R.id.dropoffAddressTv);
        webView = (WebView)findViewById(R.id.webView);
    }

    public void onPickupAddressClick(View view) {

        Intent intent = new Intent(this, AddressSuggestActivity.class);
        intent.putExtra(ADDRESS_TYPE_EXTRA, AddressSuggestionType.PICKUP.toString());
        startActivityForResult(intent, ADDRESS_SUGGEST_ACTIVITY_PICKUP_CODE);
    }

    public void onDropoffAddressClick(View view) {

        Intent intent = new Intent(this, AddressSuggestActivity.class);
        intent.putExtra(ADDRESS_TYPE_EXTRA, AddressSuggestionType.DROPOFF.toString());
        startActivityForResult(intent, ADDRESS_SUGGEST_ACTIVITY_DROPOFF_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADDRESS_SUGGEST_ACTIVITY_PICKUP_CODE) {
            if (data != null) {
                Bundle extras = data.getExtras();
                pickupAddress = (SerializableFavorite) extras.getSerializable(AddressSuggestActivity.RESULT_ADDRESS_EXTRA);

                pickupAddressTv.setText(pickupAddress.getName());
            }
        } else if (requestCode == ADDRESS_SUGGEST_ACTIVITY_DROPOFF_CODE) {
            if (data != null) {
                Bundle extras = data.getExtras();
                dropoffAddress = (SerializableFavorite) extras.getSerializable(AddressSuggestActivity.RESULT_ADDRESS_EXTRA);

                dropoffAddressTv.setText(dropoffAddress.getName());
            }
        }
    }

    public void onShowWebAppClick(View view) {

        if (pickupAddress == null || dropoffAddress == null) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);

            builder.setMessage("Please choose pickup and dropoff addresses")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {

            Intent intent = new Intent(this, WebViewActivity.class);
            if (pickupAddress != null) {
                intent.putExtra(EXTRA_PICKUP_ADDRESS, pickupAddress);
            }

            if (dropoffAddress != null) {
                intent.putExtra(EXTRA_DROPOFF_ADDRESS, dropoffAddress);
            }

            startActivity(intent);

        //webView.loadUrl("http://www.google.com");
        }
    }
}
