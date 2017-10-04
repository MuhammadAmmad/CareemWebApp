package com.careemwebapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.careemwebapp.AddressFinder;
import com.careemwebapp.AddressSuggestionType;
import com.careemwebapp.IAddressFinderCallback;
import com.careemwebapp.R;
import com.careemwebapp.SerializableAddress;
import com.careemwebapp.SerializableFavorite;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.webapplibrary.ViaWebApp;

import static com.careemwebapp.activities.CommonAddressSuggestActivity.ADDRESS_TYPE_EXTRA;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private enum AddressType {
        PICKUP,
        DROPOFF
    }

    public static final int DEFAULT_ZOOM = 17;

    AddressType mAddressType = AddressType.PICKUP;

    private final int ADDRESS_SUGGEST_ACTIVITY_PICKUP_CODE = 1;
    private final int ADDRESS_SUGGEST_ACTIVITY_DROPOFF_CODE = 2;
    private final int WEBVIEW_ACTIVITY_CODE = 3;

    public static final String EXTRA_PICKUP_ADDRESS = "extraPickupAddress";
    public static final String EXTRA_DROPOFF_ADDRESS = "extraDropoffAddress";

    TextView pickupAddressTv;
    TextView dropoffAddressTv;

    Button careemButton;
    Button careemBusButton;
    Button rideNowButton;

    ImageView pickupMarkerIv;
    ImageView dropoffMarkerIv;

    LinearLayout buttonsLayout;

    private SerializableAddress pickupAddress;
    private SerializableAddress dropoffAddress;
    private LatLng mPickupLatLng;
    private LatLng mDropoffLatLng;

    private GoogleMap mMap;
    protected Marker mPickupMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.top_bar);

        careemButton = (Button)findViewById(R.id.careemButton);

        careemBusButton = (Button)findViewById(R.id.careemBusButton);

        rideNowButton = (Button)findViewById(R.id.rideBowButton);

        pickupAddressTv = (TextView)findViewById(R.id.pickupAddress);
        dropoffAddressTv = (TextView)findViewById(R.id.dropoffAddress);

        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);

        pickupMarkerIv = (ImageView) findViewById(R.id.ivPickUpMarker);
        dropoffMarkerIv = (ImageView) findViewById(R.id.ivDropOffMarker);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng nyc = new LatLng(40.777795, -73.946552);
        LatLng ryadh = new LatLng(24.722906, 46.686543);

        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ryadh));
        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.zoom(DEFAULT_ZOOM);
        builder.target(ryadh);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                lookupAddress(cameraPosition.target.latitude, cameraPosition.target.longitude);

                if (mAddressType.equals(AddressType.PICKUP)) {
                    mPickupLatLng = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                } else {
                    mDropoffLatLng = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                }
            }
        });
    }

    protected void lookupAddress(final double latitude, final double longitude) {

        AddressFinder addressFinder = new AddressFinder(this);
        addressFinder.lookupAsync(latitude, longitude, new IAddressFinderCallback() {
            @Override
            public void onAddress(String address, String addressShort) {

                if (mAddressType == AddressType.PICKUP) {
                    pickupAddressTv.setText(address);
                    pickupAddress = new SerializableAddress(latitude, longitude,address);
                } else {
                    dropoffAddressTv.setText(address);
                    dropoffAddress = new SerializableAddress(latitude, longitude,address);
                }
            }
        });
    }

    public void onSetPickupClick(View view) {

        setPickupMarker(mMap.getCameraPosition().target);
        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.zoom(DEFAULT_ZOOM);
        builder.target(mMap.getCameraPosition().target);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

        mAddressType = AddressType.DROPOFF;
        rideNowButton.setVisibility(View.GONE);
        careemButton.setVisibility(View.VISIBLE);
        careemBusButton.setVisibility(View.VISIBLE);
        pickupMarkerIv.setVisibility(View.GONE);
        dropoffMarkerIv.setVisibility(View.VISIBLE);

        CameraPosition cameraPosition = mMap.getCameraPosition();
        lookupAddress(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    public void resetPickup(View view) {

        mAddressType = AddressType.PICKUP;
        rideNowButton.setVisibility(View.VISIBLE);
        careemButton.setVisibility(View.GONE);
        careemBusButton.setVisibility(View.GONE);
        pickupMarkerIv.setVisibility(View.VISIBLE);
        dropoffMarkerIv.setVisibility(View.GONE);

        if (mPickupLatLng != null) {
            centerOnLocation(mPickupLatLng, true, DEFAULT_ZOOM);
        }

        removePickupMarker();
    }

    public void setPickupMarker(LatLng position) {
        if(mMap != null) {
            if (mPickupMarker == null) {
                mPickupMarker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pu_map_pin)));
            } else {
                mPickupMarker.setPosition(position);
            }
        }
        mPickupMarker.setVisible(true);
    }

    public void removePickupMarker() {
        /*if (mPickupMarker!= null) {
            mPickupMarker.remove();
        }*/
        if (mPickupMarker!= null) {
            mPickupMarker.setVisible(false);
        }
    }

    public void onPickupAddressSearchClick(View view) {

        Intent intent = new Intent(this, AddressSuggestActivity.class);
        intent.putExtra(ADDRESS_TYPE_EXTRA, AddressSuggestionType.PICKUP.toString());
        startActivityForResult(intent, ADDRESS_SUGGEST_ACTIVITY_PICKUP_CODE);
    }

    public void onDropoffAddressSearchClick(View view) {

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
                mPickupLatLng = new LatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude());

                if (mAddressType.equals(AddressType.DROPOFF)) {
                    resetPickup(null);
                }

                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mPickupLatLng));
                centerOnLocation(mPickupLatLng, true, DEFAULT_ZOOM);
            }
        } else if (requestCode == ADDRESS_SUGGEST_ACTIVITY_DROPOFF_CODE) {
            if (data != null) {
                Bundle extras = data.getExtras();
                dropoffAddress = (SerializableFavorite) extras.getSerializable(AddressSuggestActivity.RESULT_ADDRESS_EXTRA);

                dropoffAddressTv.setText(dropoffAddress.getName());

                mDropoffLatLng = new LatLng(dropoffAddress.getLatitude(), dropoffAddress.getLongitude());

                if (mAddressType.equals(AddressType.PICKUP)) {
                    onSetPickupClick(null);
                }

                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mDropoffLatLng));
                centerOnLocation(mDropoffLatLng, true, DEFAULT_ZOOM);
            }
        } else if (requestCode == WEBVIEW_ACTIVITY_CODE) {
            resetPickup(null);
        }
    }

    public void centerOnLocation(LatLng location, boolean withZoom, int defaultZoom/*, final IMapAnimationListener mapAnimationListener*/) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder().target(location).zoom(
                            withZoom ? defaultZoom : mMap.getCameraPosition().zoom).build()), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    public void onShowWebAppClick(View view) {

        Intent intent = new Intent(this, WebViewActivity.class);
        if (pickupAddress != null) {
            intent.putExtra(EXTRA_PICKUP_ADDRESS, pickupAddress);
        }

        if (dropoffAddress != null) {
            intent.putExtra(EXTRA_DROPOFF_ADDRESS, dropoffAddress);
        }

        ViaWebApp.openViaWebApp(this, pickupAddress.getLatitude(), pickupAddress.getLongitude(), dropoffAddress.getLatitude(), dropoffAddress.getLongitude(), 123, "Joe", "Rogan", 1);
        //startActivityForResult(intent, WEBVIEW_ACTIVITY_CODE);
    }
}