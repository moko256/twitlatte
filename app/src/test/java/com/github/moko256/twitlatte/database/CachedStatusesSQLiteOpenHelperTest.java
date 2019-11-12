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

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.moko256.latte.client.base.entity.Status;
import com.github.moko256.latte.client.base.entity.StatusObject;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import kotlin.Pair;
import kotlin.collections.ArraysKt;
import kotlin.collections.MapsKt;

import static com.github.moko256.twitlatte.testutils.EmptyAccessTokenKt.emptyAccessToken;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        addStatusTestWithIncrement();
        getIdInUseTest();
        helper.close();
    }

    private void addCacheTest() {
        helper.addCachedStatus(generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0), false);
        StatusObject addedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((Status) addedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_0);
    }

    private void updateCacheTest() {
        helper.addCachedStatus(generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_1), false);
        StatusObject updatedStatusResult = helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1);

        assertEquals(((Status) updatedStatusResult).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private void removeCacheTest() {
        helper.deleteCachedStatuses(Collections.singletonList(TEST_DUMMY_STATUS_ID_1));

        assertNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1));
    }

    private void addStatusesTest() {
        helper.addCachedStatuses(Arrays.asList(
                generateStatus(TEST_DUMMY_STATUS_ID_1, TEST_DUMMY_STATUS_TEXT_0),
                generateStatus(TEST_DUMMY_STATUS_ID_2, TEST_DUMMY_STATUS_TEXT_1)
                ), false
        );

        assertEquals(((Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1)).getText(), TEST_DUMMY_STATUS_TEXT_0);
        assertEquals(((Status) helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2)).getText(), TEST_DUMMY_STATUS_TEXT_1);
    }

    private void addStatusTestWithIncrement() {
        helper.addCachedStatus(generateStatus(3, "3"), true);
        helper.addCachedStatus(generateStatus(4, "3"), true);
        helper.addCachedStatus(generateStatus(4, "3"), true);

        assertNotNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1));
        assertNotNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2));
        assertNotNull(helper.getCachedStatus(3));
        assertNotNull(helper.getCachedStatus(4));

        helper.deleteCachedStatuses(Arrays.asList(3L, 4L));

        assertNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_1));
        assertNull(helper.getCachedStatus(TEST_DUMMY_STATUS_ID_2));
        assertNull(helper.getCachedStatus(3));
        assertNotNull(helper.getCachedStatus(4));

        helper.deleteCachedStatuses(Collections.singletonList(4L));

        assertNull(helper.getCachedStatus(4));
    }

    private static Map<Long, Pair<Long, Long>> data = MapsKt.mapOf(
            new Pair<>(1L, new Pair<>(-1L, -1L)),
            new Pair<>(2L, new Pair<>(-1L, 1L)),
            new Pair<>(3L, new Pair<>(2L, -1L)),
            new Pair<>(4L, new Pair<>(-1L, 3L)),
            new Pair<>(5L, new Pair<>(4L, -1L)),
            new Pair<>(6L, new Pair<>(-1L, 6L)),
            new Pair<>(7L, new Pair<>(-1L, 3L)),
            new Pair<>(8L, new Pair<>(-1L, -1L))
    );

    private void getIdInUseTest() {
        SQLiteDatabase database = helper.getReadableDatabase();
        assertEquals(0, DatabaseUtils.queryNumEntries(database, "CachedStatuses"));
        SQLiteStatement statement = database.compileStatement(
                "insert into CachedStatuses(id,repeatedStatusId,quotedStatusId) values(?,?,?)"
        );
        data.forEach((t, u) -> {
            statement.bindLong(1, t);
            statement.bindLong(2, u.getFirst());
            statement.bindLong(3, u.getSecond());
            statement.execute();
        });
        database.close();

        Collection<Long> result = helper.getIdsInUse(Arrays.asList(5L, 6L, 7L, 9L));
        assertArrayEquals(new long[]{1L, 2L, 3L, 4L}, ArraysKt.toLongArray(result.toArray(new Long[0])));
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