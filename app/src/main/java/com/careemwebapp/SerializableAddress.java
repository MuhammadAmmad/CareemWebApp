package com.careemwebapp;

import android.location.Address;

import java.io.Serializable;


public class SerializableAddress implements Serializable {
    private double lat;
    private double lng;
    private String desc;
    protected String name;
    private String shortAddress;
    private String shortName;

    public SerializableAddress() {

    }

    public SerializableAddress(double lat, double lng, String desc, String shortName) {
        this.lat = lat;
        this.lng = lng;
        this.desc = desc;
        this.shortName = shortName;
    }

    public SerializableAddress(double lat, double lng, String desc) {
        this.lat = lat;
        this.lng = lng;
        this.desc = desc;
    }

    public SerializableAddress(Address address) {
        if(address.hasLatitude()) {
            this.lat = address.getLatitude();
        }
        if(address.hasLongitude()) {
            this.lng = address.getLongitude();
        }
        this.desc = AddressFinder.formatFullAddressString(address);
        this.name = address.getFeatureName();
        this.shortAddress = AddressFinder.formatShortAddressString(address);
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    public String getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(String shortAddress) {
        this.shortAddress = shortAddress;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}
