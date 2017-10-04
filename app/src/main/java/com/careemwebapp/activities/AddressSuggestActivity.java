package com.careemwebapp.activities;

import com.careemwebapp.R;

public class AddressSuggestActivity extends CommonAddressSuggestActivity {

    @Override
    protected int getPickupTextColor() {
        return R.color.pickup_text_color;
    }

    @Override
    protected int getDropoffTextColor() {
        return R.color.dropoff_text_color;
    }

    @Override
    protected int getPickupSearchIcon() {
        return R.mipmap.address_search;
    }

    @Override
    protected int getDropoffSearchIcon() {
        return R.mipmap.address_search;
    }
}
