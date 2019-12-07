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

package com.github.moko256.twitlatte.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.moko256.twitlatte.database.utils.transaction
import com.github.moko256.twitlatte.testutils.emptyAccessToken
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by moko256 on 2019/12/07.
 *
 * @author moko256
 */

@RunWith(AndroidJUnit4::class)
class CachedIdListSQLiteOpenHelperTest {

    private lateinit var helper: CachedIdListSQLiteOpenHelper

    @Before
    fun setUp() {
        helper = CachedIdListSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            emptyAccessToken,
            "testIdsDatabase"
        )
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun getIds() {
        helper.transaction {
            execSQL("insert into IdList values(3)")
            execSQL("insert into IdList values(2)")
            execSQL("insert into IdList values(1)")
        }
        assertArrayEquals(longArrayOf(1, 2, 3), helper.getIds().toLongArray())
    }

    @Test
    fun insertIdsAtFirst() {
        val input = (1L..100L).toList()
        helper.insertIdsAtFirst(input)
        assertArrayEquals(input.toLongArray(), helper.getIds().toLongArray())
    }

    @Test
    fun insertIdsAtLast() {
        val firstInput = longArrayOf(1, 2, 3, 4, 5)
        val lastInput = longArrayOf(6, 7, 8, 9, 10)

        helper.insertIdsAtFirst(firstInput.toList())
        helper.insertIdsAtLast(lastInput.toList())

        assertArrayEquals(firstInput + lastInput, helper.getIds().toLongArray())
    }

    @Test
    fun insertIdsAt() {
        helper.insertIdsAtFirst(listOf(1, 2, 3, 4))
        helper.insertIdsAt(2, listOf(10, 11, 12))

        assertArrayEquals(longArrayOf(1, 2, 10, 11, 12, 3, 4), helper.getIds().toLongArray())
    }

    @Test
    fun removeAt() {
        helper.insertIdsAtFirst(listOf(1, 2, 3, 1, 4))
        helper.removeAt(3)

        assertArrayEquals(longArrayOf(1, 2, 3, 4), helper.getIds().toLongArray())
    }

    @Test
    fun removeFromLast() {
        helper.insertIdsAtFirst(listOf(1, 2, 3, 4, 4, 3, 2))
        helper.removeFromLast(3)

        assertArrayEquals(longArrayOf(1, 2, 3, 4), helper.getIds().toLongArray())
    }

    @Test
    fun getSeeingId() {
        helper.writableDatabase.execSQL("insert into SeeingId values(123)")
        assertEquals(123, helper.getSeeingId())
    }

    @Test
    fun setSeeingId() {
        helper.setSeeingId(123)
        assertEquals(123, helper.getSeeingId())

        helper.setSeeingId(333)
        assertEquals(333, helper.getSeeingId())
    }
}