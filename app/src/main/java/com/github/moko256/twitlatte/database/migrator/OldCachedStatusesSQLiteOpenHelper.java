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

package com.github.moko256.twitlatte.database.migrator;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.moko256.twitlatte.entity.Emoji;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kotlin.Pair;
import kotlin.sequences.Sequence;
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

public class OldCachedStatusesSQLiteOpenHelper {

    private static final String TABLE_NAME = "CachedStatuses";
    private static final String[] TABLE_COLUMNS = new String[]{
            "createdAt",
            "id",
            "userId",
            "retweetedStatusId",
            "text",
            "source",
            "inReplyToStatusId",
            "inReplyToUserId",
            "isFavorited",
            "isRetweeted",
            "favoriteCount",
            "inReplyToScreenName",
            "retweetCount",
            "isPossiblySensitive",
            "lang",
            "UserMentionEntity_texts",
            "UserMentionEntity_ids",
            "UserMentionEntity_names",
            "UserMentionEntity_screenNames",
            "UserMentionEntity_starts",
            "UserMentionEntity_ends",
            "URLEntity_texts",
            "URLEntity_expandedURLs",
            "URLEntity_displayURLs",
            "URLEntity_starts",
            "URLEntity_ends",
            "HashtagEntity_texts",
            "HashtagEntity_starts",
            "HashtagEntity_ends",
            "MediaEntity_texts",
            "MediaEntity_expandedURLs",
            "MediaEntity_displayURLs",
            "MediaEntity_ids",
            "MediaEntity_MediaURLs",
            "MediaEntity_MediaURLHttpSs",
            "MediaEntity_types",
            "MediaEntity_Variants_bitrates",
            "MediaEntity_Variants_contentTypes",
            "MediaEntity_Variants_uris",
            "MediaEntity_starts",
            "MediaEntity_ends",
            "SymbolEntity_texts",
            "SymbolEntity_starts",
            "SymbolEntity_ends",
            "quotedStatusId",
            "url",
            "Emoji_shortcodes",
            "Emoji_urls",
            "contentWarning",
            "repliesCount",
            "count"
    };

    public static Result getCachedStatus(SQLiteDatabase database){
        @SuppressLint("Recycle") Cursor c = database.query(
                TABLE_NAME,
                TABLE_COLUMNS,
                null, null, null, null, null
        );
        return new Result(c);
    }

    @Nullable
    private static String[] splitComma(@Nullable String string){
        if (!TextUtils.isEmpty(string)) {
            return string.split(",");
        } else {
            return null;
        }
    }

