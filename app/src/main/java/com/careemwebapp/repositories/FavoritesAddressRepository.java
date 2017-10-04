package com.careemwebapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.careemwebapp.FavoritesType;
import com.careemwebapp.SerializableFavorite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 10.9.15.
 */
public class FavoritesAddressRepository extends BaseSQLiteRepository {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "FavoritesStorage";
    private static final String TABLE_FAVORITES = "Favorites";

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE_STR = "latitude_str";
    private static final String KEY_LONGITUDE_STR = "longitude_str";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_NAME = "name";

    private static final String CREATE_FAVORITES_TABLE_QUERY =
            "CREATE TABLE " + TABLE_FAVORITES + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_LATITUDE + " REAL, "
                    + KEY_LONGITUDE + " REAL, "
                    + KEY_ADDRESS + " TEXT, "
                    + KEY_NAME + " TEXT UNIQUE, "
                    + KEY_TYPE + " INTEGER,"
                    + KEY_LATITUDE_STR + " TEXT, "
                    + KEY_LONGITUDE_STR + " TEXT "
                    + ")";

    public FavoritesAddressRepository(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public String getCreateTableQuery() {
        return CREATE_FAVORITES_TABLE_QUERY;
    }

    @Override
    public String getTableName() {
        return TABLE_FAVORITES;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == DATABASE_VERSION) {
            db.execSQL("ALTER TABLE " + getTableName() + " ADD COLUMN " + KEY_LATITUDE_STR + " TEXT");
            db.execSQL("ALTER TABLE " + getTableName() + " ADD COLUMN " + KEY_LONGITUDE_STR + " TEXT");
        } else {
            super.onUpgrade(db, oldVersion, newVersion);
        }
    }

    public long edit(final SerializableFavorite originalFavorite, final SerializableFavorite newFavorite) {
        deleteFavoriteIncludingHomeWork(originalFavorite.getName());
        long newFavId = add(newFavorite);
        if(newFavId > 0) {
            return newFavId;
        } else {
            add(originalFavorite);
            return Long.MIN_VALUE;
        }
    }

