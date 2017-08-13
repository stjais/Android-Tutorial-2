package com.aptech.findfriend.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by navin on 6/22/17.
 */

public class UsersProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "com.aptech.findfriend.database.UsersProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/users";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String MOBILE_NUMBER = "mobile_number";
    public static final String NAME = "name";
    public static final String GENDER = "gender";

    /**
     * Database specific constant declarations
     */
    public SQLiteDatabase db;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FindFriendDatabase";
    public static final String USERS_TABLE_NAME = "users";
    public static final String CREATE_DB_TABLE =
            " CREATE TABLE "
                    + USERS_TABLE_NAME
                    + " ("
                    + MOBILE_NUMBER + " TEXT PRIMARY KEY NOT NULL, "
                    + NAME + " TEXT NOT NULL, "
                    + GENDER + " TEXT NOT NULL "
                    + " );";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
            onCreate(db);
        }
    }

    private static HashMap<String, String> USERS_PROJECTION_MAP;

    static final int USERS = 1;
    static final int USER_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "users", USERS);
        uriMatcher.addURI(PROVIDER_NAME, "users/#", USER_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(USERS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case USERS:
                qb.setProjectionMap(USERS_PROJECTION_MAP);
                break;

            case USER_ID:
                qb.appendWhere(MOBILE_NUMBER + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == "") {
            /**
             * By default sort on users names
             */
            sortOrder = NAME;
        }

        Cursor c = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all users records
             */
            case USERS:
                return "vnd.android.cursor.dir/vnd.example.users";
            /**
             * Get a particular user
             */
            case USER_ID:
                return "vnd.android.cursor.item/vnd.example.users";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        /**
         * Add a new user record
         */
        long rowID = db.insert(USERS_TABLE_NAME, "", contentValues);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        } else {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case USERS:
                count = db.delete(USERS_TABLE_NAME, selection, selectionArgs);
                break;

            case USER_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(USERS_TABLE_NAME, MOBILE_NUMBER + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND(" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case USERS:
                count = db.update(USERS_TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case USER_ID:
                count = db.update(USERS_TABLE_NAME, contentValues, MOBILE_NUMBER + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND(" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
