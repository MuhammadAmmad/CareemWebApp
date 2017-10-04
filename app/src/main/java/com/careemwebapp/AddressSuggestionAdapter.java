package com.careemwebapp;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.careemwebapp.components.CustomTextView;

import java.util.List;

/**
 * Created by alex on 3.11.15.
 */
public class AddressSuggestionAdapter extends ArrayAdapter<SerializableSuggestion> {

    public interface ISuggestionClickListener {
        void onSuggestionClicked(SerializableSuggestion serializableSuggestion);
    }

    private List<SerializableSuggestion> mAllFavorites;
    private ISuggestionClickListener mSuggestionClickListener;

    public AddressSuggestionAdapter(Context context, List<SerializableSuggestion> objects, ISuggestionClickListener suggestionClickListener) {
        super(context, R.layout.favorites_suggestion_item, objects);
        this.mAllFavorites = objects;
        this.mSuggestionClickListener = suggestionClickListener;
    }

    @Override
    public int getCount() {
        return mAllFavorites == null ? 0 : mAllFavorites.size();
    }

    @Override
    public SerializableSuggestion getItem(int position) {
        return mAllFavorites == null ? null : mAllFavorites.get(position);
    }

    private static class ViewHolder {
        CustomTextView name;
        CustomTextView address;
        ImageView logo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SerializableSuggestion suggestion = getItem(position);
        ViewHolder viewHolder = new ViewHolder();
        if (suggestion.getSuggestionType() == SuggestionType.SUGGESTION_TYPE_ADDRESS_ELEMENT
                || suggestion.getSuggestionType() == SuggestionType.SUGGESTION_TYPE_FAVORITE_ELEMENT
                || suggestion.getSuggestionType() == SuggestionType.SUGGESTION_TYPE_PLACE_ELEMENT) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.favorites_suggestion_item, parent, false);

            viewHolder.logo = (ImageView) convertView.findViewById(R.id.favorite_suggestion_ic);
            viewHolder.name = (CustomTextView) convertView
                    .findViewById(R.id.favorite_suggestion_name);
            viewHolder.address = (CustomTextView) convertView
                    .findViewById(R.id.favorite_suggestion_address);
            viewHolder.name.setTextColor(R.color.black);
            viewHolder.address.setTextColor(R.color.black);
            if (!isEmptyString(suggestion.getSerializableFavorite().getName())) {
                viewHolder.name.setText(suggestion.getSerializableFavorite().getName());
                viewHolder.name
                        .setVisibility(isEmptyString(suggestion.getSerializableFavorite().getName())
                                ? View.GONE : View.VISIBLE);
            }

            if (!isEmptyString(suggestion.getSerializableFavorite().getDesc())) {
                viewHolder.address.setText(suggestion.getSerializableFavorite().getDesc());
                viewHolder.address
                        .setVisibility(isEmptyString(suggestion.getSerializableFavorite().getDesc())
                                ? View.GONE : View.VISIBLE);
            }

            switch (suggestion.getSerializableFavorite().getType()) {
                case FavoritesType.FAVORITE_TYPE_HOME:
                    viewHolder.logo.setImageResource(R.mipmap.fav_home_add_edit_icon);
                    break;
                case FavoritesType.FAVORITE_TYPE_WORK:
                    viewHolder.logo.setImageResource(R.mipmap.fav_work_add_edit_icon);
                    break;
                case FavoritesType.FAVORITE_TYPE_CUSTOM:
                    //viewHolder.logo.setImageResource(R.mipmap.custom_favorite_icon);
                    break;
                default:
                    //viewHolder.logo.setImageResource(R.mipmap.ic_suggestion_place);
            }
            viewHolder.logo.setVisibility(View.VISIBLE);

            //make suggetion items clickable
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSuggestionClickListener != null) {
                        mSuggestionClickListener.onSuggestionClicked(suggestion);
                    }
                }
            });

        } else {
            //header items should not be clickable
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.suggestions_header, parent, false);

            CustomTextView header = (CustomTextView) convertView.findViewById(R.id.suggestions_header_text);
            switch (suggestion.getSuggestionType()) {
                case SUGGESTION_TYPE_HEADER_FAVORITE:
                    header.setText(R.string.address_favorites);
                    break;
                case SUGGESTION_TYPE_HEADER_HISTORY:
                    header.setText(R.string.address_recent);
                    break;
                case SUGGESTION_TYPE_HEADER_SUGGESTION:
                    header.setText(R.string.address_suggestions);
                    break;
            }

        }
        return convertView;
    }

    private boolean isEmptyString(String str) {
        return str == null || str.length() == 0 || TextUtils.isEmpty(str.trim());
    }
}
