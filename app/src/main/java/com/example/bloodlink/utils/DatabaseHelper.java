package com.example.bloodlink.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BloodLinkLocal.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_IMAGES = "user_images";
    private static final String COL_UID = "uid";
    private static final String COL_IMAGE = "image_data";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_IMAGES + " (" +
                COL_UID + " TEXT PRIMARY KEY, " +
                COL_IMAGE + " BLOB)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

    // Save Image
    public boolean insertOrUpdateImage(String uid, Bitmap bitmap) {
        SQLiteDatabase db = this.getWritableDatabase();
        byte[] data = getBitmapAsByteArray(bitmap);

        ContentValues values = new ContentValues();
        values.put(COL_UID, uid);
        values.put(COL_IMAGE, data);

        // Replaces the row if the UID already exists
        long result = db.insertWithOnConflict(TABLE_IMAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // Retrieve Image
    public Bitmap getImage(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_IMAGES, new String[]{COL_IMAGE},
                COL_UID + "=?", new String[]{uid}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            byte[] imgByte = cursor.getBlob(0);
            cursor.close();
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        }
        return null;
    }

    // Convert Bitmap to Byte Array
    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Compress to PNG (or JPEG to save space)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }
}