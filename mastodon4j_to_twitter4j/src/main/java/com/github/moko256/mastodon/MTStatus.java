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

package com.github.moko256.mastodon;

import com.sys1yagi.mastodon4j.api.entity.Status;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

class MTStatus implements twitter4j.Status{

    Status status;

    MTStatus(Status status){
        this.status = status;
    }

    @Override
    public Date getCreatedAt() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(status.getCreatedAt().replace("X", ""));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getId() {
        return status.getId();
    }

    @Override
    public String getText() {
        return status.getContent();
    }

    @Override
    public int getDisplayTextRangeStart() {
        return 0;
    }

    @Override
    public int getDisplayTextRangeEnd() {
        return getText().length();
    }

    @Override
    public String getSource() {
        if (status.getApplication() != null) {
            return "<a href='" + status.getApplication().getWebsite() + "'>" + status.getApplication().getName() + "</a>";
        } else {
            return "unknown";
        }
    }

    @Override
    public boolean isTruncated() {
        return false;
    }

    @Override
    public long getInReplyToStatusId() {
        Long id = status.getInReplyToId();
        return id == null?-1:id;
    }

    @Override
    public long getInReplyToUserId() {
        Long id = status.getInReplyToAccountId();
        return id == null?-1:id;
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
        return status.isFavourited();
    }

    @Override
    public boolean isRetweeted() {
        return status.isReblogged();
    }

    @Override
    public int getFavoriteCount() {
        return status.getFavouritesCount();
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
        return new MTStatus(status.getReblog());
    }

    @Override
    public long[] getContributors() {
        return null;
    }

    @Override
    public int getRetweetCount() {
        return status.getReblogsCount();
    }

    @Override
    public boolean isRetweetedByMe() {
        return false;
    }

    @Override
    public long getCurrentUserRetweetId() {
        return -1;
    }

    @Override
    public boolean isPossiblySensitive() {
        return status.isSensitive();
    }

    @Override
    public String getLang() {
        return null;//status.getLanguage();
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
        return -1;
    }

    @Override
    public twitter4j.Status getQuotedStatus() {
        return null;
    }

    @Override
    public int compareTo(@NotNull twitter4j.Status status) {
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
}
