/*
 * Copyright 2015-2018 The twicalico authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twicalico.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.twicalico.BuildConfig;

import java.io.File;
import java.util.List;

/**
 * Created by moko256 on 2018/01/04.
 *
 * @author moko256
 */

class CachedStatusesCountSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "StatusesCount";

    CachedStatusesCountSQLiteOpenHelper(Context context, long userId){
        super(context, new File(context.getCacheDir(), String.valueOf(userId) + "/StatusesCount.db").getAbsolutePath(), null, BuildConfig.CACHE_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_NAME + "(id, count, primary key(id))");
        sqLiteDatabase.execSQL("create unique index idindex on " + TABLE_NAME + "(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void incrementCounts(List<Long> ids){
        int[] counts = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            counts[i] = getCount(ids.get(i));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (int i = 0; i < ids.size(); i++) {
            incrementCountAtTransaction(ids.get(i), counts[i]);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public void decrementCounts(List<Long> ids){
        int[] counts = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            counts[i] = getCount(ids.get(i));
        }

        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (int i = 0; i < ids.size(); i++) {
            decrementCountAtTransaction(ids.get(i), counts[i]);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void incrementCountAtTransaction(long id, int count){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("id", id);
        contentValues.put("count", count + 1);
        SQLiteDatabase database = getWritableDatabase();
        database.replace(TABLE_NAME, null, contentValues);
    }

    private void decrementCountAtTransaction(long id, int oldCount) {
        SQLiteDatabase database = getWritableDatabase();
        int count = oldCount - 1;
        if (count == 0) {
            database.delete(TABLE_NAME, "id=" + String.valueOf(id), null);
        } else {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put("id", id);
            contentValues.put("count", count);
            database.replace(TABLE_NAME, null, contentValues);
        }
    }

    public int getCount(long id){
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{"id", "count"},
                "id=" + String.valueOf(id), null,
                null, null, null, "1"
        );
        if (cursor.moveToNext()){
            count = cursor.getInt(1);
        }
        cursor.close();
        database.close();
        return count;
    }

}