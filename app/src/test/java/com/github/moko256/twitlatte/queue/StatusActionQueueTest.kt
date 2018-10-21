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

import com.github.moko256.twitlatte.entity.Post
import com.github.moko256.twitlatte.entity.StatusAction
import com.github.moko256.twitlatte.repository.server.base.StatusActionRepository
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
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
    private lateinit var mockRepo: MockRepo

    @Before
    fun init() {
        mockRepo = MockRepo()
        testTarget = StatusActionQueue(
                api = mockRepo,
                delay = 1,
                unit = TimeUnit.MILLISECONDS
        )
    }

    @Test
    fun add0() {
        testTarget.add(1, StatusAction.FAVORITE).blockingAwait()

        mockRepo.notify.onComplete()

        mockRepo.notify.blockingSubscribe { pair ->
            assert(pair.first == 1L)
            assert(pair.second == StatusAction.FAVORITE)
        }
    }

    @Test
    fun add1() {
        Completable.merge(
                listOf(
                        testTarget.add(2, StatusAction.FAVORITE),
                        testTarget.add(3, StatusAction.FAVORITE)
                )
        ).blockingAwait()

        mockRepo.notify.onComplete()

        mockRepo.notify.blockingIterable().forEachIndexed { index, pair ->
            assert(pair.first == index + 2L)
            assert(pair.second == StatusAction.FAVORITE)
        }
    }

    @Test
    fun add2() {
        Completable.merge(
                listOf(
                        testTarget.add(4, StatusAction.FAVORITE),
                        testTarget.add(5, StatusAction.REPEAT),
                        testTarget.add(4, StatusAction.UNFAVORITE),
                        testTarget.add(5, StatusAction.UNREPEAT),
                        testTarget.add(4, StatusAction.REPEAT)
                )
        ).blockingAwait()

        mockRepo.notify.onComplete()

        var i = 0

        mockRepo.notify.blockingSubscribe{ pair ->
            if (i == 0) {
                assert(pair.first == 4L)
                assert(pair.second == StatusAction.REPEAT)
            } else {
                assert(false)
            }
            i++
        }
    }

    @Test(expected = NoSuchElementException::class)
    fun add3() {
        Completable.merge(
                listOf(
                        testTarget.add(6, StatusAction.FAVORITE),
                        testTarget.add(6, StatusAction.REPEAT),
                        testTarget.add(6, StatusAction.UNFAVORITE),
                        testTarget.add(6, StatusAction.UNREPEAT)
                )
        ).blockingAwait()

        mockRepo.notify.onComplete()
        mockRepo.notify.blockingFirst()
        assert(false) //will not reach
    }

    @Test
    fun add4() {
        repeat(21) {
            try {
                testTarget
                        .add(it + 7L, StatusAction.FAVORITE)
                        .blockingAwait()
                assert(it < 27)
            } catch (e: Throwable) {
                assert(it == 27)
            }
        }
    }

    private class MockRepo: StatusActionRepository {
        val notify = PublishSubject.create<Pair<Long, StatusAction>>()

        override fun createFavorite(targetStatusId: Long): Post {
            println(targetStatusId.toString() + ": create favorite")
            notify.onNext(targetStatusId to StatusAction.FAVORITE)
            return Post(id = targetStatusId)
        }

        override fun removeFavorite(targetStatusId: Long): Post {
            println(targetStatusId.toString() + ": destroy favorite")
            notify.onNext(targetStatusId to StatusAction.UNFAVORITE)
            return Post(id = targetStatusId)
        }

        override fun createRepeat(targetStatusId: Long): Post {
            println(targetStatusId.toString() + ": create repeat")
            notify.onNext(targetStatusId to StatusAction.REPEAT)
            return Post(id = targetStatusId)
        }

        override fun removeRepeat(targetStatusId: Long): Post {
            println(targetStatusId.toString() + ": destroy repeat")
            notify.onNext(targetStatusId to StatusAction.UNREPEAT)
            return Post(id = targetStatusId)
        }
    }
}