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

import com.sys1yagi.mastodon4j.api.entity.Account;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * Created by moko256 on 2017/10/03.
 *
 * @author moko256
 */

public class MTUser implements User {

    public final Account account;

    MTUser(Account account){
        this.account = account;
    }

    @Override
    public long getId() {
        return account.getId();
    }

    @Override
    public String getName() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getEmail() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getScreenName() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getLocation() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getDescription() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isContributorsEnabled() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String get400x400ProfileImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getBiggerProfileImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getMiniProfileImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getOriginalProfileImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileImageURLHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String get400x400ProfileImageURLHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getBiggerProfileImageURLHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getMiniProfileImageURLHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getOriginalProfileImageURLHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isDefaultProfileImage() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isProtected() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getFollowersCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public Status getStatus() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBackgroundColor() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileTextColor() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileLinkColor() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileSidebarFillColor() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileSidebarBorderColor() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isProfileUseBackgroundImage() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isDefaultProfile() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isShowAllInlineMedia() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getFriendsCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public Date getCreatedAt() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getFavouritesCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getUtcOffset() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getTimeZone() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBackgroundImageURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBackgroundImageUrlHttps() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerRetinaURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerIPadURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerIPadRetinaURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerMobileURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBannerMobileRetinaURL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBanner300x100URL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBanner600x200URL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getProfileBanner1500x500URL() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isProfileBackgroundTiled() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String getLang() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getStatusesCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isGeoEnabled() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isVerified() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isTranslator() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int getListedCount() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public boolean isFollowRequestSent() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public URLEntity[] getDescriptionURLEntities() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public URLEntity getURLEntity() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public String[] getWithheldInCountries() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public int compareTo(@NotNull User user) {
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
}
