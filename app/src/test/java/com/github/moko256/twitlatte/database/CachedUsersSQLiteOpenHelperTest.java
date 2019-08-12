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

import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.testdata.TestEntityCreatorKt;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.github.moko256.twitlatte.testutils.EmptyAccessTokenKt.emptyAccessToken;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by moko256 on 2017/03/17.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class CachedUsersSQLiteOpenHelperTest {

    private CachedUsersSQLiteOpenHelper helper = new CachedUsersSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            emptyAccessToken
    );

    private static final long TEST_DUMMY_USER_ID_1 = 1L;
    private static final long TEST_DUMMY_USER_ID_2 = 2L;

    private static final String TEST_DUMMY_USER_NAME_0 = "0";
    private static final String TEST_DUMMY_USER_NAME_1 = "1";

    @Test
    public void test() {
        helper.getWritableDatabase().delete("CachedUsers", null, null);

        addCacheTest();
        updateCacheTest();
        removeCacheTest();
        addUsersTest();
        helper.close();
    }

    private void addCacheTest() {
        helper.addCachedUser(generateUser(TEST_DUMMY_USER_ID_1, TEST_DUMMY_USER_NAME_0));
        User addedStatusResult = helper.getCachedUser(TEST_DUMMY_USER_ID_1);

        assertEquals(addedStatusResult.getName(), TEST_DUMMY_USER_NAME_0);
    }

    private void updateCacheTest() {
        helper.addCachedUser(generateUser(TEST_DUMMY_USER_ID_1, TEST_DUMMY_USER_NAME_1));
        User updatedStatusResult = helper.getCachedUser(TEST_DUMMY_USER_ID_1);

        assertEquals(updatedStatusResult.getName(), TEST_DUMMY_USER_NAME_1);
    }

    private void removeCacheTest() {
        helper.deleteCachedUser(TEST_DUMMY_USER_ID_1);

        assertNull(helper.getCachedUser(TEST_DUMMY_USER_ID_1));
    }

    private void addUsersTest() {
        helper.addCachedUsers(Arrays.asList(
                generateUser(TEST_DUMMY_USER_ID_1, TEST_DUMMY_USER_NAME_0),
                generateUser(TEST_DUMMY_USER_ID_2, TEST_DUMMY_USER_NAME_1))
        );

        assertEquals(helper.getCachedUser(TEST_DUMMY_USER_ID_1).getName(), TEST_DUMMY_USER_NAME_0);
        assertEquals(helper.getCachedUser(TEST_DUMMY_USER_ID_2).getName(), TEST_DUMMY_USER_NAME_1);
    }

    private static User generateUser(final long testId, final String testName) {
        return TestEntityCreatorKt.testUser(testId, testName);
    }
}