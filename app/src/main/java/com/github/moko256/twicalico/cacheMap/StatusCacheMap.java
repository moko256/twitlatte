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

package com.github.moko256.twicalico.cacheMap;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.github.moko256.mastodon.MTStatus;
import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.database.CachedStatusesSQLiteOpenHelper;
import com.github.moko256.twicalico.entity.AccessToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import rx.Observable;
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
        diskCache = new CachedStatusesSQLiteOpenHelper(context, accessToken.getUserId());
    }

    public int size() {
        return cache.size();
    }

    public void add(@Nullable final Status status) {
        if (status != null) {
            GlobalApplication.userCache.add(status.getUser());
            if (status.isRetweet()) {
                add(status.getRetweetedStatus());
            }
            Status cacheStatus = new CachedStatus(status);
            cache.put(status.getId(), cacheStatus);
            diskCache.addCachedStatus(cacheStatus);
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

    public void addAll(Collection<? extends Status> c) {
        if (c.size() > 0) {
            Observable<Status> statusesObservable = Observable.unsafeCreate(subscriber -> {
                for (Status status : c) {
                    if (status != null) {
                        subscriber.onNext(status);
                        if (status.isRetweet()) {
                            subscriber.onNext(status.getRetweetedStatus());
                        }
                    }
                }
                subscriber.onCompleted();
            });

            GlobalApplication.userCache.addAll(
                    statusesObservable.map(Status::getUser).toList().toSingle().toBlocking().value()
            );

            Observable<Status> cachedStatusObservable = statusesObservable.map(CachedStatus::new);

            cachedStatusObservable.forEach(status -> cache.put(status.getId(), status));

            diskCache.addCachedStatuses(cachedStatusObservable.toList().toSingle().toBlocking().value());
        }
    }

    public void delete(List<Long> ids){
        List<Long> list = new ArrayList<>();
        for (Long id : ids) {
            if (id != null) {
                list.add(id);
            }
        }
        diskCache.deleteCachedStatuses(list);
    }

    @Keep
    public static class CachedStatus implements Status{

        /* Based on twitter4j.StatusJSONImpl */

        private final Date createdAt;
        private final long id;

        private final long userId;

        private final long retweetedStatusId;

        private final String text;
        private final String source;
        private final boolean isTruncated;
        private final long inReplyToStatusId;
        private final long inReplyToUserId;
        private final boolean isFavorited;
        private final boolean isRetweeted;
        private final int favoriteCount;
        private final String inReplyToScreenName;
        private final GeoLocation geoLocation;
        private final Place place;

        private final int retweetCount;
        private final boolean isPossiblySensitive;
        private final String lang;

        private final long[] contributorsIDs;

        private final UserMentionEntity[] userMentionEntities;
        private final URLEntity[] urlEntities;
        private final HashtagEntity[] hashtagEntities;
        private final MediaEntity[] mediaEntities;
        private final SymbolEntity[] symbolEntities;
        private final long currentUserRetweetId;
        private final Scopes scopes;
        private final String[] withheldInCountries;
        private final long quotedStatusId;
        private final Status quotedStatus;

        private final int displayTextRangeStart;
        private final int displayTextRangeEnd;

        private final String url;

        public CachedStatus(Status status){
            createdAt=new Date(status.getCreatedAt().getTime());
            id=status.getId();

            userId=status.getUser().getId();

            retweetedStatusId=status.isRetweet()?status.getRetweetedStatus().getId():-1;

            if (!isRetweet()) {
                text=status.getText();
                source=status.getSource();
                isTruncated=status.isTruncated();
                inReplyToStatusId=status.getInReplyToStatusId();
                inReplyToUserId=status.getInReplyToUserId();
                isFavorited=status.isFavorited();
                isRetweeted=status.isRetweeted();
                favoriteCount=status.getFavoriteCount();
                inReplyToScreenName=status.getInReplyToScreenName();
                geoLocation = status.getGeoLocation();
                place = status.getPlace();

                retweetCount=status.getRetweetCount();
                isPossiblySensitive=status.isPossiblySensitive();
                lang=status.getLang();

                contributorsIDs=status.getContributors();

                userMentionEntities=status.getUserMentionEntities();
                urlEntities=status.getURLEntities();
                hashtagEntities=status.getHashtagEntities();
                mediaEntities=status.getMediaEntities();
                symbolEntities=status.getSymbolEntities();
                currentUserRetweetId = status.getCurrentUserRetweetId();
                scopes=status.getScopes();
                withheldInCountries = status.getWithheldInCountries();
                quotedStatusId = status.getQuotedStatusId();
                quotedStatus = status.getQuotedStatus();

                displayTextRangeStart = status.getDisplayTextRangeStart();
                displayTextRangeEnd = status.getDisplayTextRangeEnd();
            } else {
                text=null;
                source=null;
                isTruncated=false;
                inReplyToStatusId=-1;
                inReplyToUserId=-1;
                isFavorited=false;
                isRetweeted=false;
                favoriteCount=-1;
                inReplyToScreenName=null;
                geoLocation = null;
                place = null;

                retweetCount=-1;
                isPossiblySensitive=false;
                lang=null;

                contributorsIDs=null;

                userMentionEntities=null;
                urlEntities=null;
                hashtagEntities=null;
                mediaEntities=null;
                symbolEntities=null;
                currentUserRetweetId = -1;
                scopes=null;
                withheldInCountries = null;
                quotedStatusId = -1;
                quotedStatus = null;

                displayTextRangeStart = -1;
                displayTextRangeEnd = -1;
            }

            url = (status instanceof MTStatus)
                    ? ((MTStatus) status).status.getUrl()
                    : "https://twitter.com/"
                            + status.getUser().getScreenName()
                            + "/status/"
                            + String.valueOf(status.getId());
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
            return isTruncated;
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
            return geoLocation;
        }

        @Override
        public Place getPlace() {
            return place;
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
            return contributorsIDs;
        }

        @Override
        public int getRetweetCount() {
            return retweetCount;
        }

        @Override
        public boolean isRetweetedByMe() {
            return currentUserRetweetId != -1L;
        }

        @Override
        public long getCurrentUserRetweetId() {
            return currentUserRetweetId;
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
            return scopes;
        }

        @Override
        public String[] getWithheldInCountries() {
            return withheldInCountries;
        }

        @Override
        public long getQuotedStatusId() {
            return quotedStatusId;
        }

        @Override
        public Status getQuotedStatus() {
            return quotedStatus;
        }

        @Override
        public int getDisplayTextRangeStart() {
            return displayTextRangeStart;
        }

        @Override
        public int getDisplayTextRangeEnd() {
            return displayTextRangeEnd;
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