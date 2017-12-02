/*
 * Copyright 2017 The twicalico authors
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

import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2016/07/31.
 *
 * @author moko256
 */
public class TokenSQLiteOpenHelper extends SQLiteOpenHelper {
    public TokenSQLiteOpenHelper(Context context){
        super(context,"AccountTokenList.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table AccountTokenList(userName string , userId string primary key , token string , tokenSecret string);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public AccessToken getAccessToken(int index){
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query("AccountTokenList",new String[]{"userName","userId","token","tokenSecret"},null,null,null,null,null);
        if (!c.moveToPosition(index)){
            return null;
        }

        AccessToken accessToken=new AccessToken(c.getString(2),c.getString(3)){
            String screenName = c.getString(0);
            long userId = Long.parseLong(c.getString(1),10);

            @Override
            public String getScreenName() {
                return screenName;
            }

            @Override
            public long getUserId() {
                return userId;
            }
        };

        c.close();
        database.close();

        return accessToken;
    }

    public long addAccessToken(AccessToken accessToken){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("userName",accessToken.getScreenName());
        contentValues.put("userId",String.valueOf(accessToken.getUserId()));
        contentValues.put("token",accessToken.getToken());
        contentValues.put("tokenSecret",accessToken.getTokenSecret());

        database.replace("AccountTokenList", "zero", contentValues);

        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }

    public long deleteAccessToken(long userId){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("AccountTokenList","userId=?",new String[]{String.valueOf(userId)});
        long count = DatabaseUtils.queryNumEntries(database,"AccountTokenList");
        database.close();
        return count;
    }


}