    public long add(final SerializableFavorite favorite) {
        long toRet = Long.MIN_VALUE;
        if(getFavoriteItem("SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_NAME + " LIKE ?",
                new String[]{String.valueOf(favorite.getName())}) == null) {
            ContentValues values = new ContentValues();
            values.put(KEY_LATITUDE, favorite.getLatitude());
            values.put(KEY_LONGITUDE, favorite.getLongitude());
            values.put(KEY_LATITUDE_STR, "" + favorite.getLatitude());
            values.put(KEY_LONGITUDE_STR, "" + favorite.getLongitude());
            values.put(KEY_ADDRESS, favorite.getDesc());
            values.put(KEY_NAME, favorite.getName());
            values.put(KEY_TYPE, favorite.getType());
            toRet = insertWithOnConflict(TABLE_FAVORITES, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        return toRet;
    }

    public List<SerializableFavorite> getCustomFavorites() {
        String selectQuery = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_TYPE + " = "
                + FavoritesType.FAVORITE_TYPE_CUSTOM;

        ArrayList<SerializableFavorite> favoritesList = new ArrayList<SerializableFavorite>();
        MatrixCursor cursor = rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                favoritesList.add(buildItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return favoritesList;
    }

    public SerializableFavorite getFavorite(double lat, double lng) {
        SerializableFavorite favorite = getFavoriteItem(
                "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_LATITUDE + " = "
                        + lat + " AND " + KEY_LONGITUDE + " = " + lng, null);
        if (favorite == null) {
            favorite = new SerializableFavorite(FavoritesType.FAVORITE_TYPE_CUSTOM);
        }
        return favorite;
    }

    public SerializableFavorite getHomeFavorite() {
        SerializableFavorite homeAddress = getFavoriteItem(
                "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_TYPE + " = "
                        + FavoritesType.FAVORITE_TYPE_HOME, null);
        if (homeAddress == null) {
            homeAddress = new SerializableFavorite(FavoritesType.FAVORITE_TYPE_HOME);
        }
        return homeAddress;
    }

    public SerializableFavorite getWorkFavorite() {
        SerializableFavorite workAddress = getFavoriteItem(
                "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_TYPE + " = "
                        + FavoritesType.FAVORITE_TYPE_WORK, null);
        if (workAddress == null) {
            workAddress = new SerializableFavorite(FavoritesType.FAVORITE_TYPE_WORK);
        }
        return workAddress;
    }

    private SerializableFavorite getFavoriteItem(String sqlQuery, String[] selectionArgs) {
        MatrixCursor cursor = rawQuery(sqlQuery, selectionArgs);
        SerializableFavorite favorite = null;
        if (cursor != null && cursor.moveToFirst()) {
            favorite = buildItem(cursor);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return favorite;
    }

    private SerializableFavorite buildItem(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
        double latitude;
        double longitude;

        try {
            latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE_STR)));
        } catch (Exception e) {
            latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
        }

        try {
            longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE_STR)));
        } catch (Exception e1) {
            longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
        }

        String address = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
        int type = cursor.getInt(cursor.getColumnIndex(KEY_TYPE));
        return new SerializableFavorite(id, latitude, longitude, address, type, name);
    }

    public boolean deleteFavorite(String name) {
        return delete(TABLE_FAVORITES,
                KEY_NAME + " LIKE ? AND " + KEY_TYPE + " NOT IN ("
                        + FavoritesType.FAVORITE_TYPE_HOME + ", " + FavoritesType.FAVORITE_TYPE_WORK
                        + ")", new String[]{String.valueOf(name)}) > 0;
    }

    public boolean deleteFavoriteIncludingHomeWork(String name) {
        return delete(TABLE_FAVORITES, KEY_NAME + " LIKE ?", new String[]{String.valueOf(name)}) > 0;
    }

    public void updateAddress(final SerializableFavorite originalFavorite,
            final SerializableFavorite newFavorite) {
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, newFavorite.getLatitude());
        values.put(KEY_LONGITUDE, newFavorite.getLongitude());
        values.put(KEY_LATITUDE_STR, "" + newFavorite.getLatitude());
        values.put(KEY_LONGITUDE_STR, "" + newFavorite.getLongitude());
        if (!TextUtils.isEmpty(newFavorite.getDesc())) {
            values.put(KEY_ADDRESS, newFavorite.getDesc());
        } else {
            values.put(KEY_ADDRESS, "");
        }
        values.put(KEY_NAME, newFavorite.getName());
        values.put(KEY_TYPE, newFavorite.getType());
        update(TABLE_FAVORITES, values, KEY_ID + " = " + originalFavorite.getId(), null);
    }

    public List<SerializableFavorite> getFavoritesList() {
        List<SerializableFavorite> favorites = new ArrayList<>();
        SerializableFavorite temp = getHomeFavorite();
        if (!TextUtils.isEmpty(temp.getName())) {
            favorites.add(temp);
        }
        temp = getWorkFavorite();
        if (!TextUtils.isEmpty(temp.getName())) {
            favorites.add(temp);
        }
        favorites.addAll(getCustomFavorites());
        return favorites;
    }

    public List<SerializableFavorite> getFavoritesForDropoffSuggestion() {
        List<SerializableFavorite> favorites = new ArrayList<>();
        SerializableFavorite home = getHomeFavorite();
        SerializableFavorite work = getWorkFavorite();
        if (TextUtils.isEmpty(work.getName()) && !TextUtils.isEmpty(home.getName())) {
            favorites.add(work);
            favorites.add(home);
        } else {
            favorites.add(home);
            favorites.add(work);
        }
        favorites.addAll(getCustomFavorites());
        return favorites;
    }

    public ArrayList<SerializableFavorite> getAllFavorites() {
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITES;
        ArrayList<SerializableFavorite> userPhotoList = new ArrayList<SerializableFavorite>();
        MatrixCursor cursor = rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                SerializableFavorite entry = buildItem(cursor);
                userPhotoList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return userPhotoList;
    }
}
