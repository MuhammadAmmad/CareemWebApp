package com.careemwebapp;

import android.location.Address;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by alex on 10.9.15.
 */
public class SerializableFavorite extends SerializableAddress implements Serializable {

    private int id;
    private int mType;

    public SerializableFavorite(int id, double lat, double lng, String address, int type, String name) {
        super(lat, lng, address);
        this.id = id;
        this.mType = type;
        this.name = name;
    }

    public SerializableFavorite(double lat, double lng, String address, int type, String name) {
        super(lat, lng, address);
        this.mType = type;
        this.name = name;
    }

    public SerializableFavorite(Address address, int type, String name) {
        super(address);
        this.mType = type;
        if(!TextUtils.isEmpty(address.getFeatureName()) && TextUtils.isEmpty(name)) {
            this.name = address.getFeatureName();
        } else {
            this.name = name;
        }

    }

    public SerializableFavorite(SerializableAddress serializableAddress, int type) {
        super(serializableAddress.getLatitude(), serializableAddress.getLongitude(), serializableAddress.getDesc(), serializableAddress.getShortName());
        this.mType = type;
    }

    public SerializableFavorite(int type) {
        this.mType = type;
    }

    public int getType() {
        return mType;
    }

    public int getId() {
        return id;
    }
}
