package com.github.moko256.twicalico;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

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

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */

public class CachedStatusesSQLiteOpenHelperTest extends ApplicationTestCase<Application> {

    private static final long TEST_DUMMY_STATUS_ID = 1L;
    private static final String TEST_DUMMY_STATUS_TEXT_0 = "0";
    private static final String TEST_DUMMY_STATUS_TEXT_1 = "1";

    public CachedStatusesSQLiteOpenHelperTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        CachedStatusesSQLiteOpenHelper helper = new CachedStatusesSQLiteOpenHelper(new RenamingDelegatingContext(getContext(), "test_"));

        helper.addCachedStatus(new TestStatus(TEST_DUMMY_STATUS_ID, TEST_DUMMY_STATUS_TEXT_0));
        Status addedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID);

        assertEquals(addedStatusResult.getText(), TEST_DUMMY_STATUS_TEXT_0);


        helper.addCachedStatus(new TestStatus(TEST_DUMMY_STATUS_ID, TEST_DUMMY_STATUS_TEXT_1));
        Status updatedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID);

        assertEquals(updatedStatusResult.getText(), TEST_DUMMY_STATUS_TEXT_1);


        helper.deleteCachedStatus(TEST_DUMMY_STATUS_ID);

        assertEquals(helper.getCachedStatus(TEST_DUMMY_STATUS_ID), null);

        helper.close();
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
            return null;
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
        public int compareTo(Status o) {
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