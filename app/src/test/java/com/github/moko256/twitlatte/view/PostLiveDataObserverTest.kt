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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

/**
 * Created by moko256 on 2019/07/06.
 *
 * @author moko256
 */
class PostLiveDataObserverTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val owner = object : LifecycleOwner {
        override fun getLifecycle() = LifecycleRegistry(this)
    }

    var setStatusIdTestStateCount = 0
    @Test
    fun setStatusId() {
        owner.lifecycle.currentState = Lifecycle.State.INITIALIZED
        owner.lifecycle.currentState = Lifecycle.State.CREATED
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        synchronized(setStatusIdTestStateCount) {
            PostLiveDataObserver(
                    owner,
                    { assertNull(it) },
                    { assertNull(it) },
                    { assertNull(it) },
                    { assertNull(it) },
                    { assertNull(it) },
                    { assertNull(it) }
            ).setStatusId(-1, StatusCacheMap(), UserCacheMap())
        }
    }

    @Test
    fun close() {
        owner.lifecycle.currentState = Lifecycle.State.INITIALIZED
        owner.lifecycle.currentState = Lifecycle.State.CREATED
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        PostLiveDataObserver(
                owner,
                { error("Unreachable") },
                { error("Unreachable") },
                { error("Unreachable") },
                { error("Unreachable") },
                { error("Unreachable") },
                { error("Unreachable") }
        ).setStatusId(200, StatusCacheMap(), UserCacheMap())
        owner.lifecycle.currentState = Lifecycle.State.DESTROYED
        TODO("Not working")
    }
}