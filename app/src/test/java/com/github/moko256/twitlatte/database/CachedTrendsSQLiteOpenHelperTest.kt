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
import com.github.moko256.latte.client.base.entity.Trend
import com.github.moko256.twitlatte.testutils.emptyAccessToken
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by moko256 on 2020/01/11.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4::class)
class CachedTrendsSQLiteOpenHelperTest {

    private val input0 = arrayOf("0", "1", "2")
    private val input1 = arrayOf("3", "4")

    lateinit var helper: CachedTrendsSQLiteOpenHelper

    @Before
    fun setUp() {
        helper = CachedTrendsSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            emptyAccessToken
        )
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun setTrends() {
        val input = createTestTrends(input0)
        helper.trends = input
        assertArrayEquals(input.toTypedArray(), helper.trends.toTypedArray())
    }

    @Test
    fun updateTrends() {
        helper.trends = createTestTrends(input0)
        val input = createTestTrends(input1)
        helper.trends = input
        assertArrayEquals(input.toTypedArray(), helper.trends.toTypedArray())

    }


    private fun createTestTrends(names: Array<String>) = names.map { Trend(it, -1) }

}