/*
 * Copyright 2015-2018 The twitlatte authors
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

import com.github.moko256.twitlatte.BuildConfig;
import com.github.moko256.twitlatte.entity.AccessToken;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Trend;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class CachedTrendsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "Trends";

    public CachedTrendsSQLiteOpenHelper(Context context, AccessToken accessToken){
        super(context, accessToken != null? new File(context.getCacheDir(), accessToken.getKeyString() + "/" + "Trends.db").getAbsolutePath(): null, null, BuildConfig.CACHE_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(name,volume)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("alter table " + TABLE_NAME + " add column volume");
            db.execSQL("insert into " + TABLE_NAME + "(volume) values(-1)");
        }
    }

    public List<Trend> getTrends(){
        List<Trend> trends;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(TABLE_NAME, new String[]{"name", "volume"}, null, null, null, null, null);
            trends = new ArrayList<>(c.getCount());

            while (c.moveToNext()) {
                trends.add(new CachedTrend(
                        c.getString(0),
                        c.getInt(1)
                ));
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
            database.delete(TABLE_NAME, null, null);

            for (int i = 0; i < trends.size(); i++) {
                Trend item = trends.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", item.getName());
                contentValues.put("volume", item.getTweetVolume());

                database.insert(TABLE_NAME, "", contentValues);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    private static class CachedTrend implements Trend{
        private final String name;
        private final int tweetVolume;

        private CachedTrend(String name, int tweetVolume){
            this.name = name;
            this.tweetVolume = tweetVolume;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getURL() {
            return "http://twitter.com/search?q=" + getQuery();
        }

        @Override
        public String getQuery() {
            try {
                return URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getTweetVolume() {
            return tweetVolume;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj!=null&&obj instanceof Trend&&((Trend) obj).getName().equals(name);
        }
    }
}
