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

import com.github.moko256.twitlatte.converter.StatusConverterKt;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.StatusObject;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
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

import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class CachedStatusesSQLiteOpenHelperTest {

    private CachedStatusesSQLiteOpenHelper helper = new CachedStatusesSQLiteOpenHelper(
            InstrumentationRegistry.getTargetContext(),
            new AccessToken(
                    -2,
                    "example.com",
                    0,
                    "test",
                    "",
                    ""
            )
    );

    private static final long TEST_DUMMY_STATUS_ID_1 = 1L;
    private static final long TEST_DUMMY_STATUS_ID_2 = 2L;

    private static final String TEST_DUMMY_STATUS_TEXT_0 = "0";
    private static final String TEST_DUMMY_STATUS_TEXT_1 = "1";

    @Test
    public void test() {
        helper.getWritableDatabase().delete("CachedStatuses", null, null);

        addCacheTest();
        updateCacheTest();
        removeCacheTest();
        addStatusesTest();
        helper.close();
    }

    private void addCacheTest(){
        helper.addCachedStatus(StatusConverterKt.convertToCommonStatus(new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0)), false);
        StatusObject addedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((com.github.moko256.twitlatte.entity.Status) addedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_0);
    }

    private void updateCacheTest(){
        helper.addCachedStatus(StatusConverterKt.convertToCommonStatus(new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_1)), false);
        StatusObject updatedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((com.github.moko256.twitlatte.entity.Status) updatedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private void removeCacheTest(){
        helper.deleteCachedStatuses(Collections.singletonList(TEST_DUMMY_STATUS_ID_1));

        assertEquals(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1), null);
    }

    private void addStatusesTest(){
        helper.addCachedStatuses(Arrays.asList(
                StatusConverterKt.convertToCommonStatus(new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0)),
                StatusConverterKt.convertToCommonStatus(new TestStatus(TEST_DUMMY_STATUS_ID_2, TEST_DUMMY_STATUS_TEXT_1))), false
        );

        assertEquals(((com.github.moko256.twitlatte.entity.Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1)).getText(), TEST_DUMMY_STATUS_TEXT_0);
        assertEquals(((com.github.moko256.twitlatte.entity.Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2)).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private static class TestStatus implements Status {

        private final long id;
        private final String text;

        TestStatus(final long testId, final String testText){
            id = testId;
            text = testText;
        }

        @Override
        public Date getCreatedAt() {
            return new Date();
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
        public int getDisplayTextRangeStart() {
            return 0;
        }

        @Override
        public int getDisplayTextRangeEnd() {
            return 0;
        }

        @Override
        public String getSource() {
            return "test";
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
            return new User() {
                @Override
                public long getId() {
                    return 0;
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
    }
}