package com.careemwebapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import com.careemwebapp.SerializableAddress;

import java.util.ArrayList;
import java.util.Date;

public class AddressHistoryRepository extends BaseSQLiteRepository {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "HistoryManager";

    // History table
    private static final String TABLE_HISTORY = "History";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_DESC = "description";
    private static final String KEY_SHORT_NAME = "short_name";
    private static final String KEY_TIMESTAMP = "timestamp";

    private static final int MAX_HISTORY_ENTRIES = 15;

    private static final String CREATE_HISTORY_TABLE_QUERY =
            "CREATE TABLE " + TABLE_HISTORY + "("
            + KEY_LATITUDE + " REAL, "
            + KEY_LONGITUDE + " REAL, "
            + KEY_DESC + " TEXT, "
            + KEY_SHORT_NAME + " TEXT, "
            + KEY_TIMESTAMP + " INTEGER, "
            + " UNIQUE (" + KEY_DESC + ")"
            + ")";

    private static final String DELETE_OLD_VALUES_QUERY =
            "DELETE FROM " + TABLE_HISTORY +
            " WHERE " + KEY_DESC + " NOT IN " +
            "(SELECT " + KEY_DESC +
            " FROM " + TABLE_HISTORY +
            " ORDER BY " + KEY_TIMESTAMP + " DESC " +
            " LIMIT " + MAX_HISTORY_ENTRIES + ")";

    private static final String UPGRADE_ADD_SHORT_NAME =
            "ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + KEY_SHORT_NAME + " TEXT";

    public AddressHistoryRepository(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public String getCreateTableQuery() {
        return CREATE_HISTORY_TABLE_QUERY;
    }

    @Override
    public String getTableName() {
        return TABLE_HISTORY;
    }

    public void add(SerializableAddress address) {
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, address.getLatitude());
        values.put(KEY_LONGITUDE, address.getLongitude());
        values.put(KEY_DESC, address.getDesc());
        values.put(KEY_SHORT_NAME, address.getShortName());
        values.put(KEY_TIMESTAMP, (new Date().getTime()) / 1000);
        insertWithOnConflict(TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        //Delete old values
        execSQL(DELETE_OLD_VALUES_QUERY, null);
    }

    public boolean delete(String description) {
        return delete(TABLE_HISTORY, KEY_DESC + " LIKE ?", new String[]{String.valueOf(description)}) > 0;
    }

    public ArrayList<SerializableAddress> getHistory() {
        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_TIMESTAMP + " DESC";
        ArrayList<SerializableAddress> historyList = new ArrayList<>();
        MatrixCursor cursor = rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
                String desc = cursor.getString(cursor.getColumnIndex(KEY_DESC));
                String shortName = cursor.getString(cursor.getColumnIndex(KEY_SHORT_NAME));

                SerializableAddress historyEntry = new SerializableAddress(latitude, longitude, desc, shortName);
                historyList.add(historyEntry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return historyList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == DATABASE_VERSION) {
            db.execSQL(UPGRADE_ADD_SHORT_NAME);
        } else {
            super.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
