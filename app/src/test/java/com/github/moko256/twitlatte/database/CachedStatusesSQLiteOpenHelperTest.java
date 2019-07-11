/*
 * Copyright 2015-2019 The twitlatte authors
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

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.moko256.latte.client.base.entity.Status;
import com.github.moko256.latte.client.base.entity.StatusObject;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static com.github.moko256.twitlatte.testutils.EmptyAccessTokenKt.emptyAccessToken;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class CachedStatusesSQLiteOpenHelperTest {

    private CachedStatusesSQLiteOpenHelper helper = new CachedStatusesSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            emptyAccessToken
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
        helper.addCachedStatus(generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0), false);
        StatusObject addedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((Status) addedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_0);
    }

    private void updateCacheTest(){
        helper.addCachedStatus(generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_1), false);
        StatusObject updatedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((Status) updatedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private void removeCacheTest(){
        helper.deleteCachedStatuses(Collections.singletonList(TEST_DUMMY_STATUS_ID_1));

        assertNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1));
    }

    private void addStatusesTest(){
        helper.addCachedStatuses(Arrays.asList(
                generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0),
                generateStatus(TEST_DUMMY_STATUS_ID_2, TEST_DUMMY_STATUS_TEXT_1)
                ), false
        );

        assertEquals(((Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1)).getText(), TEST_DUMMY_STATUS_TEXT_0);
        assertEquals(((Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2)).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private static Status generateStatus(final long testId, final String testText) {
        return new Status(
                new Date(),
                testId,
                0,
                testText,
                null,
                null,
                0,
                0,
                "",
                false,
                false,
                0,
                0,
                0,
                false,
                "",
                null,
                null,
                null,
                0,
                "",
                "",
                null,
                "",
                null,
                null
        );
    }
}