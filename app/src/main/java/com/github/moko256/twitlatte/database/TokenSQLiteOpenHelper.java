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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.AccessTokenKt;

import org.jetbrains.annotations.TestOnly;

import kotlin.Pair;

/**
 * Created by moko256 on 2016/07/31.
 *
 * @author moko256
 */

public class TokenSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "AccountTokenList";
    private static final String[] TABLE_COLUMNS = new String[]{ "type", "url", "userId", "userName", "token", "tokenSecret"};

    public static final String TWITTER_URL = "twitter.com";

    public TokenSQLiteOpenHelper(Context context){
        super(context,"AccountTokenList.db",null,1);
    }

    @TestOnly
    TokenSQLiteOpenHelper(Context context, String fileName){
        super(context, fileName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table " + TABLE_NAME + "(type string , url string , userId integer , userName string , token string , tokenSecret string , primary key(url , userId))"
        );
        sqLiteDatabase.execSQL("create unique index idindex on " + TABLE_NAME + "(url , userId)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public AccessToken[] getAccessTokens(){
        AccessToken[] accessTokens;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(TABLE_NAME, TABLE_COLUMNS, null, null, null, null, null);

            accessTokens = new AccessToken[c.getCount()];

            while (c.moveToNext()) {
                accessTokens[c.getPosition()] = convertFromCursor(c);
            }

            c.close();
            database.close();
        }

        return accessTokens;
    }

    public AccessToken getAccessToken(String key){
        Pair<String, Long> pair = AccessTokenKt.splitAccessTokenKey(key);
        AccessToken accessToken;

        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(
                    TABLE_NAME,
                    TABLE_COLUMNS,
                    "url = '" + pair.getFirst() + "' AND " + "userId = " + String.valueOf(pair.getSecond()),
                    null, null, null, null, "1");


            if (c.moveToNext()) {
                accessToken = convertFromCursor(c);
            } else {
                accessToken = null;
            }
            c.close();
            database.close();
        }

        return accessToken;
    }

    private AccessToken convertFromCursor(Cursor c){
        return new AccessToken(
                c.getInt(0),
                c.getString(1),
                c.getLong(2),
                c.getString(3),
                c.getString(4),
                c.getString(5)
        );
    }

    public void addAccessToken(AccessToken accessToken){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();

            ContentValues contentValues = new ContentValues(TABLE_COLUMNS.length);
            contentValues.put("type", accessToken.getType());
            contentValues.put("url", accessToken.getUrl());
            contentValues.put("userName", accessToken.getScreenName());
            contentValues.put("userId", String.valueOf(accessToken.getUserId()));
            contentValues.put("token", accessToken.getToken());
            contentValues.put("tokenSecret", accessToken.getTokenSecret());

            database.replace(TABLE_NAME, null, contentValues);

            database.close();
        }
    }

    public void deleteAccessToken(AccessToken accessToken){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.delete(TABLE_NAME, "url = '" + accessToken.getUrl() + "' AND " + "userId = " + String.valueOf(accessToken.getUserId()), null);
            database.close();
        }
    }

    public long getSize(){
        long count;
        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            count = DatabaseUtils.queryNumEntries(database, TABLE_NAME);
            database.close();
        }
        return count;
    }

}
