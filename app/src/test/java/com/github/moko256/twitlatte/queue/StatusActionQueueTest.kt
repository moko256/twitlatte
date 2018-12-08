/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte.queue

import com.github.moko256.latte.client.base.entity.StatusAction
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by moko256 on 2018/10/21.
 *
 * @author moko256
 */
class StatusActionQueueTest {

    private lateinit var testTarget: StatusActionQueue

    @Before
    fun init() {
        testTarget = StatusActionQueue(
                delay = 5,
                unit = TimeUnit.MILLISECONDS,
                doImmediateFirst = false
        )
    }

    @Test
    fun add0() {
        var flag = false
        testTarget.add(1, StatusAction.FAVORITE) { flag = true }.blockingAwait()

        assert(flag)
    }

    @Test
    fun add1() {
        var flag1 = false
        var flag2 = false
        Completable.merge(
                listOf(
                        testTarget.add(2, StatusAction.FAVORITE) { flag1 = true},
                        testTarget.add(3, StatusAction.FAVORITE) { flag2 = true}
                )
        ).blockingAwait()

        assert(flag1)
        assert(flag2)
    }

    @Test
    fun add2() {
        var flag4 = true
        var flag5 = true
        var flag6 = true
        var flag7 = true
        var flag8 = false

        Completable.merge(
                listOf(
                        testTarget.add(4, StatusAction.FAVORITE) { flag4 = false},
                        testTarget.add(5, StatusAction.REPEAT) { flag5 = false},
                        testTarget.add(4, StatusAction.UNFAVORITE) { flag6 = false},
                        testTarget.add(5, StatusAction.UNREPEAT) { flag7 = false},
                        testTarget.add(4, StatusAction.REPEAT) { flag8 = true}
                )
        ).blockingAwait()

        assert(flag4 && flag5 && flag6 && flag7 && flag8)
    }

    @Test()
    fun add3() {
        var flag9 = true
        var flag10 = true
        var flag11 = true
        var flag12 = true

        Completable.merge(
                listOf(
                        testTarget.add(6, StatusAction.FAVORITE) { flag9= false},
                        testTarget.add(6, StatusAction.REPEAT) { flag10 = false},
                        testTarget.add(6, StatusAction.UNFAVORITE) { flag11 = false},
                        testTarget.add(6, StatusAction.UNREPEAT) { flag12 = false}
                )
        ).blockingAwait()

        assert(flag9 && flag10 && flag11 && flag12)
    }

    @Test
    fun add4() {
        repeat(21) {
            try {
                var flag = false
                testTarget
                        .add(it + 7L, StatusAction.FAVORITE) { _ -> flag = true }
                        .blockingAwait()
                assert(flag)
                assert(it < 27)
            } catch (e: Throwable) {
                assert(it == 27)
            }
        }
    }
}