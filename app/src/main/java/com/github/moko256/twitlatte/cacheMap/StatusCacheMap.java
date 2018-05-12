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

package com.github.moko256.twitlatte.cacheMap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.github.moko256.mastodon.MTStatus;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.database.CachedStatusesSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.Emoji;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class StatusCacheMap {

    private LruCache<Long, Status> cache =new LruCache<>(GlobalApplication.statusCacheListLimit / 4);
    private CachedStatusesSQLiteOpenHelper diskCache;

    public void prepare(Context context, AccessToken accessToken){
        if (diskCache != null){
            diskCache.close();
        }
        if (cache.size() > 0){
            cache.evictAll();
        }
        diskCache = new CachedStatusesSQLiteOpenHelper(context, accessToken);
    }

    public int size() {
        return cache.size();
    }

    public void add(@Nullable final Status status, boolean incrementCount) {
        if (status != null && status.getRetweetedStatus() != null && status.getQuotedStatus() != null) {
            GlobalApplication.userCache.add(status.getUser());
            CachedStatus cacheStatus = new CachedStatus(status);
            cache.put(status.getId(), cacheStatus);
            diskCache.addCachedStatus(cacheStatus, incrementCount);
        } else {
            addAll(Collections.singletonList(status), incrementCount);
        }
    }

    @Nullable
    public Status get(Long id){
        Status memoryCache = cache.get(id);
        if (memoryCache == null){
            Status storageCache = diskCache.getCachedStatus(id);
            if (storageCache != null) {
                cache.put(storageCache.getId(), storageCache);
            }
            return  storageCache;
        } else {
            return memoryCache;
        }
    }

    public void addAll(Collection<? extends Status> c, long... excludeIncrementIds) {
        addAll(c, true, excludeIncrementIds);
    }

    private void addAll(Collection<? extends Status> c, boolean incrementCount, long... excludeIncrementIds) {
        if (c.size() > 0) {
            ArrayList<Status> statuses = new ArrayList<>(c.size() * 3);
            ArrayList<Status> repeats = new ArrayList<>(c.size());
            ArrayList<Status> quotes = new ArrayList<>(c.size());

            ArrayList<User> users = new ArrayList<>(c.size() * 3);

            for (Status status : c) {
                if (status != null) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }

                    if (status.getRetweetedStatus() != null) {
                        repeats.add(status.getRetweetedStatus());
                        if (status.getRetweetedStatus().getQuotedStatus() != null){
                            quotes.add(status.getRetweetedStatus().getQuotedStatus());
                        }
                    } else if (status.getQuotedStatus() != null){
                        quotes.add(status.getQuotedStatus());
                    }
                }
            }

            for (Status status : repeats) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }
                }
            }

            for (Status status : quotes) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }
                }
            }

            GlobalApplication.userCache.addAll(users);

            ArrayList<CachedStatus> cachedStatuses = new ArrayList<>(statuses.size());
            for (Status status : statuses){
                CachedStatus cachedStatus = new CachedStatus(status);
                cache.put(status.getId(), cachedStatus);
                cachedStatuses.add(cachedStatus);
            }

            diskCache.addCachedStatuses(cachedStatuses, incrementCount, excludeIncrementIds);
        }
    }

    public void delete(List<Long> ids){
        List<Long> list = new ArrayList<>(ids.size() * 6);
        for (Long id : ids) {
            if (id != null) {
                list.add(id);
            }
        }
        List<Long> use = diskCache.getIdsInUse(list);

        HashSet<Long> remove = new HashSet<>();
        remove.addAll(list);
        remove.addAll(use);
        diskCache.deleteCachedStatuses(remove);
    }

    public static class CachedStatus implements Status{
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

        public CachedStatus(Status status){
            createdAt=new Date(status.getCreatedAt().getTime());
            id=status.getId();

            userId=status.getUser().getId();

            retweetedStatusId=status.isRetweet()?status.getRetweetedStatus().getId():-1;

            if (!isRetweet()) {
                text=status.getText();
                source=status.getSource();
                //isTruncated=status.isTruncated();
                inReplyToStatusId=status.getInReplyToStatusId();
                inReplyToUserId=status.getInReplyToUserId();
                isFavorited=status.isFavorited();
                isRetweeted=status.isRetweeted();
                favoriteCount=status.getFavoriteCount();
                inReplyToScreenName=status.getInReplyToScreenName();
                //geoLocation = status.getGeoLocation();
                //place = status.getPlace();

                retweetCount=status.getRetweetCount();
                isPossiblySensitive=status.isPossiblySensitive();
                lang=status.getLang();

                //contributorsIDs=status.getContributors();

                userMentionEntities=status.getUserMentionEntities();
                urlEntities=status.getURLEntities();
                hashtagEntities=status.getHashtagEntities();
                mediaEntities=status.getMediaEntities();
                symbolEntities=status.getSymbolEntities();
                //currentUserRetweetId = status.getCurrentUserRetweetId();
                //scopes=status.getScopes();
                //withheldInCountries = status.getWithheldInCountries();
                quotedStatusId = status.getQuotedStatusId();

                //displayTextRangeStart = status.getDisplayTextRangeStart();
                //displayTextRangeEnd = status.getDisplayTextRangeEnd();
            } else {
                text=null;
                source=null;
                //isTruncated=false;
                inReplyToStatusId=-1;
                inReplyToUserId=-1;
                isFavorited=false;
                isRetweeted=false;
                favoriteCount=-1;
                inReplyToScreenName=null;
                //geoLocation = null;
                //place = null;

                retweetCount=-1;
                isPossiblySensitive=false;
                lang=null;

                //contributorsIDs=null;

                userMentionEntities=null;
                urlEntities=null;
                hashtagEntities=null;
                mediaEntities=null;
                symbolEntities=null;
                //currentUserRetweetId = -1;
                //scopes=null;
                //withheldInCountries = null;
                quotedStatusId = -1;

                //displayTextRangeStart = -1;
                //displayTextRangeEnd = -1;
            }

            if (status instanceof MTStatus) {
                url = ((MTStatus) status).status.getUrl();
                List<com.sys1yagi.mastodon4j.api.entity.Emoji> oldEmojis = ((MTStatus) status).status.getEmojis();

                emojis = new ArrayList<>(oldEmojis.size());
                for (com.sys1yagi.mastodon4j.api.entity.Emoji emoji : oldEmojis) {
                    emojis.add(new Emoji(emoji.getShortcode(), emoji.getUrl()));
                }
            } else {
                url = "https://twitter.com/"
                        + status.getUser().getScreenName()
                        + "/status/"
                        + String.valueOf(status.getId());
                emojis = null;
            }
        }

        public CachedStatus(Date createdAt, long id, long userId, long retweetedStatusId, String text, String source, long inReplyToStatusId, long inReplyToUserId, boolean isFavorited, boolean isRetweeted, int favoriteCount, String inReplyToScreenName, int retweetCount, boolean isPossiblySensitive, String lang, UserMentionEntity[] userMentionEntities, URLEntity[] urlEntities, HashtagEntity[] hashtagEntities, MediaEntity[] mediaEntities, SymbolEntity[] symbolEntities, long quotedStatusId, String url, List<Emoji> emojis) {
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
            return GlobalApplication.userCache.get(userId);
        }

        @Override
        public boolean isRetweet() {
            return retweetedStatusId!=-1;
        }

        @Override
        public Status getRetweetedStatus() {
            return GlobalApplication.statusCache.get(retweetedStatusId);
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
            return GlobalApplication.statusCache.get(quotedStatusId);
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
}