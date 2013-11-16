/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2013  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.wahtod.wififixer.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zanshin on 11/16/13.
 */
public class LogDBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 2;
    public static final String DB_NAME = "log";
    public static final String TABLE_NAME = "entries";
    public static final String ID_KEY = "id";
    public static final String TEXT_KEY = "logtext";
    public static final String TIMESTAMP_KEY = "timeStamp";

    public LogDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + ID_KEY + " INTEGER PRIMARY KEY," +
                TEXT_KEY + " TEXT," +
                TIMESTAMP_KEY + " DATE DEFAULT (datetime('now','localtime')))";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void expireEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + TIMESTAMP_KEY + " >= datetime('now','-10 minutes')";
        db.execSQL(query);
        db.close();
    }

    public String getCurrentEntry(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{ID_KEY, TEXT_KEY, TIMESTAMP_KEY}, ID_KEY + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getString(2) + ":" + cursor.getString(1);
    }

    public String getAllEntries() {
        StringBuilder out = new StringBuilder();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                out.append(cursor.getString(2) + ": " + cursor.getString(1));
                out.append("\n");

            } while (cursor.moveToNext());
        }
        db.close();
        return out.toString();
    }

    public String getAllEntriesAfterId(int id) {
        StringBuilder out = new StringBuilder();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID_KEY + " > " + String.valueOf(id);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                out.append(cursor.getString(2) + ": " + cursor.getString(1));
                out.append("\n");

            } while (cursor.moveToNext());
        }
        db.close();
        return out.toString();
    }

    public int getlastEntry() {
        String query = "SELECT max(" + ID_KEY + ") FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int out = -1;
        if (cursor.moveToFirst())
            out = cursor.getInt(0);
        db.close();
        return out;
    }

    public void addLogEntry(String logEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEXT_KEY, logEntry);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
}
