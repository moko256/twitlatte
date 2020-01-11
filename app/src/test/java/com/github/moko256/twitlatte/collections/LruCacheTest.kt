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

package com.github.moko256.twitlatte.collections

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by moko256 on 2019/06/23.
 *
 * @author moko256
 */
class LruCacheTest {

    @Test
    fun test() {
        val lruCache = LruCache<Int, String>(2)
        testMap(lruCache)
        lruCache.clear()
        testMap(lruCache)
        lruCache.clearIfNotEmpty()
        testMap(lruCache)
    }


    private fun testMap(lruCache: LruCache<Int, String>) {
        lruCache.put(0, "zero")
        assertEquals(lruCache.get(0), "zero")
        assertEquals(lruCache.values().single(), "zero")

        lruCache.put(1, "one")
        assertArrayEquals(lruCache.values().toTypedArray(), arrayOf("zero", "one"))

        lruCache.put(2, "two")
        assertArrayEquals(lruCache.values().toTypedArray(), arrayOf("one", "two"))

        assertEquals(lruCache.get(1), "one")

        lruCache.put(3, "three")
        assertArrayEquals(lruCache.values().toTypedArray(), arrayOf("one", "three"))

        lruCache.put(1, "one")
        assertArrayEquals(lruCache.values().toTypedArray(), arrayOf("three", "one"))

        lruCache.put(3, "THREE")
        assertArrayEquals(lruCache.values().toTypedArray(), arrayOf("one", "THREE"))
    }
}