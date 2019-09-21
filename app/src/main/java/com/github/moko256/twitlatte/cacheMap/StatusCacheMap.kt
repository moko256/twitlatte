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

package com.github.moko256.twitlatte.cacheMap

import android.content.Context
import androidx.collection.ArraySet
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.Status
import com.github.moko256.latte.client.base.entity.StatusObject
import com.github.moko256.latte.client.base.entity.getId
import com.github.moko256.twitlatte.LIMIT_OF_SIZE_OF_OBJECT_CACHE
import com.github.moko256.twitlatte.collections.LruCache
import com.github.moko256.twitlatte.database.CachedStatusesSQLiteOpenHelper

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

class StatusCacheMap {

    private val cache = LruCache<Long, StatusObject>(LIMIT_OF_SIZE_OF_OBJECT_CACHE)
    private var diskCache: CachedStatusesSQLiteOpenHelper? = null

    fun prepare(context: Context, accessToken: AccessToken) {
        diskCache?.close()
        cache.clearIfNotEmpty()
        diskCache = CachedStatusesSQLiteOpenHelper(context, accessToken)
    }

    fun close() {
        diskCache?.close()
        diskCache = null
        cache.clearIfNotEmpty()
    }

    fun get(id: Long): StatusObject? {
        val memoryCache = cache.get(id)
        return memoryCache ?: try {
            val storageCache = diskCache?.getCachedStatus(id)
            if (storageCache != null) {
                cache.put(storageCache.getId(), storageCache)
            }
            storageCache
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    fun add(c: Status, incrementCount: Boolean) {
        cache.put(c.id, c)
        diskCache?.addCachedStatus(c, incrementCount)
    }

    fun addAll(c: Collection<StatusObject>, incrementCount: Boolean, vararg excludeIncrementIds: Long) {
        c.forEach {
            cache.put(it.getId(), it)
        }

        diskCache?.addCachedStatuses(c, incrementCount, *excludeIncrementIds)
    }

    fun delete(ids: List<Long>) {
        val list = ArraySet<Long>(ids.size * 6)
        for (id in ids) {
            if (id != -1L) {
                list.add(id)
            }
        }
        val use = diskCache?.getIdsInUse(list)

        if (use != null && use.isNotEmpty()) {
            list.addAll(use)
        }
        diskCache?.deleteCachedStatuses(list)
    }

}