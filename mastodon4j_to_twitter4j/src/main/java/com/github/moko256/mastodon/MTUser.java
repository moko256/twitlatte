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
    public Account account;

    MTUser(Account account){
        this.account = account;
    }

    @Override
    public long getId() {
        return account.getId();
    }

    @Override
    public String getName() {
        String name = account.getDisplayName();
        if (name.equals("")){
            name = account.getUserName();
        }
        return name;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getScreenName() {
        return account.getAcct();
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public String getDescription() {
        return account.getNote();
    }

    @Override
    public boolean isContributorsEnabled() {
        return false;
    }

    @Override
    public String getProfileImageURL() {
        return account.getAvatar();
    }

    @Override
    public String get400x400ProfileImageURL() {
        return account.getAvatar();
    }

    @Override
    public String getBiggerProfileImageURL() {
        return account.getAvatar();
    }

    @Override
    public String getMiniProfileImageURL() {
        return account.getAvatar();
    }

    @Override
    public String getOriginalProfileImageURL() {
        return account.getAvatar();
    }

    @Override
    public String getProfileImageURLHttps() {
        return account.getAvatar();
    }

    @Override
    public String get400x400ProfileImageURLHttps() {
        return account.getAvatar();
    }

    @Override
    public String getBiggerProfileImageURLHttps() {
        return account.getAvatar();
    }

    @Override
    public String getMiniProfileImageURLHttps() {
        return account.getAvatar();
    }

    @Override
    public String getOriginalProfileImageURLHttps() {
        return account.getAvatar();
    }

    @Override
    public boolean isDefaultProfileImage() {
        return true;
    }

    @Override
    public String getURL() {
        return null;
    }

    @Override
    public boolean isProtected() {
        return account.isLocked();
    }

    @Override
    public int getFollowersCount() {
        return account.getFollowersCount();
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
        return true;
    }

    @Override
    public boolean isDefaultProfile() {
        return true;
    }

    @Override
    public boolean isShowAllInlineMedia() {
        return false;
    }

    @Override
    public int getFriendsCount() {
        return account.getFollowingCount();
    }

    @Override
    public Date getCreatedAt() {
        return MastodonTwitterImpl.parseDate(account.getCreatedAt());
    }

    @Override
    public int getFavouritesCount() {
        return -1;
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
        return account.getHeader();
    }

    @Override
    public String getProfileBannerRetinaURL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBannerIPadURL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBannerIPadRetinaURL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBannerMobileURL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBannerMobileRetinaURL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBanner300x100URL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBanner600x200URL() {
        return account.getHeader();
    }

    @Override
    public String getProfileBanner1500x500URL() {
        return account.getHeader();
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
        return account.getStatusesCount();
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
    public int compareTo(@NotNull User user) {
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
}
