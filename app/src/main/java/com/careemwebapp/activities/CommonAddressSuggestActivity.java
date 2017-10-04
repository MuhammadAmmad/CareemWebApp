package com.careemwebapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.careemwebapp.AddressSuggestionAdapter;
import com.careemwebapp.AddressSuggestionType;
import com.careemwebapp.NoConnectivityException;
import com.careemwebapp.R;
import com.careemwebapp.SerializableAddress;
import com.careemwebapp.SerializableFavorite;
import com.careemwebapp.SerializableSuggestion;
import com.careemwebapp.SuggestionType;
import com.careemwebapp.components.CustomEditText;
import com.careemwebapp.components.CustomTextView;
import com.careemwebapp.repositories.AddressHistoryRepository;
import com.careemwebapp.repositories.FavoritesAddressRepository;
import com.careemwebapp.utils.ConnectivityUtils;
import com.careemwebapp.utils.DialogUtils;
import com.careemwebapp.utils.KeyboardUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultTransform;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.R.attr.colorPrimaryDark;
import static com.careemwebapp.R.color.drawer_button_text_color;

public abstract class CommonAddressSuggestActivity extends BaseToolbarActivity {

    private static final String TAG = CommonAddressSuggestActivity.class.getName();

    public static final int REQUEST_CODE = 3123;

    private static final String REMOVE_STATE_COUNTRY_REGEX = "\\,[^\\,]*\\,[^\\,]*$";

    public static final String ADDRESS_TYPE_EXTRA = "via.rider.activities.CommonAddressSuggestActivity.ADDRESS_TYPE_EXTRA";
    public static final String RESULT_ADDRESS_EXTRA = "via.rider.activities.CommonAddressSuggestActivity.RESULT_ADDRESS_EXTRA";
    public static final String FAVORITE_ID_EXTRA = "via.rider.activities.CommonAddressSuggestActivity.FAVORITE_ID_EXTRA";
    public static final String SHOW_KEYBOARD = "via.rider.activities.CommonAddressSuggestActivity.SHOW_KEYBOARD";

    public static final String KEY_SUGGESTIONS = "suggestions";
    private static final String KEY_PREDICTIONS = "predictions";
    private static final String KEY_RESULT = "results";
    private static final String KEY_GEOMETRY = "geometry";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_ADDRESS_COMPONENTS = "address_components";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_LONG_NAME = "long_name";
    private static final String KEY_FORMATTED_ADDRESS = "formatted_address";
    private static final String KEY_TYPES = "types";
    private static final String KEY_ADMIN_LEVEL_1 = "administrative_are_level_1";
    private static final String KEY_SHORT_NAME = "short_name";
    private static final String KEY_SUBLOCALITY = "sublocality";
    private static final String KEY_LOCALITY = "locality";

    private static final String GEOCODER_API_URL =
            "http://maps.google.com/maps/api/geocode/json?address=%s&ka&sensor=false&components=country:us";

    private static final String GEOCODER_WEB_API_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&key=%s&radius=%s";
    private static final String GEOCODER_RADIOS = "10000";


    private static final int MAX_ADDRESS_SUGGESTIONS = 10;

    private static final String BUNDLE_PREDICTION_PLACE_ID = "via.rider.activities.CommonAddressSuggestActivity.BUNDLE_PREDICTION_PLACE_ID";

    private GoogleApiClient mGoogleApiClient;

    private AddressSuggestionAdapter mAddressSuggestionAdatper;
    private AddressHistoryRepository mHistoryRepository;
    private FavoritesAddressRepository mFavoritesAddressRepository;

    private ListView mAddressSuggestionsList;
    private CustomEditText mAddressInput;
    private ImageView mEditTextIv;

    private int mFavoriteIdToHide = Integer.MIN_VALUE;

    private AddressSuggestionType mAddressType;
    private Handler mTextDelayHandler = new Handler();
    private Runnable mRunnable;

    private boolean mNeedToShowKeyboard;

