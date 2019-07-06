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

package com.github.moko256.twitlatte.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.moko256.latte.client.base.entity.Repeat
import com.github.moko256.latte.client.base.entity.Status
import com.github.moko256.latte.client.base.entity.StatusObject
import com.github.moko256.latte.client.base.entity.User
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap
import com.github.moko256.twitlatte.entity.Client
import java.io.Closeable

/**
 * Created by moko256 on 2019/07/05.
 *
 * @author moko256
 */
class PostLiveDataObserver(
        private val owner: LifecycleOwner,
        private val repeatView: (Repeat?) -> Unit,
        private val repeatedUserView: (User?) -> Unit,
        private val statusView: (Status?) -> Unit,
        private val userView: (User?) -> Unit,
        private val quotedRepeatingStatusView: (Status?) -> Unit,
        private val quotedRepeatingUserView: (User?) -> Unit
) : Closeable {

    private val usingLiveDataList = ArrayList<LiveData<out Any>>(6)

    fun setStatusId(id: Long, client: Client) {
        setStatusId(id, client.statusCache, client.userCache)
    }

    fun setStatusId(id: Long, statusCache: StatusCacheMap, userCache: UserCacheMap) {
        statusCache.observe(id) {
            when (it) {
                null -> {
                    repeatView(null)
                    repeatedUserView(null)
                    statusView(null)
                    userView(null)
                    quotedRepeatingStatusView(null)
                    quotedRepeatingUserView(null)
                }
                is Repeat -> {
                    repeatView(it)
                    userCache.observe(it.userId, repeatedUserView)
                    statusCache.observe(it.repeatedStatusId) { status ->
                        statusView(status as Status?)
                        setStatusInternal(status, statusCache, userCache)
                    }
                }
                is Status -> {
                    repeatView(null)
                    repeatedUserView(null)
                    statusView(it)
                    setStatusInternal(it, statusCache, userCache)
                }
            }
        }
    }

    private fun setStatusInternal(status: Status?, statusCache: StatusCacheMap, userCache: UserCacheMap) {
        userCache.observe(status?.userId, userView)
        statusCache.observe(status?.quotedStatusId) {
            quotedRepeatingStatusView(it as Status?)
            userCache.observe(it?.userId, quotedRepeatingUserView)
        }
    }

    private fun StatusCacheMap.observe(id: Long?, callback: (StatusObject?) -> Unit) {
        if (id != null && id > 0) {
            getAsLiveData(id)
                    .also { usingLiveDataList.add(it) }
                    .observe(owner, Observer(callback))
        } else {
            callback(null)
        }
    }

    private fun UserCacheMap.observe(id: Long?, callback: (User?) -> Unit) {
        if (id != null && id > 0) {
            getAsLiveData(id)
                    .also { usingLiveDataList.add(it) }
                    .observe(owner, Observer(callback))
        } else {
            callback(null)
        }
    }

    override fun close() {
        usingLiveDataList.forEach { it.removeObservers(owner) }
    }
}