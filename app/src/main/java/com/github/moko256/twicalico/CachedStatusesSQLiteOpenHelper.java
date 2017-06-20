/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import twitter4j.Status;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedStatusesSQLiteOpenHelper extends SQLiteOpenHelper {

    private final String[] columns = new String[]{
            "id","status"/*
            "flags",
            "createdAt",
            "text",
            "source",
            "isTruncated",
            "inReplyToStatusId",
            "inReplyToUserId",
            "isFavorited",
            "isRetweeted",
            "favoriteCount",
            "inReplyToScreenName",
            "geoLocation_lat",
            "geoLocation_lon",
            "retweetCount",
            "isPossiblySensitive",
            "lang",
            "contributorsIDs",
            "retweetedStatusId",
            "userMentionEntities_json",
            "urlEntities_json",
            "hashtagEntities_json",
            "mediaEntities_json",
            "symbolEntities_json",
            "currentUserRetweetId",
            "withheldInCountries",
            "quotedStatusId",
            "quotedStatus_json",
            "displayTextRangeStart",
            "displayTextRangeEnd",
            "userId"*/
    };

    public CachedStatusesSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, new File(context.getCacheDir(), String.valueOf(GlobalApplication.userId) + "/" + name).getAbsolutePath(), factory, version);
    }
    public CachedStatusesSQLiteOpenHelper(Context context){
        this(context, "CachedStatuses.db", null, BuildConfig.CACHE_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String columnsStr = Arrays.toString(columns);
        db.execSQL(
                "create table CachedStatuses(" + columnsStr.substring(1, columnsStr.length() - 1) + ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized Status getCachedStatus(long id){
        Status status = null;
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(
                "CachedStatuses",
                columns,
                "id=" + String.valueOf(id), null
                ,null,null,null
        );
        if (c.moveToLast()){
            try {
                ByteArrayInputStream byteArrayInputStream =new ByteArrayInputStream(c.getBlob(1));
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                status = (Status) objectInputStream.readObject();
                objectInputStream.close();
                byteArrayInputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            /*
            status = new CachedStorageStatus() {
                @Override
                public long getId() {
                    return c.getLong(0);
                }

                @Override
                public int getFlags() {
                    return c.getInt(1);
                }

                @Override
                public Date getCreatedAt() {
                    return new Date(c.getLong(2));
                }

                @Override
                public String getText() {
                    return c.getString(3);
                }

                @Override
                public String getSource() {
                    return c.getString(4);
                }

                @Override
                public boolean isTruncated() {
                    return c.getInt(5) != 0;
                }

                @Override
                public long getInReplyToStatusId() {
                    return c.getLong(6);
                }

                @Override
                public long getInReplyToUserId() {
                    return c.getLong(7);
                }

                @Override
                public Place getPlace() {
                    return null;
                }

                @Override
                public boolean isFavorited() {
                    return c.getInt(8) != 0;
                }

                @Override
                public boolean isRetweeted() {
                    return c.getInt(9) != 0;
                }

                @Override
                public int getFavoriteCount() {
                    return c.getInt(10);
                }

                @Override
                public String getInReplyToScreenName() {
                    return c.getString(11);
                }

                @Override
                public GeoLocation getGeoLocation() {
                    return new GeoLocation(c.getDouble(12), c.getDouble(13));
                }

                @Override
                public int getRetweetCount() {
                    return c.getInt(14);
                }

                @Override
                public boolean isPossiblySensitive() {
                    return c.getInt(15) != 0;
                }

                @Override
                public String getLang() {
                    return c.getString(16);
                }

                @Override
                public User getUser() {
                    return null;
                }

                @Override
                public boolean isRetweet() {
                    return false;
                }

                @Override
                public Status getRetweetedStatus() {
                    return null;
                }

                @Override
                public long getRetweetedStatusId() {
                    return c.getLong(18);
                }

                @Override
                public long[] getContributors() {
                    if (c.getString(17).length()==0){
                        return null;
                    }
                    String[] s = c.getString(17).split(",");
                    long[] l = new long[s.length];
                    for (int i = 0; i < s.length; i++){
                        l[i] = Long.valueOf(s[i]);
                    }
                    return l;
                }

                @Override
                public boolean isRetweetedByMe() {
                    return false;
                }

                @Override
                public long getCurrentUserRetweetId() {
                    return c.getLong(24);
                }

                @Override
                public Scopes getScopes() {
                    return null;
                }

                @Override
                public String[] getWithheldInCountries() {
                    return c.getString(25).split(",");
                }

                @Override
                public long getQuotedStatusId() {
                    return c.getLong(26);
                }

                @Override
                public int getDisplayTextRangeStart() {
                    return c.getInt(28);
                }

                @Override
                public int getDisplayTextRangeEnd() {
                    return c.getInt(29);
                }

                @Override
                public Status getQuotedStatus() {
                    return null;
                }

                @Override
                public int compareTo(@NonNull Status o) {
                    return 0;
                }

                @Override
                public UserMentionEntity[] getUserMentionEntities() {
                    return new UserMentionEntity[0];
                }

                @Override
                public URLEntity[] getURLEntities() {
                    return new URLEntity[0];
                }

                @Override
                public HashtagEntity[] getHashtagEntities() {
                    return new HashtagEntity[0];
                }

                @Override
                public MediaEntity[] getMediaEntities() {
                    return new MediaEntity[0];
                }

                @Override
                public SymbolEntity[] getSymbolEntities() {
                    return new SymbolEntity[0];
                }

                @Override
                public long getUserId() {
                    return c.getLong(30);
                }

                @Override
                public RateLimitStatus getRateLimitStatus() {
                    return null;
                }

                @Override
                public int getAccessLevel() {
                    return 0;
                }
            };*/
        }

        c.close();
        database.close();
        return status;
    }

    public synchronized void addCachedStatus(Status status){
        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        addCachedStatusAtTransaction(status);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    public synchronized void addCachedStatuses(Status[] statuses){
        SQLiteDatabase database=getWritableDatabase();
        database.beginTransaction();
        for (Status status : statuses) {
            addCachedStatusAtTransaction(status);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    private void addCachedStatusAtTransaction(Status status){
        byte[] serializedStatusByte = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream;
            ObjectOutputStream objectOutputStream;
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(status);
            serializedStatusByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serializedStatusByte != null){
            SQLiteDatabase database=getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", status.getId());
            contentValues.put("status", serializedStatusByte);

            Cursor c=database.query(
                    "CachedStatuses",
                    columns,
                    "id=" + String.valueOf(status.getId()),
                    null,null,null,null
            );

            if (c.moveToNext()){
                database.update("CachedStatuses", contentValues, "id=" + String.valueOf(status.getId()), null);
            } else {
                database.insert("CachedStatuses", "", contentValues);
            }

            c.close();
        }
    }


    public synchronized void deleteCachedStatus(long id){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("CachedStatuses", "id=" + String.valueOf(id), null);
        database.close();
    }

    public interface CachedStorageStatus extends Status {
        int getFlags();
        long getUserId();
        long getRetweetedStatusId();
    }
}