    protected abstract int getPickupTextColor();
    protected abstract int getDropoffTextColor();
    protected abstract int getPickupSearchIcon();
    protected abstract int getDropoffSearchIcon();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add geo data api
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        mHistoryRepository = new AddressHistoryRepository(this);

        mAddressSuggestionAdatper = new AddressSuggestionAdapter(this,
                new ArrayList<SerializableSuggestion>(),
                new AddressSuggestionAdapter.ISuggestionClickListener() {
                    @Override
                    public void onSuggestionClicked(SerializableSuggestion serializableSuggestion) {
                        //if no lat/lng are here then we can assume we have all required info for this address
                        if(serializableSuggestion.getSerializableFavorite().getLatitude() != 0
                                && serializableSuggestion.getSerializableFavorite().getLongitude() != 0) {
                            finishActivityWithResult(serializableSuggestion);
                            //if no lat/lng and we have place id then we have to request place details
                        } else if(!TextUtils.isEmpty(serializableSuggestion.getPlaceId())) {
                            getPlaceDetailsAndFinish(serializableSuggestion);
                        }
                    }
                });

        mAddressSuggestionsList = (ListView) findViewById(R.id.address_suggestions_list_view);
        mAddressSuggestionsList.setAdapter(mAddressSuggestionAdatper);

        //ImageView imageView = (ImageView) findViewById(R.id.address_suggestion_icon);
        CustomTextView textView = (CustomTextView) findViewById(R.id.address_suggestion_text);
        mEditTextIv = (ImageView) findViewById(R.id.ivSearchIcon);

        if(getIntent().hasExtra(ADDRESS_TYPE_EXTRA)) {
            mAddressType = AddressSuggestionType.valueOf(
                    getIntent().getStringExtra(ADDRESS_TYPE_EXTRA));
        } else {
            mAddressType = AddressSuggestionType.PICKUP;
        }

        mNeedToShowKeyboard = getIntent().getBooleanExtra(SHOW_KEYBOARD, true);

        switch (mAddressType) {
            case PICKUP:
                //imageView.setImageResource(R.mipmap.map_set_pickup);
                textView.setText(R.string.pickup_location_title);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                mEditTextIv.setImageResource(getPickupSearchIcon());
                break;
            case DROPOFF:
                mFavoriteIdToHide = getIntent().getIntExtra(FAVORITE_ID_EXTRA, Integer.MIN_VALUE);
                //imageView.setImageResource(R.mipmap.map_set_dropoff);
                textView.setText(R.string.dropoff_location_title);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                mEditTextIv.setImageResource(getDropoffSearchIcon());
                break;
            case FAVORITE_CUSTOM:
                //imageView.setImageResource(R.mipmap.fav_location_marker);
                textView.setText(R.string.custom_location_title);
                textView.setTextColor(getResources().getColor(R.color.favorite_custom_text_color));
                break;
            case FAVORITE_WORK:
                //imageView.setImageResource(R.mipmap.fav_work_location_marker);
                textView.setText(R.string.work_location_title);
                textView.setTextColor(getResources().getColor(R.color.favorite_work_text_color));
                break;
            case FAVORITE_HOME:
                //imageView.setImageResource(R.mipmap.fav_home_location_marker);
                textView.setText(R.string.home_location_title);
                textView.setTextColor(getResources().getColor(R.color.favorite_home_text_color));
                break;
            default:
                return;
        }

        mAddressInput = (CustomEditText) findViewById(R.id.address_input_text);
        final ImageView ivClearInput = (ImageView) findViewById(R.id.ivClearInput);

        mAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ivClearInput.setVisibility(editable.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    setDataToSuggestionsList(getFilteredFavorites(), null, mHistoryRepository.getHistory());
                    return;
                }
                //remove callback if already exists
                if(mRunnable != null) {
                    mTextDelayHandler.removeCallbacks(mRunnable);
                }
                //create a new one
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        addressSuggest(s.toString(), mSuggestionHandler);
                    }
                };
                //dealay callback
                mTextDelayHandler.postDelayed(mRunnable, 400);

            }
        });

        ivClearInput.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mAddressInput.setText("");
                                            }
                                        }
        );

        if (!ConnectivityUtils.isConnected(CommonAddressSuggestActivity.this)) {
            DialogUtils.showConnectionDialog(CommonAddressSuggestActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            return;
        }

        setDataToSuggestionsList(getFilteredFavorites(), null, mHistoryRepository.getHistory());
    }

    @Override
    public int getLayoutId() {
        return R.layout.address_suggestions;
    }

    @Override
    public int getToolbarId() {
        return R.id.toolbar;
    }

    @Override
    protected int getHomeIconResourceId() {
        return R.mipmap.ic_keyboard_arrow_left_white_24dp;
    }

    private List<SerializableFavorite> getFilteredFavorites() {
        List<SerializableFavorite> allFavorites = getFavorites();
        if (mAddressInput == null || TextUtils.isEmpty(mAddressInput.getText())) {
            return allFavorites;
        } else {
            List<SerializableFavorite> suggestions = new ArrayList<>();
            if (allFavorites != null && !allFavorites.isEmpty()) {
                for (SerializableFavorite favorite : allFavorites) {
                    if (favorite.getName().toLowerCase().contains(
                            mAddressInput.getText().toString().toLowerCase())) {
                        suggestions.add(favorite);
                    }
                }
            }
            return suggestions;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                KeyboardUtils.hideKeyboard(CommonAddressSuggestActivity.this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void finishActivityWithResult(final SerializableSuggestion address) {
        Log.i(TAG,"Address selected");
        Intent returnIntent = new Intent();
        if (address.getSuggestionType() == SuggestionType.SUGGESTION_TYPE_FAVORITE_ELEMENT && mAddressType == AddressSuggestionType.PICKUP) {
            returnIntent.putExtra(FAVORITE_ID_EXTRA, address.getSerializableFavorite().getId());
        }
        returnIntent.putExtra(ADDRESS_TYPE_EXTRA, address.getSuggestionType() == SuggestionType.SUGGESTION_TYPE_FAVORITE_ELEMENT);
        returnIntent.putExtra(RESULT_ADDRESS_EXTRA, address.getSerializableFavorite());
        setResult(RESULT_OK, returnIntent);
        KeyboardUtils.hideKeyboard(CommonAddressSuggestActivity.this);
        finish();
    }

    private Handler mSuggestionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<Address> addresses =
                    msg.getData().getParcelableArrayList(KEY_SUGGESTIONS);
            setDataToSuggestionsList(getFilteredFavorites(), addresses,
                    mHistoryRepository.getHistory());
        }
    };

    private void setDataToSuggestionsList(List<SerializableFavorite> favorites,
            ArrayList<Address> addresses, ArrayList<SerializableAddress> historyList) {
        mAddressSuggestionAdatper.clear();
        if (favorites != null && !favorites.isEmpty()) {
            mAddressSuggestionAdatper.add(new SerializableSuggestion(
                    SuggestionType.SUGGESTION_TYPE_HEADER_FAVORITE));
            for (SerializableFavorite favorite : favorites) {
                mAddressSuggestionAdatper.add(new SerializableSuggestion(favorite));
            }
        }

        //add search results to the list
        if (addresses != null && !addresses.isEmpty()) {
            mAddressSuggestionAdatper.add(new SerializableSuggestion(
                    SuggestionType.SUGGESTION_TYPE_HEADER_SUGGESTION));
            for (Address address : addresses) {
                mAddressSuggestionAdatper.add(new SerializableSuggestion(address,
                        address.getExtras() != null
                                ? address.getExtras().getString(BUNDLE_PREDICTION_PLACE_ID, null)
                                : null));
            }
        }
        //add history to the list
        if (historyList != null && !historyList.isEmpty()) {
            mAddressSuggestionAdatper.add(new SerializableSuggestion(
                    SuggestionType.SUGGESTION_TYPE_HEADER_HISTORY));
            for (SerializableAddress history : historyList) {
                mAddressSuggestionAdatper.add(new SerializableSuggestion(history));
            }
        }

        mAddressSuggestionAdatper.notifyDataSetChanged();

        if (mAddressSuggestionAdatper.getCount() != 0 && !mNeedToShowKeyboard) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtils.hideKeyboard(CommonAddressSuggestActivity.this);
                }
            }, 300);
        }
    }

    private List<Address> getAddressesFromGeocoder(String searchString) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addressList = new ArrayList<>();
        try {
            if (Geocoder.isPresent() && isNetworkAvailable()) {
                List<Address> result = geocoder.getFromLocationName( searchString,
                        MAX_ADDRESS_SUGGESTIONS);

                for (Address address : result) {
                    addressList.add(address);
                }
            }
        } catch (IOException e) {
            if (e != null && !TextUtils.isEmpty(e.getMessage())) {
            } else {
            }
            Log.e(TAG,"Impossible to connect to Geocoder: " + e);
        } finally {
            return addressList;
        }
    }

    private String executeRequest(String url) throws Exception {
        if (!isNetworkAvailable()) {
            Log.w(TAG,"No network available");
            throw new NoConnectivityException();
        }

        HttpGet httpGet = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (Exception e) {
            Log.e(TAG,"Can't perform request to google maps API");
            if (e != null && !TextUtils.isEmpty(e.getMessage())) {
            } else {
            }
            throw e;
        }
        return stringBuilder.toString();
    }

    private List<Address> getAddressesFromPredictionsWebApi(String searchString){
        List<Address> result = new ArrayList<>();

//        CityInfo cityInfo = getCityInfo();
//        String bounds = cityInfo.getCityMaxPt().getLat() + "," + cityInfo.getCityMinPt().getLng();

        String url = String.format(GEOCODER_WEB_API_URL, URLEncoder.encode(searchString), getResources().getString(R.string.google_predictions_api_key), GEOCODER_RADIOS);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(executeRequest(url));
            String status = jsonObject.getString("status");
            if(status != null) {
                if (status.equals("OK")) {
                    JSONArray jsonArray = jsonObject.getJSONArray(KEY_PREDICTIONS);
                    Address address;
                    Bundle bundle;
                    JSONObject jObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (i >= MAX_ADDRESS_SUGGESTIONS) {
                            break;
                        }
                        jObject = jsonArray.getJSONObject(i);
                        bundle = new Bundle();
                        bundle.putString(BUNDLE_PREDICTION_PLACE_ID, jObject.getString("place_id"));
                        address = new Address(Locale.getDefault());
                        address.setFeatureName(jObject.getString("description"));
                        address.setExtras(bundle);
                        result.add(address);
                    }
                } else if (status.equals("OVER_QUERY_LIMIT")) {
                    Log.e(TAG,"Query quota exceeded error in google predictions web api");
                }
                else if (status.equals("REQUEST_DENIED")) {
                    Log.e(TAG,"Request denied in google predictions web api");
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"Illegal response from google maps API");
            if (!TextUtils.isEmpty(e.getMessage())) {
            } else {
            }
            return result;
        }

        return result;
    }


    private List<Address> getAddressesFromWebApi(String searchString) throws Exception {
        List<Address> addressList = new ArrayList<>();

        JSONArray jsonArray;
        try {
            JSONObject jsonObject = new JSONObject(executeRequest(getGeocoderApiUrl(searchString)));
            jsonArray = ((JSONArray) jsonObject.get(KEY_RESULT));
        } catch (JSONException e) {
            Log.e(TAG,"Illegal response from google maps API");
            if (e != null && !TextUtils.isEmpty(e.getMessage())) {
            } else {
            }
            throw e;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (i >= MAX_ADDRESS_SUGGESTIONS) {
                    break;
                }
                JSONObject match = jsonArray.getJSONObject(i);
                Address address = convertJsonToAddress(match);
                if(address != null) {
                    addressList.add(address);
                }
            } catch (JSONException e) {
                Log.e(TAG,"Illegal JSON response from google maps API");
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                } else {
                }
                throw e;
            }
        }
        return addressList;
    }

    private Address convertJsonToAddress(JSONObject jsonAddress) throws JSONException {
        double lat = jsonAddress.getJSONObject(KEY_GEOMETRY).getJSONObject(KEY_LOCATION).getDouble(KEY_LAT);
        double lng = jsonAddress.getJSONObject(KEY_GEOMETRY).getJSONObject(KEY_LOCATION).getDouble(KEY_LNG);

        JSONArray addressComponents = jsonAddress.getJSONArray(KEY_ADDRESS_COMPONENTS);

        JSONArray addressTypes = jsonAddress.getJSONArray(KEY_TYPES);

        for(int i = 0; i < addressTypes.length(); i++) {
            if(addressTypes.getString(i).equals(KEY_COUNTRY)) {
                return null;
            }
        }

        Address address = new Address(Locale.getDefault());
        address.setLatitude(lat);
        address.setLongitude(lng);
        if(jsonAddress.has(KEY_ADDRESS_COMPONENTS) && jsonAddress.getJSONArray(KEY_ADDRESS_COMPONENTS) != null
                && jsonAddress.getJSONArray(KEY_ADDRESS_COMPONENTS).length() > 0) {
            address.setAddressLine(0, jsonAddress.getJSONArray(KEY_ADDRESS_COMPONENTS).getJSONObject(0).getString(KEY_LONG_NAME));
        } else {
            address.setAddressLine(0, jsonAddress.getString(KEY_FORMATTED_ADDRESS));
        }

        for (int i = 0; i < addressComponents.length(); i++) {
            JSONObject component = addressComponents.getJSONObject(i);
            JSONArray types = component.getJSONArray(KEY_TYPES);

            for (int j = 0; j < types.length(); j++) {
                if (types.getString(j).equals(KEY_ADMIN_LEVEL_1)) {
                    address.setAdminArea(component.getString(KEY_SHORT_NAME));
                } /*else if (types.getString(j).equals("country")) {
                    address.setCountryCode(component.getString("short_name"));
                    address.setCountryName(component.getString("long_name"));
                } else if (types.getString(j).equals("postal_code")) {
                    address.setPostalCode(component.getString("long_name"));
                }*/ else if (types.getString(j).equals(KEY_SUBLOCALITY)) {
                    address.setSubLocality(component.getString(KEY_LONG_NAME));
                } else if (types.getString(j).equals(KEY_LOCALITY)) {
                    address.setLocality(component.getString(KEY_LONG_NAME));
                }
            }
        }
        return address;
    }

    // NOTE: this method limits the suggestion space around NY.
    // As VIA expends geographically, we might want to change this behaviour.
    private String getGeocoderApiUrl(String searchString) {

        return String.format(GEOCODER_API_URL, URLEncoder.encode(searchString));
    }

    private void addressSuggest(final String searchString, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                List<Address> addressList = getAddressesFromPredictionsWebApi(searchString);
                /*try {
                    addressList.addAll(getPlaces(searchString));
                } catch (Exception ex) {
                    addressList.addAll(getAddressesFromGeocoder(searchString));
                }
                if(addressList.isEmpty()) {
                    try {
                        addressList.addAll(getAddressesFromWebApi(searchString));
                    } catch (Exception e) {
                        addressList.addAll(getAddressesFromGeocoder(searchString));
                    }
                }*/
                if(addressList.isEmpty()) {
                    addressList.addAll(getAddressesFromGeocoder(searchString));
                }

                Message msg = Message.obtain();
                msg.setTarget(handler);
                if (addressList != null && !addressList.isEmpty()) {
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(KEY_SUGGESTIONS, (ArrayList<Address>) addressList);
                    msg.setData(bundle);
                } else {
                    msg.what = 0;
                }
                msg.sendToTarget();
            }
        };
        thread.start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private List<SerializableFavorite> getFavorites() {
        List<SerializableFavorite> favorites = new ArrayList<>();
        if (mFavoritesAddressRepository == null) {
            mFavoritesAddressRepository = new FavoritesAddressRepository(this);
        }
        SerializableFavorite temp = mFavoritesAddressRepository.getHomeFavorite();
        if (!TextUtils.isEmpty(temp.getName()) && temp.getId() != mFavoriteIdToHide) {
            favorites.add(temp);
        }
        temp = mFavoritesAddressRepository.getWorkFavorite();
        if (!TextUtils.isEmpty(temp.getName()) && temp.getId() != mFavoriteIdToHide) {
            favorites.add(temp);
        }
        List<SerializableFavorite> customFavorites = mFavoritesAddressRepository.getCustomFavorites();
        if (mFavoriteIdToHide == Integer.MIN_VALUE) {
            favorites.addAll(customFavorites);
        } else {
            for (int i = 0; i < customFavorites.size(); i++) {
              if (customFavorites.get(i).getId() != mFavoriteIdToHide) {
                  favorites.add(customFavorites.get(i));
              }
            }
        }
        return favorites;
    }

    private List<Address> getPlaces(final String searchText) {
        List<Address> tempList = new ArrayList<>();
        if(mGoogleApiClient.isConnected()) {

            // Prepare request for suggestions and specify bounds where search should be performed
            // and no filter should be applied.
            PendingResult<AutocompletePredictionBuffer> suggestionResults = Places.GeoDataApi
                    .getAutocompletePredictions(mGoogleApiClient, searchText, null, null);
            // Get the suggestions and copy it into a new AutocompletePredictionBuffer object.
            // 30 is the max time of waiting for response (timeout).
            AutocompletePredictionBuffer autocompletePredictions = suggestionResults.await(30, TimeUnit.SECONDS);
            // If some results were received from the API
            if (autocompletePredictions.getStatus().isSuccess() && autocompletePredictions.getCount() > 0) {
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                // Iterate through all suggestion results and get details for each place
                Bundle bundle = new Bundle();
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    bundle.putString(BUNDLE_PREDICTION_PLACE_ID, prediction.getPlaceId());
                    Address address = new Address(Locale.getDefault());
                    final String shortAddress = prediction.getFullText(null).toString().replaceAll(REMOVE_STATE_COUNTRY_REGEX, "");
                    address.setFeatureName(shortAddress);
                    address.setExtras(bundle);
                    if(!isThisNameAdded(shortAddress, tempList)) {
                        tempList.add(address);
                    }
                }
            } else {
                if (!autocompletePredictions.getStatus().isSuccess()) {
                }
                if (autocompletePredictions.getCount() == 0) {
                }
            }
            // To avoid memory leaks release the buffer now that all data has been copied.
            autocompletePredictions.release();
        }
        return tempList;
    }

    private boolean isThisNameAdded(final String name, final List<Address> list) {
        if(list != null && !list.isEmpty()) {
            for(Address address : list) {
                if(address.getFeatureName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getPlaceDetailsAndFinish(final SerializableSuggestion serializableSuggestion) {
        if (!TextUtils.isEmpty(serializableSuggestion.getPlaceId())) {
            //Requests should not be sent in the UI thread.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, serializableSuggestion.getPlaceId());
                    PlaceBuffer detailsBuffer = placeResult.await(30, TimeUnit.SECONDS);
                    if (detailsBuffer.getStatus().isSuccess() && detailsBuffer.getCount() > 0) {
                        Iterator<Place> placeIterator = detailsBuffer.iterator();
                        if (placeIterator.hasNext()) {
                            Place tempPlace = placeIterator.next();
                            Address address = new Address(Locale.getDefault());
                            address.setLatitude(tempPlace.getLatLng().latitude);
                            address.setLongitude(tempPlace.getLatLng().longitude);
                            address.setFeatureName(tempPlace.getName().toString());
                            address.setAddressLine(0, serializableSuggestion.getSerializableFavorite().getName());
                            finishActivityWithResult(new SerializableSuggestion(address, null));
                        }
                    } else {
                        if (!detailsBuffer.getStatus().isSuccess()) {
                        }
                        if (detailsBuffer.getCount() == 0) {
                        }
                    }
                    detailsBuffer.release();
                }
            }).start();
        }
    }
}
