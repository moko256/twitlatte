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
import com.github.moko256.latte.client.base.entity.ListEntry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by moko256 on 2019/07/11.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4::class)
class CachedListEntriesSQLiteOpenHelperTest {

    private lateinit var helper: CachedListEntriesSQLiteOpenHelper

    @Before
    fun prepare() {
        helper = CachedListEntriesSQLiteOpenHelper(ApplicationProvider.getApplicationContext(), "CachedListEntriesSQLiteOpenHelperTest.db")
    }

    @Test
    fun test() {
        helper.setListEntries(listOf(mockListEntry(0)))
        assertArrayEquals(
                arrayOf(mockListEntry(0)),
                helper.getListEntries().toTypedArray()
        )

        helper.setListEntries(listOf(mockListEntry(1), mockListEntry(2)))
        assertArrayEquals(
                arrayOf(mockListEntry(1), mockListEntry(2)),
                helper.getListEntries().toTypedArray()
        )
    }

    @After
    fun close() {
        helper.close()
    }

    private fun mockListEntry(id: Long) = ListEntry(id, id.toString(), id.toString(), false)
}