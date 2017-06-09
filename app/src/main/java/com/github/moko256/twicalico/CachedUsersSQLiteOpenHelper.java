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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedUsersSQLiteOpenHelper extends SQLiteOpenHelper {

    private final String[] columns = new String[]{
            "id",
            "name",
            "email",
            "screenName",
            "location",
            "description",
            "isContributorsEnabled",
            "profileImageURL",
            "profileImageURLHttps",
            "isDefaultProfileImage",
            "url",
            "isProtected",
            "followersCount",
            "profileBackgroundColor",
            "profileTextColor",
            "profileLinkColor",
            "profileSidebarFillColor",
            "profileSidebarBorderColor",
            "isProfileUseBackgroundImage",
            "isDefaultProfile",
            "isShowAllInlineMedia",
            "friendsCount",
            "createdAt",
            "favoritesCount",
            "utcOffset",
            "timeZone",
            "profileBackgroundImageURL",
            "profileBackgroundImageURLHttps",
            "profileBannerImageUrl",
            "isProfileBackgroundTiled",
            "lang",
            "statusesCount",
            "isGeoEnabled",
            "isVerified",
            "isTranslator",
            "isFollowRequestSent",
            "URLEntity_texts",
            "URLEntity_URLs",
            "URLEntity_expandedURLs",
            "URLEntity_displayURLs",
            "URLEntity_starts",
            "URLEntity_ends",
            "withheldInCountries"
    };

    public CachedUsersSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, new File(context.getCacheDir(), String.valueOf(GlobalApplication.userId) + "/" + name).getAbsolutePath(), factory, version);
    }
    public CachedUsersSQLiteOpenHelper(Context context){
        this(context, "CachedUsers.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String columnsStr = Arrays.toString(columns);
        db.execSQL(
                "create table CachedUsers(" + columnsStr.substring(1, columnsStr.length() - 1) + ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized User getCachedUser(long id){
        User user = null;
        SQLiteDatabase database=getReadableDatabase();
        Cursor c=database.query(
                "CachedUsers",
                columns,
                "id=" + String.valueOf(id), null
                ,null,null,null
        );
        if (c.moveToLast()){
            user = new User() {
                long id = c.getLong(0);
                String name = c.getString(1);
                String email = c.getString(2);
                String screenName = c.getString(3);
                String location = c.getString(4);
                String description = c.getString(5);
                boolean isContributorsEnabled = c.getInt(6) != 0;
                String profileImageURL = c.getString(7);
                String profileImageURLHttps = c.getString(8);
                boolean isDefaultProfileImage = c.getInt(9) != 0;
                String url = c.getString(10);
                boolean isProtected = c.getInt(11) != 0;
                int followersCount = c.getInt(12);
                String profileBackgroundColor = c.getString(13);
                String profileTextColor = c.getString(14);
                String profileLinkColor = c.getString(15);
                String profileSidebarFillColor = c.getString(16);
                String profileSidebarBorderColor = c.getString(17);
                boolean isProfileUseBackgroundImage = c.getInt(18) != 0;
                boolean isDefaultProfile = c.getInt(19) != 0;
                boolean isShowAllInlineMedia = c.getInt(20) != 0;
                int friendsCount = c.getInt(21);
                Date createdAt = new Date(c.getLong(22));
                int favoritesCount = c.getInt(23);
                int utcOffset = c.getInt(24);
                String timeZone = c.getString(25);
                String profileBackgroundImageURL = c.getString(26);
                String profileBackgroundImageURLHttps = c.getString(27);
                String profileBannerImageUrl = c.getString(28);
                boolean isProfileBackgroundTiled = c.getInt(29) != 0;
                String lang = c.getString(30);
                int statusesCount = c.getInt(31);
                boolean isGeoEnabled = c.getInt(32) != 0;
                boolean isVerified = c.getInt(33) != 0;
                boolean isTranslator = c.getInt(34) != 0;
                boolean isFollowRequestSent = c.getInt(35) != 0;
                URLEntity[] descriptionURLEntities = restoreURLEntities(
                        c.getString(36).split(","),
                        c.getString(37).split(","),
                        c.getString(38).split(","),
                        c.getString(39).split(","),
                        c.getString(40).split(","),
                        c.getString(41).split(",")
                );
                String[] withheldInCountries = c.getString(42).split(",");

                @Override
                public long getId() {
                    return id;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getEmail() {
                    return email;
                }

                @Override
                public String getScreenName() {
                    return screenName;
                }

                @Override
                public String getLocation() {
                    return location;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public boolean isContributorsEnabled() {
                    return isContributorsEnabled;
                }

                private String toResizedURL(String originalURL, String sizeSuffix) {
                    if (null != originalURL) {
                        int index = originalURL.lastIndexOf("_");
                        int suffixIndex = originalURL.lastIndexOf(".");
                        int slashIndex = originalURL.lastIndexOf("/");
                        String url = originalURL.substring(0, index) + sizeSuffix;
                        if (suffixIndex > slashIndex) {
                            url += originalURL.substring(suffixIndex);
                        }
                        return url;
                    }
                    return null;
                }

                @Override
                public String getProfileImageURL() {
                    return profileImageURL;
                }

                @Override
                public String getBiggerProfileImageURL() {
                    return toResizedURL(profileImageURL, "_bigger");
                }

                @Override
                public String getMiniProfileImageURL() {
                    return toResizedURL(profileImageURL, "_mini");
                }

                @Override
                public String getOriginalProfileImageURL() {
                    return toResizedURL(profileImageURL, "");
                }

                @Override
                public String getProfileImageURLHttps() {
                    return profileImageURLHttps;
                }

                @Override
                public String getBiggerProfileImageURLHttps() {
                    return toResizedURL(profileImageURLHttps, "_bigger");
                }

                @Override
                public String getMiniProfileImageURLHttps() {
                    return toResizedURL(profileImageURLHttps, "_mini");
                }

                @Override
                public String getOriginalProfileImageURLHttps() {
                    return toResizedURL(profileImageURLHttps, "");
                }

                @Override
                public boolean isDefaultProfileImage() {
                    return isDefaultProfileImage;
                }

                @Override
                public String getURL() {
                    return url;
                }

                @Override
                public boolean isProtected() {
                    return isProtected;
                }

                @Override
                public int getFollowersCount() {
                    return followersCount;
                }

                @Override
                public Status getStatus() {
                    return null;
                }

                @Override
                public String getProfileBackgroundColor() {
                    return profileBackgroundColor;
                }

                @Override
                public String getProfileTextColor() {
                    return profileTextColor;
                }

                @Override
                public String getProfileLinkColor() {
                    return profileLinkColor;
                }

                @Override
                public String getProfileSidebarFillColor() {
                    return profileSidebarFillColor;
                }

                @Override
                public String getProfileSidebarBorderColor() {
                    return profileSidebarBorderColor;
                }

                @Override
                public boolean isProfileUseBackgroundImage() {
                    return isProfileUseBackgroundImage;
                }

                @Override
                public boolean isDefaultProfile() {
                    return isDefaultProfile;
                }

                @Override
                public boolean isShowAllInlineMedia() {
                    return isShowAllInlineMedia;
                }

                @Override
                public int getFriendsCount() {
                    return friendsCount;
                }

                @Override
                public Date getCreatedAt() {
                    return createdAt;
                }

                @Override
                public int getFavouritesCount() {
                    return favoritesCount;
                }

                @Override
                public int getUtcOffset() {
                    return utcOffset;
                }

                @Override
                public String getTimeZone() {
                    return timeZone;
                }

                @Override
                public String getProfileBackgroundImageURL() {
                    return profileBackgroundImageURL;
                }

                @Override
                public String getProfileBackgroundImageUrlHttps() {
                    return profileBackgroundImageURLHttps;
                }

                @Override
                public String getProfileBannerURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/web" : null;
                }

                @Override
                public String getProfileBannerRetinaURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/web_retina" : null;
                }

                @Override
                public String getProfileBannerIPadURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/ipad" : null;
                }

                @Override
                public String getProfileBannerIPadRetinaURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/ipad_retina" : null;
                }

                @Override
                public String getProfileBannerMobileURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/mobile" : null;
                }

                @Override
                public String getProfileBannerMobileRetinaURL() {
                    return profileBannerImageUrl != null ? profileBannerImageUrl + "/mobile_retina" : null;
                }

                @Override
                public boolean isProfileBackgroundTiled() {
                    return isProfileBackgroundTiled;
                }
                @Override
                public String getLang() {
                    return lang;
                }

                @Override
                public int getStatusesCount() {
                    return statusesCount;
                }

                @Override
                public boolean isGeoEnabled() {
                    return isGeoEnabled;
                }

                @Override
                public boolean isVerified() {
                    return isVerified;
                }

                @Override
                public boolean isTranslator() {
                    return isTranslator;
                }

                @Override
                public int getListedCount() {
                    return 0;
                }

                @Override
                public boolean isFollowRequestSent() {
                    return isFollowRequestSent;
                }

                @Override
                public URLEntity[] getDescriptionURLEntities() {
                    return descriptionURLEntities;
                }

                @Override
                public URLEntity getURLEntity() {
                    return url == null? null: new URLEntity() {
                        @Override
                        public String getText() {
                            return url;
                        }

                        @Override
                        public String getURL() {
                            return url;
                        }

                        @Override
                        public String getExpandedURL() {
                            return url;
                        }

                        @Override
                        public String getDisplayURL() {
                            return url;
                        }

                        @Override
                        public int getStart() {
                            return 0;
                        }

                        @Override
                        public int getEnd() {
                            return url.length();
                        }
                    };
                }

                @Override
                public String[] getWithheldInCountries() {
                    return withheldInCountries;
                }

                @Override
                public int compareTo(@NonNull User that) {
                    return (int) (this.id - that.getId());
                }

                @Override
                public RateLimitStatus getRateLimitStatus() {
                    return null;
                }

                @Override
                public int getAccessLevel() {
                    return 0;
                }

                @Override
                public int hashCode() {
                    return (int) id;
                }

                @Override
                public boolean equals(Object obj) {
                    return obj != null && (this == obj || obj instanceof User && ((User) obj).getId() == this.id);
                }
            };
        }

        c.close();
        database.close();
        return user;
    }

    public synchronized void addCachedUser(User user){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(columns[0], user.getId());
        contentValues.put(columns[1], user.getName());
        contentValues.put(columns[2], user.getEmail());
        contentValues.put(columns[3], user.getScreenName());
        contentValues.put(columns[4], user.getLocation());
        contentValues.put(columns[5], user.getDescription());
        contentValues.put(columns[6], user.isContributorsEnabled()? 1: 0);
        contentValues.put(columns[7], user.getProfileImageURL());
        contentValues.put(columns[8], user.getProfileImageURLHttps());
        contentValues.put(columns[9], user.isDefaultProfileImage()? 1: 0);
        contentValues.put(columns[10], user.getURL());
        contentValues.put(columns[11], user.isProtected()? 1: 0);
        contentValues.put(columns[12], user.getFollowersCount());
        contentValues.put(columns[13], user.getProfileBackgroundColor());
        contentValues.put(columns[14], user.getProfileTextColor());
        contentValues.put(columns[15], user.getProfileLinkColor());
        contentValues.put(columns[16], user.getProfileSidebarFillColor());
        contentValues.put(columns[17], user.getProfileSidebarBorderColor());
        contentValues.put(columns[18], user.isProfileUseBackgroundImage()? 1: 0);
        contentValues.put(columns[19], user.isDefaultProfile()? 1: 0);
        contentValues.put(columns[20], user.isShowAllInlineMedia()? 1: 0);
        contentValues.put(columns[21], user.getFriendsCount());
        contentValues.put(columns[22], user.getCreatedAt().getTime());
        contentValues.put(columns[23], user.getFavouritesCount());
        contentValues.put(columns[24], user.getUtcOffset());
        contentValues.put(columns[25], user.getTimeZone());
        contentValues.put(columns[26], user.getProfileBackgroundImageURL());
        contentValues.put(columns[27], user.getProfileBackgroundImageUrlHttps());
        contentValues.put(columns[28], user.getProfileBannerURL());
        contentValues.put(columns[29], user.isProfileBackgroundTiled()? 1: 0);
        contentValues.put(columns[30], user.getLang());
        contentValues.put(columns[31], user.getStatusesCount());
        contentValues.put(columns[32], user.isGeoEnabled()? 1: 0);
        contentValues.put(columns[33], user.isVerified()? 1: 0);
        contentValues.put(columns[34], user.isTranslator()? 1: 0);
        contentValues.put(columns[35], user.isFollowRequestSent()? 1: 0);

        int size = user.getDescriptionURLEntities().length;
        String[] texts = new String[size];
        String[] URLs = new String[size];
        String[] expandedURLs = new String[size];
        String[] displaysURLs = new String[size];
        String[] starts = new String[size];
        String[] ends = new String[size];
        for(int i = 0;i < size; i++){
            URLEntity entity = user.getDescriptionURLEntities()[i];
            texts[i] = entity.getText();
            URLs[i] = entity.getURL();
            expandedURLs[i] = entity.getExpandedURL();
            displaysURLs[i] = entity.getDisplayURL();
            starts[i] = String.valueOf(entity.getStart());
            ends[i] = String.valueOf(entity.getEnd());
        }
        contentValues.put(columns[36], Arrays.toString(texts).replace("[", "").replace("]", ""));
        contentValues.put(columns[37], Arrays.toString(URLs).replace("[", "").replace("]", ""));
        contentValues.put(columns[38], Arrays.toString(expandedURLs).replace("[", "").replace("]", ""));
        contentValues.put(columns[39], Arrays.toString(displaysURLs).replace("[", "").replace("]", ""));
        contentValues.put(columns[40], Arrays.toString(starts).replace("[", "").replace("]", ""));
        contentValues.put(columns[41], Arrays.toString(ends).replace("[", "").replace("]", ""));
        contentValues.put(columns[42], Arrays.toString(user.getWithheldInCountries()).replace("[", "").replace("]", ""));

        Cursor c=database.query(
                "CachedUsers",
                columns,
                "id=" + String.valueOf(user.getId()),
                null,null,null,null
        );

        if (c.moveToNext()){
            database.update("CachedUsers", contentValues, "id=" + String.valueOf(user.getId()), null);
        } else {
            database.insert("CachedUsers", "", contentValues);
        }

        c.close();
        database.close();
    }

    public synchronized void deleteCachedUser(long id){
        SQLiteDatabase database=getWritableDatabase();
        database.delete("CachedUsers", "id=" + String.valueOf(id), null);
    }



    private URLEntity[] restoreURLEntities(String[] texts,
                                           String[] URLs,
                                           String[] expandedURLs,
                                           String[] displaysURLs,
                                           String[] starts,
                                           String[] ends){

        URLEntity[] entities = new URLEntity[texts.length];
        for (int i = 0; i < entities.length; i++){
            int finalI = i;
            entities[i] = new URLEntity() {
                @Override
                public String getText() {
                    return texts[finalI];
                }

                @Override
                public String getURL() {
                    return URLs[finalI];
                }

                @Override
                public String getExpandedURL() {
                    return expandedURLs[finalI];
                }

                @Override
                public String getDisplayURL() {
                    return displaysURLs[finalI];
                }

                @Override
                public int getStart() {
                    return Integer.valueOf(starts[finalI]);
                }

                @Override
                public int getEnd() {
                    return Integer.valueOf(ends[finalI]);
                }
            };
        }
        return entities;
    }
}