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
import android.support.annotation.NonNull;

import com.github.moko256.twitlatte.BuildConfig;
import com.github.moko256.twitlatte.array.ArrayUtils;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.Type;

import java.io.File;
import java.util.Collection;
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

    private final boolean isTwitter;

    private static final String TABLE_NAME = "CachedUsers";
    private static final String[] TABLE_COLUMNS = new String[]{
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

    public CachedUsersSQLiteOpenHelper(Context context, AccessToken accessToken){
        super(context, accessToken != null? new File(context.getCacheDir(), accessToken.getKeyString() + "/" + "CachedUsers.db").getAbsolutePath(): null, null, BuildConfig.CACHE_DATABASE_VERSION);
        this.isTwitter = accessToken != null && accessToken.getType() == Type.TWITTER;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_NAME + "(" + ArrayUtils.toCommaSplitString(TABLE_COLUMNS) + ", primary key(id))"
        );
        db.execSQL("create unique index idindex on " + TABLE_NAME + "(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public User getCachedUser(long id){
        User user = null;
        synchronized (this) {
            SQLiteDatabase database = getReadableDatabase();
            Cursor c = database.query(
                    TABLE_NAME,
                    TABLE_COLUMNS,
                    "id=" + String.valueOf(id), null
                    , null, null, null, "1"
            );
            if (c.moveToLast()) {
                user = new User() {
                    final long id = c.getLong(0);
                    final String name = c.getString(1);
                    final String email = c.getString(2);
                    final String screenName = c.getString(3);
                    final String location = c.getString(4);
                    final String description = c.getString(5);
                    final boolean isContributorsEnabled = c.getInt(6) != 0;
                    final String profileImageURL = c.getString(7);
                    final String profileImageURLHttps = c.getString(8);
                    final boolean isDefaultProfileImage = c.getInt(9) != 0;
                    final String url = c.getString(10);
                    final boolean isProtected = c.getInt(11) != 0;
                    final int followersCount = c.getInt(12);
                    final String profileBackgroundColor = c.getString(13);
                    final String profileTextColor = c.getString(14);
                    final String profileLinkColor = c.getString(15);
                    final String profileSidebarFillColor = c.getString(16);
                    final String profileSidebarBorderColor = c.getString(17);
                    final boolean isProfileUseBackgroundImage = c.getInt(18) != 0;
                    final boolean isDefaultProfile = c.getInt(19) != 0;
                    final boolean isShowAllInlineMedia = c.getInt(20) != 0;
                    final int friendsCount = c.getInt(21);
                    final Date createdAt = new Date(c.getLong(22));
                    final int favoritesCount = c.getInt(23);
                    final int utcOffset = c.getInt(24);
                    final String timeZone = c.getString(25);
                    final String profileBackgroundImageURL = c.getString(26);
                    final String profileBackgroundImageURLHttps = c.getString(27);
                    final String profileBannerImageUrl = c.getString(28);
                    final boolean isProfileBackgroundTiled = c.getInt(29) != 0;
                    final String lang = c.getString(30);
                    final int statusesCount = c.getInt(31);
                    final boolean isGeoEnabled = c.getInt(32) != 0;
                    final boolean isVerified = c.getInt(33) != 0;
                    final boolean isTranslator = c.getInt(34) != 0;
                    final boolean isFollowRequestSent = c.getInt(35) != 0;
                    final URLEntity[] descriptionURLEntities = restoreURLEntities(
                            c.getString(36).split(","),
                            c.getString(37).split(","),
                            c.getString(38).split(","),
                            c.getString(39).split(","),
                            c.getString(40).split(","),
                            c.getString(41).split(",")
                    );
                    final String[] withheldInCountries = c.getString(42).split(",");

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
                        if (null != originalURL && originalURL.length() >= 1 && isTwitter) {
                            int index = originalURL.lastIndexOf("_");
                            int suffixIndex = originalURL.lastIndexOf(".");
                            int slashIndex = originalURL.lastIndexOf("/");
                            String url = originalURL.substring(0, index) + sizeSuffix;
                            if (suffixIndex > slashIndex) {
                                url += originalURL.substring(suffixIndex);
                            }
                            return url;
                        }
                        return originalURL;
                    }

                    @Override
                    public String getProfileImageURL() {
                        return profileImageURL;
                    }

                    @Override
                    public String get400x400ProfileImageURL() {
                        return toResizedURL(profileImageURL, "_400x400");
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
                    public String get400x400ProfileImageURLHttps() {
                        return toResizedURL(profileImageURLHttps, "_400x400");
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
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/web" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBannerRetinaURL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/web_retina" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBannerIPadURL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/ipad" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBannerIPadRetinaURL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/ipad_retina" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBannerMobileURL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/mobile" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBannerMobileRetinaURL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/mobile_retina" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBanner300x100URL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/300x100" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBanner600x200URL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/600x200" : profileBannerImageUrl;
                    }

                    @Override
                    public String getProfileBanner1500x500URL() {
                        return profileBannerImageUrl != null && isTwitter ? profileBannerImageUrl + "/1500x500" : profileBannerImageUrl;
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
                        return url == null ? null : new URLEntity() {
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
        }
        return user;
    }

    public void addCachedUser(User user){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            addCachedUserAtTransaction(user);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    public void addCachedUsers(Collection<? extends User> users){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            for (User user : users) {
                addCachedUserAtTransaction(user);
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
    }

    private void addCachedUserAtTransaction(User user){
        SQLiteDatabase database=getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMNS[0], user.getId());
        contentValues.put(TABLE_COLUMNS[1], user.getName());
        contentValues.put(TABLE_COLUMNS[2], user.getEmail());
        contentValues.put(TABLE_COLUMNS[3], user.getScreenName());
        contentValues.put(TABLE_COLUMNS[4], user.getLocation());
        contentValues.put(TABLE_COLUMNS[5], user.getDescription());
        contentValues.put(TABLE_COLUMNS[6], user.isContributorsEnabled());
        contentValues.put(TABLE_COLUMNS[7], user.getProfileImageURL());
        contentValues.put(TABLE_COLUMNS[8], user.getProfileImageURLHttps());
        contentValues.put(TABLE_COLUMNS[9], user.isDefaultProfileImage());
        contentValues.put(TABLE_COLUMNS[10], user.getURL());
        contentValues.put(TABLE_COLUMNS[11], user.isProtected());
        contentValues.put(TABLE_COLUMNS[12], user.getFollowersCount());
        contentValues.put(TABLE_COLUMNS[13], user.getProfileBackgroundColor());
        contentValues.put(TABLE_COLUMNS[14], user.getProfileTextColor());
        contentValues.put(TABLE_COLUMNS[15], user.getProfileLinkColor());
        contentValues.put(TABLE_COLUMNS[16], user.getProfileSidebarFillColor());
        contentValues.put(TABLE_COLUMNS[17], user.getProfileSidebarBorderColor());
        contentValues.put(TABLE_COLUMNS[18], user.isProfileUseBackgroundImage());
        contentValues.put(TABLE_COLUMNS[19], user.isDefaultProfile());
        contentValues.put(TABLE_COLUMNS[20], user.isShowAllInlineMedia());
        contentValues.put(TABLE_COLUMNS[21], user.getFriendsCount());
        contentValues.put(TABLE_COLUMNS[22], user.getCreatedAt().getTime());
        contentValues.put(TABLE_COLUMNS[23], user.getFavouritesCount());
        contentValues.put(TABLE_COLUMNS[24], user.getUtcOffset());
        contentValues.put(TABLE_COLUMNS[25], user.getTimeZone());
        contentValues.put(TABLE_COLUMNS[26], user.getProfileBackgroundImageURL());
        contentValues.put(TABLE_COLUMNS[27], user.getProfileBackgroundImageUrlHttps());
        contentValues.put(TABLE_COLUMNS[28], user.getProfileBannerURL() != null? user.getProfileBannerURL().replaceAll("/web$", ""): null);
        contentValues.put(TABLE_COLUMNS[29], user.isProfileBackgroundTiled());
        contentValues.put(TABLE_COLUMNS[30], user.getLang());
        contentValues.put(TABLE_COLUMNS[31], user.getStatusesCount());
        contentValues.put(TABLE_COLUMNS[32], user.isGeoEnabled());
        contentValues.put(TABLE_COLUMNS[33], user.isVerified());
        contentValues.put(TABLE_COLUMNS[34], user.isTranslator());
        contentValues.put(TABLE_COLUMNS[35], user.isFollowRequestSent());

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
        contentValues.put(TABLE_COLUMNS[36], ArrayUtils.toCommaSplitString(texts).toString());
        contentValues.put(TABLE_COLUMNS[37], ArrayUtils.toCommaSplitString(URLs).toString());
        contentValues.put(TABLE_COLUMNS[38], ArrayUtils.toCommaSplitString(expandedURLs).toString());
        contentValues.put(TABLE_COLUMNS[39], ArrayUtils.toCommaSplitString(displaysURLs).toString());
        contentValues.put(TABLE_COLUMNS[40], ArrayUtils.toCommaSplitString(starts).toString());
        contentValues.put(TABLE_COLUMNS[41], ArrayUtils.toCommaSplitString(ends).toString());
        contentValues.put(TABLE_COLUMNS[42], ArrayUtils.toCommaSplitString(user.getWithheldInCountries()).toString());

        database.replace(TABLE_NAME, null, contentValues);
    }


    public void deleteCachedUser(long id){
        synchronized (this) {
            SQLiteDatabase database = getWritableDatabase();
            database.delete(TABLE_NAME, "id=" + String.valueOf(id), null);
            database.close();
        }
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
                    return Integer.parseInt(starts[finalI].trim());
                }

                @Override
                public int getEnd() {
                    return Integer.parseInt(ends[finalI].trim());
                }
            };
        }
        return entities;
    }
}