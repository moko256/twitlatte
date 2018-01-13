/*
 * Copyright 2018 The twicalico authors
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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.moko256.twicalico.entity.AccessToken;
import com.github.moko256.twicalico.entity.Type;

/**
 * Created by moko256 on 2016/07/31.
 *
 * @author moko256
 */

public class TokenSQLiteOpenHelper extends SQLiteOpenHelper {

    public final static String TWITTER_URL = "twitter.com";

    public TokenSQLiteOpenHelper(Context context){
        super(context,"AccountTokenList.db",null,2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table AccountTokenList(userName string , userId integer primary key , token string , tokenSecret string , url string , type string);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Cursor c = sqLiteDatabase.query("AccountTokenList", new String[]{"userName", "userId", "token", "tokenSecret"}, null, null, null, null, null);
        while (c.moveToNext()){
            Integer type;
            String url;
            String userName;
            String tokenSecret = null;
            if (c.getString(3).matches(".*\\..*")){
                type = Type.MASTODON;
                userName = c.getString(0).split("@")[0];
                url = c.getString(3);
            } else {
                type = Type.TWITTER;
                userName = c.getString(0);
                url = TWITTER_URL;
                tokenSecret = c.getString(3);
            }
            addAccessToken(new AccessToken(
                    type,
                    url,
                    Long.valueOf(c.getString(1)),
                    userName,
                    c.getString(2),
                    tokenSecret
            ));
        }
        c.close();
    }

    public AccessToken[] getAccessTokens(){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query("AccountTokenList",new String[]{"userName", "userId", "token", "tokenSecret", "url", "type"},null,null,null,null,null);

        AccessToken[] accessTokens =new AccessToken[c.getCount()];

        while (c.moveToNext()){
            accessTokens[c.getPosition()] = new AccessToken(
                    c.getInt(5),
                    c.getString(4),
                    c.getLong(1),
                    c.getString(0),
                    c.getString(2),
                    c.getString(3)
            );
        }

        c.close();
        database.close();

        return accessTokens;
    }

    public AccessToken getAccessToken(int index){
        AccessToken[] accessTokens = getAccessTokens();
        if (index < accessTokens.length){
            return accessTokens[index];
        } else {
            return null;
        }
    }

    public long addAccessToken(AccessToken accessToken){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("type", accessToken.getType());
        contentValues.put("url", accessToken.getUrl());
        contentValues.put("userName", accessToken.getScreenName());
        contentValues.put("userId", String.valueOf(accessToken.getUserId()));
        contentValues.put("token", accessToken.getToken());
        contentValues.put("tokenSecret", accessToken.getTokenSecret());

        database.replace("AccountTokenList", null, contentValues);

        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }

    public long deleteAccessToken(long userId){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("AccountTokenList", "userId=" + String.valueOf(userId), null);
        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }


}
