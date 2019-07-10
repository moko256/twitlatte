/*
 * Copyright 2015-2019 The twitlatte authors
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

package com.github.moko256.twitlatte.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.latte.client.base.entity.Trend;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class CachedTrendsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "Trends";

    public CachedTrendsSQLiteOpenHelper(Context context, AccessToken accessToken){
        super(context, accessToken != null? new File(context.getCacheDir(), accessToken.getKeyString() + "/" + "Trends.db").getAbsolutePath(): null, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(name,volume)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            DBUtilKt.addColumn(db, TABLE_NAME, "volume", "-1");
        }
    }

    public List<Trend> getTrends(){
        List<Trend> trends;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(TABLE_NAME, new String[]{"name", "volume"}, null, null, null, null, null);

            try {
                trends = new ArrayList<>(c.getCount());

                while (c.moveToNext()) {
                    trends.add(new Trend(
                            c.getString(0),
                            c.getInt(1)
                    ));
                }
            } catch (Throwable e) {
                trends = Collections.emptyList();
                SQLiteDatabase writableDatabase = getWritableDatabase();
                writableDatabase.delete(TABLE_NAME, null, null);
                writableDatabase.close();
            }

            c.close();
            database.close();
        }

        return trends;
    }

    public void setTrends(List<Trend> trends){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();

            try {
                database.delete(TABLE_NAME, null, null);

                for (int i = 0; i < trends.size(); i++) {
                    Trend item = trends.get(i);
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put("name", item.getName());
                    contentValues.put("volume", item.getVolume());

                    database.insert(TABLE_NAME, null, contentValues);
                }

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }

            database.close();
        }
    }
}
