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
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedStatusesSQLiteOpenHelper extends SQLiteOpenHelper {

    private final String[] columns = new String[]{
            "id",
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
            "userId"
    };

    public CachedStatusesSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, new File(context.getCacheDir(), name).getAbsolutePath(), factory, version);
    }
    public CachedStatusesSQLiteOpenHelper(Context context){
        super(context, "CachedStatuses.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table CachedStatuses(" +
                        "id integer," +
                        "flags integer," +
                        "createdAt integer," +
                        "text string," +
                        "source string," +
                        "isTruncated integer," +
                        "inReplyToStatusId integer," +
                        "inReplyToUserId integer," +
                        "isFavorited integer," +
                        "isRetweeted integer," +
                        "favoriteCount integer," +
                        "inReplyToScreenName integer," +
                        "geoLocation_lat real," +
                        "geoLocation_lon real," +
                        "retweetCount integer," +
                        "isPossiblySensitive integer," +
                        "lang string," +
                        "contributorsIDs string," +
                        "retweetedStatusId integer," +
                        "userMentionEntities_json string," +
                        "urlEntities_json string," +
                        "hashtagEntities_json string," +
                        "mediaEntities_json string," +
                        "symbolEntities_json string," +
                        "currentUserRetweetId integer," +
                        "withheldInCountries string," +
                        "quotedStatusId integer," +
                        "quotedStatus_json string," +
                        "displayTextRangeStart integer," +
                        "displayTextRangeEnd integer," +
                        "userId integer" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Status getCachedStatus(long id){
        Status status = null;
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(
                "CachedStatuses",
                columns,
                "id=?", new String[]{String.valueOf(id)}
                ,null,null,null
        );
        if (c.moveToLast()){
            status = new CachedStorageStatus() {
                @Override
                public int getFlags() {
                    return c.getInt(1);
                }

                @Override
                public Date getCreatedAt() {
                    return new Date(c.getLong(2));
                }

                @Override
                public long getId() {
                    return c.getLong(0);
                }

                @Override
                public String getText() {
                    return c.getString(3);
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
                public String getInReplyToScreenName() {
                    return c.getString(11);
                }

                @Override
                public GeoLocation getGeoLocation() {
                    return new GeoLocation(c.getDouble(12), c.getDouble(13));
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
                public User getUser() {
                    return null;
                }

                @Override
                public long getUserId() {
                    return c.getLong(30);
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
                public int getRetweetCount() {
                    return c.getInt(14);
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
                public boolean isPossiblySensitive() {
                    return c.getInt(15) != 0;
                }

                @Override
                public String getLang() {
                    return c.getString(16);
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
                public RateLimitStatus getRateLimitStatus() {
                    return null;
                }

                @Override
                public int getAccessLevel() {
                    return 0;
                }
            };
        }

        c.close();
        database.close();
        return status;
    }

    public void addCachedStatus(Status status){
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
            contentValues.put("id", String.valueOf(status.getId()));
            contentValues.put("status", serializedStatusByte);

            Cursor c=database.query(
                    "CachedStatuses",
                    columns,
                    "id=?",
                    new String[]{
                            String.valueOf(status.getId()),
                            status instanceof CachedStorageStatus?
                                    String.valueOf(((CachedStorageStatus) status).getFlags()):
                                    "0",
                            String.valueOf(status.getCreatedAt().getTime()),
                            status.getText(),
                            status.getSource(),
                            status.isTruncated()?"1":"0",
                            String.valueOf(status.getInReplyToStatusId()),
                            String.valueOf(status.getInReplyToUserId()),
                            String.valueOf(status.isFavorited()),
                            String.valueOf(status.isRetweeted()),
                            String.valueOf(status.getFavoriteCount()),
                            String.valueOf(status.getInReplyToScreenName()),
                            status.getGeoLocation()!=null?String.valueOf(status.getGeoLocation().getLatitude()):"",
                            status.getGeoLocation()!=null?String.valueOf(status.getGeoLocation().getLongitude()):"",
                            String.valueOf(status.getRetweetCount()),
                            status.isPossiblySensitive()?"1":"0",
                            status.getLang(),
                            "",//status.getContributors()
                            String.valueOf(
                                    status instanceof CachedStorageStatus?
                                            ((CachedStorageStatus) status).getRetweetedStatusId():
                                            status.getRetweetedStatus().getId()
                            ),
                            "userMentionEntities_json",
                            "urlEntities_json",
                            "hashtagEntities_json",
                            "mediaEntities_json",
                            "symbolEntities_json",
                            String.valueOf(status.getCurrentUserRetweetId()),
                            Arrays.toString(status.getWithheldInCountries()),
                            String.valueOf(status.getQuotedStatusId()),
                            "quotedStatus_json",
                            String.valueOf(status.getDisplayTextRangeStart()),
                            String.valueOf(status.getDisplayTextRangeEnd()),
                            String.valueOf(status.getUser().getId()),
                    }
                    ,null,null,null
            );

            if (c.moveToNext()){
                database.update("CachedStatuses", contentValues, "id=?", new String[]{String.valueOf(status.getId())});
            } else {
                database.insert("CachedStatuses", "zero", contentValues);
            }

            c.close();
            database.close();
        }
    }

    public void deleteCachedStatus(long id){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("CachedStatuses", "id=?", new String[]{String.valueOf(id)});
    }

    public interface CachedStorageStatus extends Status {
        int getFlags();
        long getUserId();
        long getRetweetedStatusId();
    }
}