    private static UserMentionEntity[] restoreUserMentionEntities(String[] texts,
                                                           String[] ids,
                                                           String[] names,
                                                           String[] screenNames,
                                                           String[] starts,
                                                           String[] ends){

        if (texts != null){
            UserMentionEntity[] entities = new UserMentionEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedUserMentionEntity(
                        texts[i],
                        ids[i],
                        names[i],
                        screenNames[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new UserMentionEntity[0];
        }
    }

    private static final class CachedUserMentionEntity implements UserMentionEntity {

        private final String text;
        private final String name;
        private final String screenName;
        private final String id;
        private final String start;
        private final String end;

        CachedUserMentionEntity(String text,
                                String id,
                                String name,
                                String screenName,
                                String start,
                                String end){
            this.text = text;
            this.id = id;
            this.name = name;
            this.screenName = screenName;
            this.start = start;
            this.end = end;

        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getScreenName() {
            return screenName;
        }

        @Override
        public long getId() {
            return Long.parseLong(id);
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    private static URLEntity[] restoreURLEntities(String[] texts,
                                           String[] expandedURLs,
                                           String[] displaysURLs,
                                           String[] starts,
                                           String[] ends){

        if (texts != null){
            URLEntity[] entities = new URLEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedURLEntity(
                        texts[i],
                        expandedURLs[i],
                        displaysURLs[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new URLEntity[0];
        }
    }

    private static final class CachedURLEntity implements URLEntity {

        private final String text;
        private final String expandedURL;
        private final String displaysURL;
        private final String start;
        private final String end;

        CachedURLEntity(String text,
                String expandedURL,
                String displaysURL,
                String start,
                String end){
            this.text = text;
            this.expandedURL = expandedURL;
            this.displaysURL = displaysURL;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getURL() {
            return text;
        }

        @Override
        public String getExpandedURL() {
            return expandedURL;
        }

        @Override
        public String getDisplayURL() {
            return displaysURL;
        }

        @Override
        public int getStart() {
            return Integer.valueOf(start);
        }

        @Override
        public int getEnd() {
            return Integer.valueOf(end);
        }

    }

    private static HashtagEntity[] restoreHashtagEntities(String[] texts,
                                               String[] starts,
                                               String[] ends){

        if (texts != null){
            HashtagEntity[] entities = new HashtagEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedHashtagEntity(
                        texts[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new HashtagEntity[0];
        }
    }

    private static final class CachedHashtagEntity implements HashtagEntity {
        private final String text;
        private final String start;
        private final String end;

        CachedHashtagEntity(String text,
                String start,
                String end){
            this.text = text;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    private static MediaEntity[] restoreMediaEntities(String[] texts,
                                               String[] expandedURLs,
                                               String[] displaysURLs,
                                               String[] ids,
                                               String[] mediaUrls,
                                               String[] mediaUrlHttpSs,
                                               String[] types,
                                               String[][] variants_bitrates,
                                               String[][] variants_contentTypes,
                                               String[][] variants_urls,
                                               String[] starts,
                                               String[] ends){

        if (texts != null){
            MediaEntity[] entities = new MediaEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                boolean hasMedia = variants_bitrates != null && variants_bitrates.length != 0;
                entities[i] = new CachedMediaEntity(
                        texts[i],
                        expandedURLs[i],
                        displaysURLs[i],
                        ids[i],
                        mediaUrls[i],
                        mediaUrlHttpSs[i],
                        types[i],
                        hasMedia && i < variants_bitrates.length ? variants_bitrates[i] : null,
                        hasMedia && i < variants_contentTypes.length ? variants_contentTypes[i] : null,
                        hasMedia && i < variants_urls.length ? variants_urls[i] : null,
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new MediaEntity[0];
        }
    }

    private static final class CachedMediaEntity implements MediaEntity{

        private final String text;
        private final String expandedURL;
        private final String displaysURL;
        private final String id;
        private final String mediaUrl;
        private final String mediaUrlHttps;
        private final String mediaType;
        private final String[] variants_bitrate;
        private final String[] variants_contentType;
        private final String[] variants_url;
        private final String start;
        private final String end;

        CachedMediaEntity(String text,
                String expandedURL,
                String displaysURL,
                String id,
                String mediaUrl,
                String mediaUrlHttps,
                String mediaType,
                String[] variants_bitrate,
                String[] variants_contentType,
                String[] variants_url,
                String start,
                String end){

            this.text = text;
            this.expandedURL = expandedURL;
            this.displaysURL = displaysURL;
            this.id = id;
            this.mediaUrl = mediaUrl;
            this.mediaUrlHttps = mediaUrlHttps;
            this.mediaType = mediaType;
            this.variants_bitrate = variants_bitrate;
            this.variants_contentType = variants_contentType;
            this.variants_url = variants_url;
            this.start = start;
            this.end = end;
        }

        @Override
        public long getId() {
            return Long.parseLong(id);
        }

        @Override
        public String getMediaURL() {
            return mediaUrl;
        }

        @Override
        public String getMediaURLHttps() {
            return mediaUrlHttps;
        }

        @Override
        public Map<Integer, MediaEntity.Size> getSizes() {
            return null;
        }

        @Override
        public String getType() {
            return mediaType;
        }

        @Override
        public int getVideoAspectRatioWidth() {
            return 0;
        }

        @Override
        public int getVideoAspectRatioHeight() {
            return 0;
        }

        @Override
        public long getVideoDurationMillis() {
            return 0;
        }

        @Override
        public MediaEntity.Variant[] getVideoVariants() {
            if (variants_url != null) {
                MediaEntity.Variant[] result = new MediaEntity.Variant[variants_url.length];
                for (int ii = 0; ii < variants_url.length; ii++) {
                    result[ii] = new Variant(
                            variants_bitrate[ii],
                            variants_contentType[ii],
                            variants_url[ii]);
                }
                return result;
            } else {
                return null;
            }
        }

        @Override
        public String getExtAltText() {
            return null;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getURL() {
            return text;
        }

        @Override
        public String getExpandedURL() {
            return expandedURL;
        }

        @Override
        public String getDisplayURL() {
            return displaysURL;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }

        private static final class Variant implements MediaEntity.Variant {
            private final String bitrate;
            private final String contentType;
            private final String url;

            Variant(String bitrate,
                    String contentType,
                    String url){
                this.bitrate = bitrate;
                this.contentType = contentType;
                this.url = url;
            }

            @Override
            public int getBitrate() {
                return Integer.parseInt(bitrate);
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getUrl() {
                return url;
            }
        }
    }

    @Nullable
    private static String[][] parse(@Nullable String string){
        if (TextUtils.isEmpty(string)){
            return null;
        }
        String[] resultA = string.split(",");
        if (resultA.length == 1 && resultA[0].equals("")){
            return null;
        }
        String[][] result = new String[resultA.length][];
        for (int i = 0; i < resultA.length; i++) {
            result[i] = resultA[i].split("\\|");
        }
        return result;
    }

    private static SymbolEntity[] restoreSymbolEntities(String[] texts,
                                                  String[] starts,
                                                  String[] ends){

        if (texts != null){
            SymbolEntity[] entities = new SymbolEntity[texts.length];
            for (int i = 0; i < entities.length; i++){
                entities[i] = new CachedSymbolEntity(
                        texts[i],
                        starts[i],
                        ends[i]
                );
            }
            return entities;
        } else {
            return new SymbolEntity[0];
        }
    }

    private static final class CachedSymbolEntity implements SymbolEntity {
        private final String text;
        private final String start;
        private final String end;

        CachedSymbolEntity(String text,
                            String start,
                            String end){
            this.text = text;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStart() {
            return Integer.parseInt(start);
        }

        @Override
        public int getEnd() {
            return Integer.parseInt(end);
        }
    }

    @Nullable
    private static List<Emoji> restoreEmojis(@Nullable String[] shortcodes,
                                              @Nullable String[] urls){

        if (shortcodes != null && urls != null) {
            List<Emoji> emojis = new ArrayList<>(shortcodes.length);
            for (int i = 0; i < shortcodes.length; i++) {
                emojis.add(new Emoji(shortcodes[i], urls[i]));
            }

            return emojis;
        } else {
            return null;
        }
    }

    public static class CachedStatus implements Status {
        /* Based on twitter4j.StatusJSONImpl */

        private final Date createdAt;
        private final long id;

        private final long userId;

        private final long retweetedStatusId;

        private final String text;
        private final String source;
        //private final boolean isTruncated;
        private final long inReplyToStatusId;
        private final long inReplyToUserId;
        private final boolean isFavorited;
        private final boolean isRetweeted;
        private final int favoriteCount;
        private final String inReplyToScreenName;
        //private final GeoLocation geoLocation;
        //private final Place place;

        private final int retweetCount;
        private final boolean isPossiblySensitive;
        private final String lang;

        //private final long[] contributorsIDs;

        private final UserMentionEntity[] userMentionEntities;
        private final URLEntity[] urlEntities;
        private final HashtagEntity[] hashtagEntities;
        private final MediaEntity[] mediaEntities;
        private final SymbolEntity[] symbolEntities;
        //private final long currentUserRetweetId;
        //private final Scopes scopes;
        //private final String[] withheldInCountries;
        private final long quotedStatusId;

        //private final int displayTextRangeStart;
        //private final int displayTextRangeEnd;

        private final String url;
        private final List<Emoji> emojis;
        private final String spoilerText;
        private final int repliesCount;

        CachedStatus(Date createdAt, long id, long userId, long retweetedStatusId, String text, String source, long inReplyToStatusId, long inReplyToUserId, boolean isFavorited, boolean isRetweeted, int favoriteCount, String inReplyToScreenName, int retweetCount, boolean isPossiblySensitive, String lang, UserMentionEntity[] userMentionEntities, URLEntity[] urlEntities, HashtagEntity[] hashtagEntities, MediaEntity[] mediaEntities, SymbolEntity[] symbolEntities, long quotedStatusId, String url, List<Emoji> emojis, String spoilerText, int repliesCount) {
            this.createdAt = createdAt;
            this.id = id;
            this.userId = userId;
            this.retweetedStatusId = retweetedStatusId;

            if (retweetedStatusId == -1) {
                this.text = text;
                this.source = source;
                this.inReplyToStatusId = inReplyToStatusId;
                this.inReplyToUserId = inReplyToUserId;
                this.isFavorited = isFavorited;
                this.isRetweeted = isRetweeted;
                this.favoriteCount = favoriteCount;
                this.inReplyToScreenName = inReplyToScreenName;
                this.retweetCount = retweetCount;
                this.isPossiblySensitive = isPossiblySensitive;
                this.lang = lang;
                this.userMentionEntities = userMentionEntities;
                this.urlEntities = urlEntities;
                this.hashtagEntities = hashtagEntities;
                this.mediaEntities = mediaEntities;
                this.symbolEntities = symbolEntities;
                this.quotedStatusId = quotedStatusId;
                this.url = url;
                this.emojis = emojis;
                this.spoilerText = spoilerText;
                this.repliesCount = repliesCount;
            } else {
                this.text=null;
                this.source=null;
                //this.isTruncated=false;
                this.inReplyToStatusId=-1;
                this.inReplyToUserId=-1;
                this.isFavorited=false;
                this.isRetweeted=false;
                this.favoriteCount=-1;
                this.inReplyToScreenName=null;
                //this.geoLocation = null;
                //this.place = null;

                this.retweetCount=-1;
                this.isPossiblySensitive=false;
                this.lang=null;

                //this.contributorsIDs=null;

                this.userMentionEntities=null;
                this.urlEntities=null;
                this.hashtagEntities=null;
                this.mediaEntities=null;
                this.symbolEntities=null;
                //this.currentUserRetweetId = -1;
                //this.scopes=null;
                //this.withheldInCountries = null;
                this.quotedStatusId = -1;

                //this.displayTextRangeStart = -1;
                //this.displayTextRangeEnd = -1;
                this.url = url;
                this.emojis = null;
                this.spoilerText = null;
                this.repliesCount = -1;
            }
        }

        @Override
        public Date getCreatedAt() {
            return createdAt;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public boolean isTruncated() {
            return false;
        }

        @Override
        public long getInReplyToStatusId() {
            return inReplyToStatusId;
        }

        @Override
        public long getInReplyToUserId() {
            return inReplyToUserId;
        }

        @Override
        public String getInReplyToScreenName() {
            return inReplyToScreenName;
        }

        @Override
        public GeoLocation getGeoLocation() {
            return null;
        }

        @Override
        public Place getPlace() {
            return null;
        }

        @Override
        public boolean isFavorited() {
            return isFavorited;
        }

        @Override
        public boolean isRetweeted() {
            return isRetweeted;
        }

        @Override
        public int getFavoriteCount() {
            return favoriteCount;
        }

        @Override
        public User getUser() {
            return new User() {
                @Override
                public long getId() {
                    return userId;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public String getEmail() {
                    return null;
                }

                @Override
                public String getScreenName() {
                    return null;
                }

                @Override
                public String getLocation() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return null;
                }

                @Override
                public boolean isContributorsEnabled() {
                    return false;
                }

                @Override
                public String getProfileImageURL() {
                    return null;
                }

                @Override
                public String getBiggerProfileImageURL() {
                    return null;
                }

                @Override
                public String getMiniProfileImageURL() {
                    return null;
                }

                @Override
                public String getOriginalProfileImageURL() {
                    return null;
                }

                @Override
                public String get400x400ProfileImageURL() {
                    return null;
                }

                @Override
                public String getProfileImageURLHttps() {
                    return null;
                }

                @Override
                public String getBiggerProfileImageURLHttps() {
                    return null;
                }

                @Override
                public String getMiniProfileImageURLHttps() {
                    return null;
                }

                @Override
                public String getOriginalProfileImageURLHttps() {
                    return null;
                }

                @Override
                public String get400x400ProfileImageURLHttps() {
                    return null;
                }

                @Override
                public boolean isDefaultProfileImage() {
                    return false;
                }

                @Override
                public String getURL() {
                    return null;
                }

                @Override
                public boolean isProtected() {
                    return false;
                }

                @Override
                public int getFollowersCount() {
                    return 0;
                }

                @Override
                public Status getStatus() {
                    return null;
                }

                @Override
                public String getProfileBackgroundColor() {
                    return null;
                }

                @Override
                public String getProfileTextColor() {
                    return null;
                }

                @Override
                public String getProfileLinkColor() {
                    return null;
                }

                @Override
                public String getProfileSidebarFillColor() {
                    return null;
                }

                @Override
                public String getProfileSidebarBorderColor() {
                    return null;
                }

                @Override
                public boolean isProfileUseBackgroundImage() {
                    return false;
                }

                @Override
                public boolean isDefaultProfile() {
                    return false;
                }

                @Override
                public boolean isShowAllInlineMedia() {
                    return false;
                }

                @Override
                public int getFriendsCount() {
                    return 0;
                }

                @Override
                public Date getCreatedAt() {
                    return null;
                }

                @Override
                public int getFavouritesCount() {
                    return 0;
                }

                @Override
                public int getUtcOffset() {
                    return 0;
                }

                @Override
                public String getTimeZone() {
                    return null;
                }

                @Override
                public String getProfileBackgroundImageURL() {
                    return null;
                }

                @Override
                public String getProfileBackgroundImageUrlHttps() {
                    return null;
                }

                @Override
                public String getProfileBannerURL() {
                    return null;
                }

                @Override
                public String getProfileBannerRetinaURL() {
                    return null;
                }

                @Override
                public String getProfileBannerIPadURL() {
                    return null;
                }

                @Override
                public String getProfileBannerIPadRetinaURL() {
                    return null;
                }

                @Override
                public String getProfileBannerMobileURL() {
                    return null;
                }

                @Override
                public String getProfileBannerMobileRetinaURL() {
                    return null;
                }

                @Override
                public String getProfileBanner300x100URL() {
                    return null;
                }

                @Override
                public String getProfileBanner600x200URL() {
                    return null;
                }

                @Override
                public String getProfileBanner1500x500URL() {
                    return null;
                }

                @Override
                public boolean isProfileBackgroundTiled() {
                    return false;
                }

                @Override
                public String getLang() {
                    return null;
                }

                @Override
                public int getStatusesCount() {
                    return 0;
                }

                @Override
                public boolean isGeoEnabled() {
                    return false;
                }

                @Override
                public boolean isVerified() {
                    return false;
                }

                @Override
                public boolean isTranslator() {
                    return false;
                }

                @Override
                public int getListedCount() {
                    return 0;
                }

                @Override
                public boolean isFollowRequestSent() {
                    return false;
                }

                @Override
                public URLEntity[] getDescriptionURLEntities() {
                    return new URLEntity[0];
                }

                @Override
                public URLEntity getURLEntity() {
                    return null;
                }

                @Override
                public String[] getWithheldInCountries() {
                    return new String[0];
                }

                @Override
                public int compareTo(@NonNull User o) {
                    return 0;
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

        @Override
        public boolean isRetweet() {
            return retweetedStatusId!=-1;
        }

        @Override
        public Status getRetweetedStatus() {
            return new Status() {
                @Override
                public Date getCreatedAt() {
                    return null;
                }

                @Override
                public long getId() {
                    return retweetedStatusId;
                }

                @Override
                public String getText() {
                    return null;
                }

                @Override
                public int getDisplayTextRangeStart() {
                    return 0;
                }

                @Override
                public int getDisplayTextRangeEnd() {
                    return 0;
                }

                @Override
                public String getSource() {
                    return null;
                }

                @Override
                public boolean isTruncated() {
                    return false;
                }

                @Override
                public long getInReplyToStatusId() {
                    return 0;
                }

                @Override
                public long getInReplyToUserId() {
                    return 0;
                }

                @Override
                public String getInReplyToScreenName() {
                    return null;
                }

                @Override
                public GeoLocation getGeoLocation() {
                    return null;
                }

                @Override
                public Place getPlace() {
                    return null;
                }

                @Override
                public boolean isFavorited() {
                    return false;
                }

                @Override
                public boolean isRetweeted() {
                    return false;
                }

                @Override
                public int getFavoriteCount() {
                    return 0;
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
                public long[] getContributors() {
                    return new long[0];
                }

                @Override
                public int getRetweetCount() {
                    return 0;
                }

                @Override
                public boolean isRetweetedByMe() {
                    return false;
                }

                @Override
                public long getCurrentUserRetweetId() {
                    return 0;
                }

                @Override
                public boolean isPossiblySensitive() {
                    return false;
                }

                @Override
                public String getLang() {
                    return null;
                }

                @Override
                public Scopes getScopes() {
                    return null;
                }

                @Override
                public String[] getWithheldInCountries() {
                    return new String[0];
                }

                @Override
                public long getQuotedStatusId() {
                    return 0;
                }

                @Override
                public Status getQuotedStatus() {
                    return null;
                }

                @Override
                public URLEntity getQuotedStatusPermalink() {
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

        @Override
        public long[] getContributors() {
            return null;
        }

        @Override
        public int getRetweetCount() {
            return retweetCount;
        }

        @Override
        public boolean isRetweetedByMe() {
            return false;
        }

        @Override
        public long getCurrentUserRetweetId() {
            return -1L;
        }

        @Override
        public boolean isPossiblySensitive() {
            return isPossiblySensitive;
        }

        @Override
        public String getLang() {
            return lang;
        }

        @Override
        public Scopes getScopes() {
            return null;
        }

        @Override
        public String[] getWithheldInCountries() {
            return null;
        }

        @Override
        public long getQuotedStatusId() {
            return quotedStatusId;
        }

        @Override
        public Status getQuotedStatus() {
            return null;
        }

        @Override
        public URLEntity getQuotedStatusPermalink() {
            return null;
        }

        @Override
        public int getDisplayTextRangeStart() {
            return -1;
        }

        @Override
        public int getDisplayTextRangeEnd() {
            return -1;
        }

        @Override
        public int compareTo(@NonNull Status o) {
            return 0;
        }

        @Override
        public UserMentionEntity[] getUserMentionEntities() {
            return userMentionEntities;
        }

        @Override
        public URLEntity[] getURLEntities() {
            return urlEntities;
        }

        @Override
        public HashtagEntity[] getHashtagEntities() {
            return hashtagEntities;
        }

        @Override
        public MediaEntity[] getMediaEntities() {
            return mediaEntities;
        }

        @Override
        public SymbolEntity[] getSymbolEntities() {
            return symbolEntities;
        }

        @Override
        public RateLimitStatus getRateLimitStatus() {
            return null;
        }

        public String getRemoteUrl(){
            return url;
        }

        public List<Emoji> getEmojis() {
            return emojis;
        }

        public String getSpoilerText() {
            return spoilerText;
        }

        public int getRepliesCount(){
            return repliesCount;
        }

        public long getRetweetedStatusId() {
            return retweetedStatusId;
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
            return obj != null && (this == obj || obj instanceof Status && ((Status) obj).getId() == this.id);
        }
    }

    public static class Result implements Sequence<Pair<CachedStatus, Integer>>, Closeable {

        private final Cursor c;
        private boolean hasNext;

        Result(Cursor c) {
            this.c = c;
            this.hasNext = c.moveToNext();
        }

        @Override
        public void close() {
            c.close();
        }

        public int size() {
            return c.getCount();
        }

        @NonNull
        @Override
        public Iterator<Pair<CachedStatus, Integer>> iterator() {
            return new Iterator<Pair<CachedStatus, Integer>>() {
                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Pair<CachedStatus, Integer> next() {
                    Pair<CachedStatus, Integer> pair = new Pair<>(
                            new CachedStatus(
                                    new Date(c.getLong(0)),
                                    c.getLong(1),
                                    c.getLong(2),
                                    c.getLong(3),
                                    c.getString(4),
                                    c.getString(5),
                                    c.getLong(6),
                                    c.getLong(7),
                                    c.getInt(8) != 0,
                                    c.getInt(9) != 0,
                                    c.getInt(10),
                                    c.getString(11),
                                    c.getInt(12),
                                    c.getInt(13) != 0,
                                    c.getString(14),
                                    restoreUserMentionEntities(
                                            splitComma(c.getString(15)),
                                            splitComma(c.getString(16)),
                                            splitComma(c.getString(17)),
                                            splitComma(c.getString(18)),
                                            splitComma(c.getString(19)),
                                            splitComma(c.getString(20))
                                    ),
                                    restoreURLEntities(
                                            splitComma(c.getString(21)),
                                            splitComma(c.getString(22)),
                                            splitComma(c.getString(23)),
                                            splitComma(c.getString(24)),
                                            splitComma(c.getString(25))
                                    ),
                                    restoreHashtagEntities(
                                            splitComma(c.getString(26)),
                                            splitComma(c.getString(27)),
                                            splitComma(c.getString(28))
                                    ),
                                    restoreMediaEntities(
                                            splitComma(c.getString(29)),
                                            splitComma(c.getString(30)),
                                            splitComma(c.getString(31)),
                                            splitComma(c.getString(32)),
                                            splitComma(c.getString(33)),
                                            splitComma(c.getString(34)),
                                            splitComma(c.getString(35)),

                                            parse(c.getString(36)),
                                            parse(c.getString(37)),
                                            parse(c.getString(38)),

                                            splitComma(c.getString(39)),
                                            splitComma(c.getString(40))
                                    ),
                                    restoreSymbolEntities(
                                            splitComma(c.getString(41)),
                                            splitComma(c.getString(42)),
                                            splitComma(c.getString(43))
                                    ),
                                    c.getLong(44),
                                    c.getString(45),
                                    restoreEmojis(
                                            splitComma(c.getString(46)),
                                            splitComma(c.getString(47))
                                    ),
                                    c.getString(48),
                                    c.getInt(49)
                            ),
                            c.getInt(50)
                    );
                    hasNext = c.moveToNext();
                    return pair;
                }
            };
        }
    }
}