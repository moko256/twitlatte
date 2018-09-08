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

package com.github.moko256.mastodon;

import com.sys1yagi.mastodon4j.api.entity.Status;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 17/10/03.
 *
 * @author moko256
 */

public class MTStatus implements twitter4j.Status{

    public Status status;

    MTStatus(Status status){
        this.status = status;
    }

    @Override
    public Date getCreatedAt() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public long getId() {
        return status.getId();
    }

    @Override
    public String getText() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getDisplayTextRangeStart() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getDisplayTextRangeEnd() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getSource() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isTruncated() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public long getInReplyToStatusId() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public long getInReplyToUserId() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getInReplyToScreenName() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public GeoLocation getGeoLocation() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public Place getPlace() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isFavorited() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isRetweeted() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getFavoriteCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public User getUser() {
        return new MTUser(status.getAccount());
    }

    @Override
    public boolean isRetweet() {
        return status.getReblog() != null;
    }

    @Override
    public twitter4j.Status getRetweetedStatus() {
        return isRetweet()?new MTStatus(status.getReblog()):null;
    }

    @Override
    public long[] getContributors() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getRetweetCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isRetweetedByMe() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public long getCurrentUserRetweetId() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isPossiblySensitive() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getLang() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public Scopes getScopes() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String[] getWithheldInCountries() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public long getQuotedStatusId() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public twitter4j.Status getQuotedStatus() {
        return null;
    }

    @Override
    public URLEntity getQuotedStatusPermalink() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int compareTo(@NotNull twitter4j.Status status) {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public URLEntity[] getURLEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public SymbolEntity[] getSymbolEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getAccessLevel() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (this == obj || obj instanceof Status && ((Status) obj).getId() == this.getId());
    }

}
