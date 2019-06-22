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
import android.text.TextUtils;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.latte.client.base.entity.AccessTokenKt;

import org.jetbrains.annotations.TestOnly;

import kotlin.Pair;

/**
 * Created by moko256 on 2016/07/31.
 *
 * @author moko256
 */

public class TokenSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "AccountTokenList";
    private static final int DATABASE_VERSION = 3;
    private static final String[] TABLE_COLUMNS = new String[]{
            "type",
            "url",
            "userId",
            "userName",
            "consumerKey",
            "consumerSecret",
            "token",
            "tokenSecret"
    };

    public static final String TWITTER_URL = "twitter.com";

    public TokenSQLiteOpenHelper(Context context){
        super(context,"AccountTokenList.db",null,DATABASE_VERSION);
    }

    @TestOnly
    TokenSQLiteOpenHelper(Context context, String fileName){
        super(context, fileName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table " + TABLE_NAME + "(" + TextUtils.join(",", TABLE_COLUMNS) + ", primary key(url , userId))"
        );
        sqLiteDatabase.execSQL("create unique index idindex on " + TABLE_NAME + "(url , userId)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("update " + TABLE_NAME + " set token='',tokenSecret='' where type=0");
        }
        if (oldVersion < 3) {
            DBUtilKt.addColumn(sqLiteDatabase, TABLE_NAME, "consumerKey", null);
            DBUtilKt.addColumn(sqLiteDatabase, TABLE_NAME, "consumerSecret", null);
        }
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
                    "url = '" + pair.getFirst() + "' AND " + "userId = " + pair.getSecond(),
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
                c.getString(5),
                c.getString(6),
                c.getString(7)
        );
    }

    public void addAccessToken(AccessToken accessToken){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();

            ContentValues contentValues = new ContentValues(TABLE_COLUMNS.length);
            contentValues.put("type", accessToken.getClientType());
            contentValues.put("url", accessToken.getUrl());
            contentValues.put("userName", accessToken.getScreenName());
            contentValues.put("userId", String.valueOf(accessToken.getUserId()));
            contentValues.put("consumerKey", accessToken.getConsumerKey());
            contentValues.put("consumerSecret", accessToken.getConsumerSecret());
            contentValues.put("token", accessToken.getToken());
            contentValues.put("tokenSecret", accessToken.getTokenSecret());

            database.replace(TABLE_NAME, null, contentValues);

            database.close();
        }
    }

    public void deleteAccessToken(AccessToken accessToken){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.delete(TABLE_NAME, "url = '" + accessToken.getUrl() + "' AND " + "userId = " + accessToken.getUserId(), null);
            database.close();
        }
    }

}
