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

package com.github.moko256.twitlatte.model.impl

import com.github.moko256.latte.client.base.entity.Paging
import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.twitlatte.LIMIT_OF_SIZE_OF_STATUSES_LIST
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.entity.EventType
import com.github.moko256.twitlatte.entity.UpdateEvent
import com.github.moko256.twitlatte.model.base.ListModel
import com.github.moko256.twitlatte.repository.server.base.ListServerRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by moko256 on 2018/10/11.
 *
 * @author moko256
 */
class ListModelImpl(
        private val api: ListServerRepository<Post>,
        private val client: Client,
        private val database: CachedIdListSQLiteOpenHelper
) : ListModel {

    private val nothingEvent = UpdateEvent(EventType.NOTHING, 0, 0)
    private val list = ArrayList<Long>()
    private val requests = CompositeDisposable()

    private var seeingId = -1L

    private val updateObserver = PublishSubject.create<UpdateEvent>()
    private val errorObserver = PublishSubject.create<Throwable>()

    init {
        val c = database.getIds()
        if (c.isNotEmpty()) {
            list.addAll(c)
        }
    }

    override fun getIdsList(): List<Long> {
        return list
    }

    override fun getListEventObservable(): Observable<UpdateEvent> {
        return updateObserver
    }

    override fun getErrorEventObservable(): Observable<Throwable> {
        return errorObserver
    }

    override fun getSeeingPosition(): Int {
        if (seeingId == -1L) {
            seeingId = database.getSeeingId()
        }
        return list.indexOf(seeingId)
    }

    override fun updateSeeingPosition(position: Int) {
        val id = getIdsList()[position]
        if (id != seeingId) {
            seeingId = id
            database.setSeeingId(id)
        }
    }

    override fun refreshFirst() {
        requests.add(
                Completable.create { status ->
                    try {
                        api.request(Paging(count = 10))
                                .apply {
                                    client.postCache.addAll(this)
                                }
                                .map { it.id }
                                .let {
                                    list.addAll(it)
                                    database.insertIdsAtFirst(it)
                                    updateObserver.onNext(UpdateEvent(EventType.ADD_FIRST, 0, it.size))
                                }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        errorObserver.onNext(e)
                    }
                    status.onComplete()
                }.subscribeOn(Schedulers.io())
                        .subscribe()
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

        val excludeId = list.takeIf { it.size >= 2 }?.firstOrNull() ?: 0

        requests.add(
                Completable.create { status ->
                    try {
                        api.request(Paging(
                                sinceId = sinceId,
                                count = client.statusLimit
                        )).apply {
                            if (isNotEmpty()) {
                                client.postCache.addAll(this, excludeId)

                                val ids = map { it.id }.toMutableList()

                                if (ids[ids.size - 1] == list[0]) {
                                    ids.removeAt(ids.size - 1)
                                } else {
                                    ids.add(-1L)
                                }

                                if (ids.size > 0) {
                                    list.addAll(0, ids)
                                    database.insertIdsAtFirst(ids)
                                    updateObserver.onNext(UpdateEvent(EventType.ADD_TOP, 0, ids.size))
                                } else {
                                    updateObserver.onNext(nothingEvent)
                                }
                            } else {
                                updateObserver.onNext(nothingEvent)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        errorObserver.onNext(e)
                    }
                    status.onComplete()
                }.subscribeOn(Schedulers.io())
                        .subscribe()
        )
    }

    override fun loadOnBottom() {
        val bottomPos = list.size - 1

        if (list[bottomPos] == -1L) {
            database.removeAt(bottomPos)
            list.removeAt(bottomPos)
            updateObserver.onNext(UpdateEvent(EventType.REMOVE, bottomPos, 1))
        }

        requests.add(
                Completable.create { status ->
                    try {
                        api.request(Paging(
                                maxId = list[list.size - 1] - 1L,
                                count = client.statusLimit
                        )).apply {
                            if (isNotEmpty()) {
                                client.postCache.addAll(this)

                                val ids = map { it.id }
                                val sizeBeforeAdded = list.size
                                list.addAll(ids)
                                database.insertIdsAtLast(ids)
                                updateObserver.onNext(UpdateEvent(
                                        EventType.ADD_BOTTOM,
                                        sizeBeforeAdded,
                                        size
                                ))
                            } else {
                                updateObserver.onNext(nothingEvent)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        errorObserver.onNext(e)
                    }
                    status.onComplete()
                }.subscribeOn(Schedulers.io())
                        .subscribe()
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

        val excludeId = list.takeIf { it.size >= position + 2 }?.get(position + 1) ?: 0

        requests.add(
                Completable.create { status ->
                    try {
                        api.request(Paging(
                                sinceId = sinceId,
                                maxId = list[position - 1] - 1L,
                                count = client.statusLimit
                        )).apply {
                            if (isNotEmpty()) {
                                client.postCache.addAll(this, excludeId)

                                val ids = map { it.id }.toMutableList()

                                val noGap = ids[ids.size - 1] == list[position + 1]
                                if (noGap) {
                                    ids.removeAt(ids.size - 1)
                                    list.removeAt(position)
                                    database.removeAt(position)
                                    updateObserver.onNext(UpdateEvent(EventType.REMOVE, position, 1))
                                } else {
                                    updateObserver.onNext(UpdateEvent(EventType.UPDATE, position, 1))
                                }

                                list.addAll(position, ids)
                                database.insertIdsAt(position, ids)

                                updateObserver.onNext(UpdateEvent(EventType.INSERT, position, ids.size))
                            } else {
                                list.removeAt(position)
                                database.removeAt(position)
                                updateObserver.onNext(UpdateEvent(EventType.REMOVE, position, 1))
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        errorObserver.onNext(e)
                    }
                    status.onComplete()
                }.subscribeOn(Schedulers.io())
                        .subscribe()
        )
    }

    override fun removeOldCache(position: Int) {
        val parentSize = list.size
        if (parentSize - position > LIMIT_OF_SIZE_OF_STATUSES_LIST * 11 / 10) {
            val targetFirst = position + LIMIT_OF_SIZE_OF_STATUSES_LIST

            val targetToRemove = list.subList(targetFirst, parentSize)

            requests.add(
                    Completable.create {
                        try {
                            database.removeFromLast(targetToRemove.size)
                            client.statusCache.delete(targetToRemove)
                            targetToRemove.clear() //Clear this range from parent's list

                            updateObserver.onNext(UpdateEvent(EventType.REMOVE, targetFirst, parentSize))
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            errorObserver.onNext(e)
                        }
                        it.onComplete()
                    }.subscribeOn(Schedulers.io())
                            .subscribe()
            )
        }
    }

    override fun close() {
        requests.dispose()
        database.close()
    }
}