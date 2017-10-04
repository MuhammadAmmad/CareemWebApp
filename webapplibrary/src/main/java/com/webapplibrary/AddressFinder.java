package com.webapplibrary;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressFinder {

    public static final String KEY_ADDRESS = "address";
    public static final String KEY_ADDRESS_SHORT = "address_short";

    private final Context mContext;

    //this flag need to be 'volatile' because we're accessing it from different threads
    private volatile boolean mGeocoderBusy = false;
    private volatile boolean mSkipAddressSearch = false;

    public AddressFinder(Context context) {
        mContext = context;
    }

    public static String formatFullAddressString(Address address) {
        String addressString = address.getAddressLine(0);
        for (int i=1 ; i<address.getMaxAddressLineIndex() ; i++) {
            addressString += ", " + address.getAddressLine(i);
        }
        if (address.getCountryName() != null && ! address.getCountryName().isEmpty()) {
            addressString += ", " + address.getCountryName();
        }
        return addressString;
    }

    public static String formatAddressString(Address address) {
        StringBuilder sb = new StringBuilder();
        if(!TextUtils.isEmpty(address.getSubThoroughfare())) {
            sb.append(address.getSubThoroughfare());
            sb.append(" ");
        }
        if(!TextUtils.isEmpty(address.getThoroughfare())) {
            sb.append(address.getThoroughfare());
        } else if(sb.length() == 0 && !TextUtils.isEmpty(address.getFeatureName())) {
            sb.append(address.getFeatureName());
        }
        if(!TextUtils.isEmpty(address.getLocality())) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getLocality());
        } else if (!TextUtils.isEmpty(address.getAdminArea())) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getAdminArea());
        }

        return sb.length() == 0 ? formatFullAddressString(address) : sb.toString();
    }

    public static String formatShortAddressString(Address address) {
       return address.getThoroughfare();
    }

    public void setSkipAddressSearch(final boolean skipAddressSearch) {
        mSkipAddressSearch = skipAddressSearch;
    }

    public void lookupAsync(final Double latitude, final Double longitude, final IAddressFinderCallback addressFinderCallback) {
        lookupAsync(latitude, longitude, addressFinderCallback, true);
    }

    public void lookupAsync(final Double latitude, final Double longitude, final IAddressFinderCallback addressFinderCallback, final boolean returnMainThread) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                //perform the code below only if geocoder is not busy.
                //otherwise we can catch a really huge amount of IOExceptions that will make app to stuck.
                if (!mGeocoderBusy) {
                    mGeocoderBusy = true;
                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    String result = null;
                    String resultShort = null;
                    try {
                        if (Geocoder.isPresent() && isNetworkAvailable()) {
                            List<Address> addresses = geocoder
                                    .getFromLocation(latitude, longitude, 1);
                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                result = formatAddressString(address);
                                resultShort = formatShortAddressString(address);
                            } else {
                            }
                        }

                    } catch (IOException e) {
                        if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                        } else {
                        }
                    } catch (IllegalArgumentException e) {
                        if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                        } else {
                        }
                    } finally {
                        if (addressFinderCallback != null) {
                            final String tempResult = result;
                            final String tempShortResult = resultShort;
                            if(returnMainThread) {
                                ActivityUtil.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addressFinderCallback.onAddress(tempResult, tempShortResult);
                                    }
                                });
                            } else {
                                addressFinderCallback.onAddress(tempResult, tempShortResult);
                            }
                        }
                        //release 'busy' flag
                        mGeocoderBusy = false;
                    }
                }
            }
        };
        thread.start();
    }

    public void lookup(final Double latitude, final Double longitude, final Handler handler) {
        if(mSkipAddressSearch) {
            return;
        }
        lookupAsync(latitude, longitude, new IAddressFinderCallback() {
            @Override
            public void onAddress(String address, String addressShort) {
                Message msg = Message.obtain();
                msg.setTarget(handler);
                if (address != null) {
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_ADDRESS, address);
                    bundle.putString(KEY_ADDRESS_SHORT, addressShort);
                    msg.setData(bundle);
                } else {
                    msg.what = 0;
                }
                msg.sendToTarget();
            }
        }, false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
