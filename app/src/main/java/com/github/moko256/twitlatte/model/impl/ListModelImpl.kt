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

package com.github.moko256.twitlatte.model.impl

import com.github.moko256.twitlatte.GlobalApplication
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.*
import com.github.moko256.twitlatte.model.base.ListModel
import com.github.moko256.twitlatte.repository.server.ListServerRepository
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by moko256 on 2018/10/11.
 *
 * @author moko256
 */
class ListModelImpl(
        private val api: ListServerRepository<Post<Repeat?, Status, User?>>,
        private val database: CachedIdListSQLiteOpenHelper
): ListModel {

    private val nothingEvent = UpdateEvent(EventType.NOTHING, 0, 0)
    private val list = ArrayList<Long>()
    private val requests = CompositeDisposable()

    val updateObserver = PublishSubject.create<UpdateEvent>()
    val errorObserver = PublishSubject.create<Throwable>()

    init {
        val c = database.ids
        if (c.size > 0) {
            list.addAll(c)
        }
    }

    override fun getSeeingId(): Long {
        return database.seeingId
    }

    override fun updateSeeingPosition(id: Long) {
        database.seeingId = id
    }

    override fun refreshFirst() {
        requests.add(
                Completable.create { status ->
                    api.get().apply {
                        GlobalApplication.statusCache.addAll(this)

                        val ids = map { it.repeat?.id?:it.status.id }

                        list.addAll(ids)
                        database.addIds(ids)
                        updateObserver.onNext(UpdateEvent(EventType.ADD_FIRST, 0, ids.size))
                        status.onComplete()
                    }
                }.doOnError {
                    it.printStackTrace()
                    errorObserver.onNext(it)
                }.subscribe()
        )
    }

    override fun refreshOnTop() {
        val sinceId: Long = if (list.size >= 2) {
            if (list[1] == -1L) {
                list[0]
            } else {
                list[1]
            }
        } else {
            list[0]
        }

        val excludeId = list.takeIf { it.size >= 2 }?.firstOrNull()?:0

        requests.add(
                Completable.create { status ->
                    api.get(sinceId = sinceId).apply {
                        if (isNotEmpty()) {
                            GlobalApplication.statusCache.addAll(this, excludeId)

                            val ids = map { it.repeat?.id?:it.status.id }.toMutableList()

                            if (ids[ids.size - 1] == list[0]) {
                                ids.removeAt(ids.size - 1)
                            } else {
                                ids.add(-1L)
                            }

                            if (ids.size > 0) {
                                list.addAll(0, ids)
                                database.insertIds(0, ids)
                                updateObserver.onNext(UpdateEvent(EventType.ADD_TOP, 0, ids.size))
                            } else {
                                updateObserver.onNext(nothingEvent)
                            }
                        } else {
                            updateObserver.onNext(nothingEvent)
                        }

                        status.onComplete()
                    }
                }.doOnError {
                    it.printStackTrace()
                    errorObserver.onNext(it)
                }.subscribe()
        )
    }

    override fun loadOnBottom() {
        val bottomPos = list.size - 1

        if (list[bottomPos] == -1L) {
            database.deleteIds(listOf(-1L))
            list.removeAt(bottomPos)
            updateObserver.onNext(UpdateEvent(EventType.REMOVE, bottomPos, 1))
        }

        requests.add(
                Completable.create { status ->
                    api.get(maxId = list[list.size - 1] - 1L).apply {
                        if (isNotEmpty()) {
                            GlobalApplication.statusCache.addAll(this)

                            val ids = map { it.repeat?.id?:it.status.id }.toMutableList()

                            if (ids[ids.size - 1] == list[0]) {
                                ids.removeAt(ids.size - 1)
                            } else {
                                ids.add(-1L)
                            }

                            if (ids.size > 0) {
                                list.addAll(0, ids)
                                database.insertIds(list.size - size, ids)
                                updateObserver.onNext(UpdateEvent(
                                        EventType.ADD_BOTTOM,
                                        list.size - size,
                                        size
                                ))
                            } else {
                                updateObserver.onNext(nothingEvent)
                            }
                        } else {
                            updateObserver.onNext(nothingEvent)
                        }

                        status.onComplete()
                    }
                }.doOnError {
                    it.printStackTrace()
                    errorObserver.onNext(it)
                }.subscribe()
        )
    }

    override fun loadOnGap(position: Int) {
        val sinceId: Long = if (list.size > position + 2) {
            if (list[position + 2] == -1L) {
                list[position + 1]
            } else {
                list[position + 2]
            }
        } else {
            list[position + 1]
        }

        val excludeId = list.takeIf { it.size >= position + 2 }?.get(position + 1)?:0

        requests.add(
                Completable.create { status ->
                    api.get(sinceId = sinceId, maxId = list[position -1] - 1L).apply {
                        if (isNotEmpty()) {
                            GlobalApplication.statusCache.addAll(this, excludeId)

                            val ids = map { it.repeat?.id?:it.status.id }.toMutableList()

                            val noGap = ids[ids.size - 1] == list[position + 1]
                            if (noGap) {
                                ids.removeAt(ids.size - 1)
                                list.removeAt(position)
                                database.deleteIds(listOf(-1L))
                                updateObserver.onNext(UpdateEvent(EventType.REMOVE, position, 0))
                            } else {
                                updateObserver.onNext(UpdateEvent(EventType.UPDATE, position, 0))
                            }

                            list.addAll(position, ids)
                            database.insertIds(position, ids)

                            updateObserver.onNext(UpdateEvent(EventType.INSERT, position, ids.size))
                        } else {
                            list.removeAt(position)
                            database.deleteIds(listOf(-1L))
                            updateObserver.onNext(UpdateEvent(EventType.REMOVE, position, 0))
                        }

                        status.onComplete()
                    }
                }.doOnError {
                    it.printStackTrace()
                    errorObserver.onNext(it)
                }.subscribe()
        )    }

    override fun removeOldCache(position: Int) {
        if (list.size - position > GlobalApplication.statusCacheListLimit) {
            val subList = list.subList(position + GlobalApplication.statusCacheListLimit, list.size)
            database.deleteIds(subList)

            GlobalApplication.statusCache.delete(subList)
        }
    }

    override fun close() {
        requests.dispose()
    }
}