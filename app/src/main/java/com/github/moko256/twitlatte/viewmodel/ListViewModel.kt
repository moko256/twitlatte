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

package com.github.moko256.twitlatte.viewmodel

import androidx.lifecycle.ViewModel
import com.github.moko256.twitlatte.GlobalApplication
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.repository.server.ListServerRepository
import either.Either
import either.Left
import either.Right
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import twitter4j.Status
import twitter4j.TwitterException

/**
 * Created by moko256 on 2018/07/13.
 *
 * @author moko256
 */
class ListViewModel: ViewModel() {
    var initilized: Boolean = false

    private val nothingEvent = UpdateEvent(EventType.NOTHING, 0, 0)

    val list = ArrayList<Long>()

    private val requests = CompositeDisposable()
    val listObserver = PublishSubject.create<Either<UpdateEvent, Throwable>>()

    lateinit var statusIdsDatabase: CachedIdListSQLiteOpenHelper
    lateinit var serverRepository: ListServerRepository<Status>

    fun start() {
        val c = statusIdsDatabase.ids
        if (c.size > 0) {
            list.addAll(c)
        }
        initilized = true
    }

    override fun onCleared() {
        requests.dispose()
    }

    fun removeOldCache(position: Int) {
        if (list.size - position > GlobalApplication.statusCacheListLimit) {
            val subList = list.subList(position + GlobalApplication.statusCacheListLimit, list.size)
            statusIdsDatabase.deleteIds(subList)

            GlobalApplication.statusCache.delete(subList)
        }
    }

    fun getSeeingId(): Long {
        return statusIdsDatabase.seeingId
    }

    fun saveSeeingPosition(id: Long) {
        statusIdsDatabase.seeingId = id
    }

    fun refreshFirst(){
        requests.add(
                getSingle()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                { result ->
                                    val ids = result
                                            .asSequence()
                                            .map { it.id }
                                            .toList()
                                    list.addAll(ids)
                                    statusIdsDatabase.addIds(ids)
                                    listObserver.onNext(Left(UpdateEvent(EventType.ADD_FIRST, 0, ids.size)))
                                },
                                {
                                    it.printStackTrace()
                                    listObserver.onNext(Right(it))
                                }
                        )
        )
    }

    fun refreshOnTop() {
        val sinceId: Long = if (list.size >= 2) {
            if (list[1] == -1L) {
                list[0]
            } else {
                list[1]
            }
        } else {
            list[0]
        }

        requests.add(
                getSingle(sinceId = sinceId,
                        excludeIds = *if (list.size >= 2) longArrayOf(list[0]) else LongArray(0))
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                { result ->
                                    if (result.isNotEmpty()) {
                                        val ids = result
                                                .asSequence()
                                                .map { it.id }
                                                .toMutableList()
                                        if (ids[ids.size - 1] == list[0]) {
                                            ids.removeAt(ids.size - 1)
                                        } else {
                                            ids.add(-1L)
                                        }

                                        if (ids.size > 0) {
                                            list.addAll(0, ids)
                                            statusIdsDatabase.insertIds(0, ids)
                                            listObserver.onNext(Left(UpdateEvent(EventType.ADD_TOP, 0, ids.size)))
                                        } else {
                                            listObserver.onNext(Left(nothingEvent))
                                        }
                                    } else {
                                        listObserver.onNext(Left(nothingEvent))
                                    }
                                },
                                {
                                    it.printStackTrace()
                                    listObserver.onNext(Right(it))
                                }
                        )
        )
    }

    fun loadOnBottom() {
        val bottomPos = list.size - 1

        if (list[bottomPos] == -1L) {
            statusIdsDatabase.deleteIds(listOf(-1L))
            list.removeAt(bottomPos)
            listObserver.onNext(Left(UpdateEvent(EventType.REMOVE_ONLY_GAP, bottomPos, 1)))
        }

        requests.add(
                getSingle(maxId = list[list.size - 1] - 1L)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                { result ->
                                    val size = result.size
                                    if (size > 0) {
                                        val ids = result
                                                .asSequence()
                                                .map { it.id }
                                                .toList()
                                        list.addAll(ids)
                                        statusIdsDatabase.insertIds(list.size - size, ids)
                                        listObserver.onNext(Left(UpdateEvent(
                                                EventType.ADD_BOTTOM,
                                                list.size - size,
                                                size
                                        )))
                                    } else {
                                        listObserver.onNext(Left(nothingEvent))
                                    }
                                },
                                {
                                    it.printStackTrace()
                                    listObserver.onNext(Right(it))
                                }
                        )
        )
    }

    fun loadOnGap(position: Int) {
        val sinceId: Long = if (list.size > position + 2) {
            if (list[position + 2] == -1L) {
                list[position + 1]
            } else {
                list[position + 2]
            }
        } else {
            list[position + 1]
        }

        requests.add(
                getSingle(sinceId = sinceId, maxId = list[position -1] - 1L, excludeIds = *longArrayOf(if (list.size >= position + 2) list[position + 1] else 0))
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                { result ->
                                    if (result.isNotEmpty()) {
                                        val ids = result
                                                .asSequence()
                                                .map { it.id }
                                                .toMutableList()
                                        val noGap = ids[ids.size - 1] == list[position + 1]
                                        if (noGap) {
                                            ids.removeAt(ids.size - 1)
                                            list.removeAt(position)
                                            statusIdsDatabase.deleteIds(listOf(-1L))
                                        }

                                        list.addAll(position, ids)
                                        statusIdsDatabase.insertIds(position, ids)

                                        listObserver.onNext(Left(UpdateEvent(EventType.INSERT_AT_GAP, position, ids.size)))

                                    } else {
                                        list.removeAt(position)
                                        statusIdsDatabase.deleteIds(listOf(-1L))
                                        listObserver.onNext(Left(UpdateEvent(EventType.REMOVE_ONLY_GAP, position, 0)))
                                    }
                                },
                                {
                                    it.printStackTrace()
                                    listObserver.onNext(Right(it))
                                }
                        )
        )
    }

    private fun getSingle(sinceId: Long? = null, maxId: Long? = null, vararg excludeIds: Long) = Single.create<List<Status>> {
        try {
            val statuses = serverRepository.get(sinceId = sinceId, maxId = maxId, limit = GlobalApplication.statusLimit)
            if (statuses.isNotEmpty()) {
                GlobalApplication.statusCache.addAll(statuses, *excludeIds)
            }
            it.onSuccess(statuses)
        } catch (e: TwitterException) {
            it.tryOnError(e)
        }
    }

    enum class EventType {
        NOTHING,
        ADD_FIRST,
        ADD_TOP,
        ADD_BOTTOM,
        INSERT_AT_GAP,
        REMOVE_ONLY_GAP
    }

    data class UpdateEvent(
            val type: EventType,
            val position: Int,
            val size: Int
    )
}