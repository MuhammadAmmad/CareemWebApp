package com.careemwebapp;

import android.location.Address;

/**
 * Created by alex on 5.11.15.
 */
public class SerializableSuggestion {

    private SuggestionType mSuggestionType;
    private SerializableFavorite mSerializableFavorite;
    private String mPlaceId;

    public SerializableSuggestion(SerializableAddress serializableAddress) {
        this.mSerializableFavorite = new SerializableFavorite(serializableAddress, Integer.MIN_VALUE);
        this.mSuggestionType = SuggestionType.SUGGESTION_TYPE_ADDRESS_ELEMENT;
    }

    public SerializableSuggestion(Address address, String placeId) {
        this.mSerializableFavorite = new SerializableFavorite(address, Integer.MIN_VALUE, null);
        this.mSuggestionType = SuggestionType.SUGGESTION_TYPE_PLACE_ELEMENT;
        this.mPlaceId = placeId;
    }

    public SerializableSuggestion(SerializableFavorite serializableFavorite) {
        this.mSerializableFavorite = serializableFavorite;
        this.mSuggestionType = SuggestionType.SUGGESTION_TYPE_FAVORITE_ELEMENT;
    }

    public SerializableSuggestion(SuggestionType suggestionType) {
        this.mSuggestionType = suggestionType;
    }

    public SuggestionType getSuggestionType() {
        return mSuggestionType;
    }

    public void setSuggestionType(SuggestionType suggestionType) {
        mSuggestionType = suggestionType;
    }

    public SerializableFavorite getSerializableFavorite() {
        return mSerializableFavorite;
    }

    public void setSerializableFavorite(SerializableFavorite serializableFavorite) {
        mSerializableFavorite = serializableFavorite;
    }

    public String getPlaceId() {
        return mPlaceId;
    }
}
