package com.careemwebapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alex on 15.3.16.
 */
public abstract class BaseSQLiteRepository extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DROP_TABLE_QUERY = "DROP TABLE IF EXISTS ";

    public BaseSQLiteRepository(Context context, final String dataBaseName) {
        super(context, dataBaseName, null, DATABASE_VERSION);
    }

    public BaseSQLiteRepository(Context context, final String dataBaseName, final int databaseVersion) {
        super(context, dataBaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateTableQuery());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_QUERY + getTableName());
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_QUERY + getTableName());
        onCreate(db);
    }

    public abstract String getCreateTableQuery();

    public abstract String getTableName();

    /**
     * Method for inserting a row into the database.
     * This method will automatically close the database connection.
     *
     * @param table the table to insert the row into
     * @param nullColumnHack optional; may be <code>null</code>.
     *            SQL doesn't allow inserting a completely empty row without
     *            naming at least one column name.  If your provided <code>initialValues</code> is
     *            empty, no column names are known and an empty row can't be inserted.
     *            If not set to null, the <code>nullColumnHack</code> parameter
     *            provides the name of nullable column name to explicitly insert a NULL into
     *            in the case where your <code>initialValues</code> is empty.
     * @param initialValues this map contains the initial column values for the
     *            row. The keys should be the column names and the values the
     *            column values
     * @param conflictAlgorithm for insert conflict resolver
     * @return the row ID of the newly inserted row
     */
    protected long insertWithOnConflict(String table, String nullColumnHack,
            ContentValues initialValues, int conflictAlgorithm) {
        SQLiteDatabase db = this.getWritableDatabase();
        long toRet = db.insertWithOnConflict(table, nullColumnHack, initialValues,
                conflictAlgorithm);
        db.close();
        return toRet;
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT/INSERT/UPDATE/DELETE.
     * This method will automatically close the database connection.
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are
     * not supported.
     * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
     * @throws SQLException if the SQL string is invalid
     */
    protected void execSQL(String sql, Object[] bindArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(bindArgs == null) {
            db.execSQL(sql);
        } else {
            db.execSQL(sql, bindArgs);
        }
        db.close();
    }

    /**
     * Convenience method for deleting rows in the database.
     * This method will automatically close the database connection.
     * @param table the table to delete from
     * @param whereClause the optional WHERE clause to apply when deleting.
     *            Passing null will delete all rows.
     * @param whereArgs You may include ?s in the where clause, which
     *            will be replaced by the values from whereArgs. The values
     *            will be bound as Strings.
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    protected int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        int toRet = db.delete(table, whereClause, whereArgs);
        db.close();
        return toRet;
    }

    /**
     * Convenience method for updating rows in the database.
     * This method will automatically close the database connection.
     * @param table the table to update in
     * @param values a map from column names to new column values. null is a
     *            valid value that will be translated to NULL.
     * @param whereClause the optional WHERE clause to apply when updating.
     *            Passing null will update all rows.
     * @param whereArgs You may include ?s in the where clause, which
     *            will be replaced by the values from whereArgs. The values
     *            will be bound as Strings.
     * @return the number of rows affected
     */
    protected int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        int toRet = db.update(table, values, whereClause, whereArgs);
        db.close();
        return toRet;
    }

    /**
     * Runs the provided SQL and returns a {@link Cursor} over the result set.
     * This method will automatically close the database connection amd cursor.
     * @param sql the SQL query. The SQL string must not be ; terminated
     * @param selectionArgs You may include ?s in where clause in the query,
     *     which will be replaced by the values from selectionArgs. The
     *     values will be bound as Strings.
     * @return A {@link Cursor} object, which is positioned before the first entry.
     */
    protected MatrixCursor rawQuery(String sql, String[] selectionArgs) {
        MatrixCursor matrixCursor = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        matrixCursor = copyCursor(cursor);
        cursor.close();
        db.close();
        return matrixCursor;
    }

    /** This method makes copy of a {@link Cursor} object into a {@link MatrixCursor}
     * We need this because a {@link Cursor} and a {@link SQLiteDatabase} must be closed in order
     * to avoid memory leals after fetching all desired info
     *
     * @param cursor the {@link Cursor} object, which is positioned before the first entry.
     * @return A {@link MatrixCursor} object, which is less sensitive for memory leaks when not closed.
     */
    private MatrixCursor copyCursor(final Cursor cursor) {
        if(cursor != null && cursor.moveToFirst()) {
            //get names of all available columns in the cursor
            String[] columns = cursor.getColumnNames();
            //create new MatrixCursor instance with available columns
            MatrixCursor newCursor = new MatrixCursor(columns , 1);
            do {
                // create new row for MatrixCursor
                MatrixCursor.RowBuilder b = newCursor.newRow();
                //iterate through all available columns
                for(String col: columns) {
                    // detect type of data and properly copy to MatrixCursor
                    switch (cursor.getType(cursor.getColumnIndex(col))) {
                        case Cursor.FIELD_TYPE_FLOAT:
                            b.add(cursor.getFloat(cursor.getColumnIndex(col)));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            b.add(cursor.getInt(cursor.getColumnIndex(col)));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            b.add(cursor.getString(cursor.getColumnIndex(col)));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            b.add(cursor.getBlob(cursor.getColumnIndex(col)));
                            break;
                    }

                }
                //iterate through all data in the cursor
            } while (cursor.moveToNext());
            return newCursor;
        }
        return null;
    }

    public void clearDB() {
        delete(getTableName(), null, null);
    }

}
