/*
 * Copyright 2017 The twicalico authors
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

package com.github.moko256.twicalico.database;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.moko256.twicalico.array.ArrayUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

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

    private CachedStatusesSQLiteOpenHelper helper = new CachedStatusesSQLiteOpenHelper(InstrumentationRegistry.getTargetContext(), 0);

    private static final long TEST_DUMMY_STATUS_ID_1 = 1L;
    private static final long TEST_DUMMY_STATUS_ID_2 = 2L;

    private static final String TEST_DUMMY_STATUS_TEXT_0 = "0";
    private static final String TEST_DUMMY_STATUS_TEXT_1 = "1";

    @Test
    public void test() throws Exception {
        try {
            helper.getWritableDatabase().execSQL("delete from CachedStatuses;");
        } catch (Exception e) {
            //Do nothing
        }

        addCacheTest();
        updateCacheTest();
        removeCacheTest();
        addStatusesTest();
        helper.close();
    }

    private void addCacheTest(){
        helper.addCachedStatus(new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0));
        Status addedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(addedStatusResult.getText(), TEST_DUMMY_STATUS_TEXT_0);
    }

    private void updateCacheTest(){
        helper.addCachedStatus(new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_1));
        Status updatedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(updatedStatusResult.getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private void removeCacheTest(){
        helper.deleteCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1), null);
    }

    private void addStatusesTest(){
        helper.addCachedStatuses(ArrayUtils.convertToList(
                new TestStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0),
                new TestStatus(TEST_DUMMY_STATUS_ID_2, TEST_DUMMY_STATUS_TEXT_1))
        );

        assertEquals(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1).getText(), TEST_DUMMY_STATUS_TEXT_0);
        assertEquals(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private static class TestStatus implements Status{

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