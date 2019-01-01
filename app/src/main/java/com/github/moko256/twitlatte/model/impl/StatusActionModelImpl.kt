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

import android.annotation.SuppressLint
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.latte.client.base.entity.StatusAction
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.model.base.StatusActionModel
import com.github.moko256.twitlatte.queue.StatusActionQueue
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by moko256 on 2018/10/20.
 *
 * @author moko256
 */
class StatusActionModelImpl(
        private val apiClient: ApiClient,
        private val queue: StatusActionQueue,
        private val database: StatusCacheMap
): StatusActionModel {

    private val updateObservable = PublishSubject.create<Pair<Long, StatusAction>>()
    private val errorObservable = PublishSubject.create<Pair<Long, Throwable>>()

    override fun getUpdateObservable() = updateObservable

    override fun getErrorObservable() = errorObservable

    override fun createFavorite(targetStatusId: Long) {
        doAction(targetStatusId, StatusAction.FAVORITE) {
            apiClient.createFavorite(targetStatusId)
        }
    }

    override fun removeFavorite(targetStatusId: Long) {
        doAction(targetStatusId, StatusAction.UNFAVORITE) {
            apiClient.destroyFavorite(targetStatusId)
        }
    }

    override fun createRepeat(targetStatusId: Long) {
        doAction(targetStatusId, StatusAction.REPEAT) {
            apiClient.createRepeat(targetStatusId)
        }
    }

    override fun removeRepeat(targetStatusId: Long) {
        doAction(targetStatusId, StatusAction.UNREPEAT) {
            apiClient.destroyRepeat(targetStatusId)
        }
    }

    @SuppressLint("CheckResult")
    private fun doAction(targetId: Long, actionType: StatusAction, action: () -> Post) {
        queue
                .add(targetId, actionType) {
                    database.add(action(), false)
                }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            updateObservable.onNext(targetId to actionType)
                        },
                        {
                            errorObservable.onNext(targetId to it)
                        }
                )
    }

}