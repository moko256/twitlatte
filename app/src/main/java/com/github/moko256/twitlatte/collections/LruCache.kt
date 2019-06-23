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

import org.jetbrains.annotations.TestOnly

/**
 * Created by moko256 on 2019/06/23.
 *
 * @author moko256
 */
class LruCache<K, V>(capacity: Int) {
    private val map = LimitedLinkedHashMap<K, V>(capacity)

    @TestOnly
    fun valueIterable() = map.values.asIterable()

    fun get(key: K): V? {
        synchronized(this) {
            return map.get(key)
        }
    }

    fun put(key: K, value: V) {
        synchronized(this) {
            map.put(key, value)
        }
    }

    fun size(): Int {
        return map.size
    }

    fun clear() {
        synchronized(this) {
            map.clear()
        }
    }

    fun clearIfNotEmpty() {
        if (map.isNotEmpty()) {
            synchronized(this) {
                map.clear()
            }
        }
    }
}

private class LimitedLinkedHashMap<K, V>(private val capacity: Int)
    : LinkedHashMap<K, V>(capacity + 2, 1f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > capacity
    }
}