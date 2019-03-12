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
import androidx.lifecycle.MutableLiveData
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.latte.client.base.entity.StatusAction
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.model.base.StatusActionModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * Created by moko256 on 2018/10/20.
 *
 * @author moko256
 */
class StatusActionModelImpl(
        private val apiClient: ApiClient,
        private val database: StatusCacheMap
): StatusActionModel {

    private val actionObservable = MutableLiveData<StatusAction>()
    private val statusObservable = MutableLiveData<Long>()
    private val errorObservable = MutableLiveData<Throwable>()

    override fun getDidActionObservable() = actionObservable
    override fun getStatusObservable() = statusObservable
    override fun getErrorObservable() = errorObservable

    override fun updateStatus(targetStatusId: Long) {
        doAction(targetStatusId) {
            apiClient.showPost(targetStatusId)
        }
    }

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

    override fun sendVote(targetStatusId: Long, targetPollId: Long, options: List<Int>) {
        doAction(targetStatusId) {
            apiClient.votePoll(targetPollId, options)
            apiClient.showPost(targetStatusId)
        }
    }

    @SuppressLint("CheckResult")
    private fun doAction(targetId: Long, actionType: StatusAction, action: () -> Post) {
        Completable
                .create {
                    try {
                        database.add(action(), false)
                        it.onComplete()
                    } catch (e: Throwable) {
                        it.tryOnError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            statusObservable.postValue(targetId)
                            actionObservable.postValue(actionType)
                        },
                        {
                            statusObservable.postValue(targetId)
                            errorObservable.postValue(it)
                        }
                )
    }

    @SuppressLint("CheckResult")
    private fun doAction(targetId: Long, action: () -> Post) {
        Completable
                .create {
                    try {
                        database.add(action(), false)
                        it.onComplete()
                    } catch (e: Throwable) {
                        it.tryOnError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            statusObservable.postValue(targetId)
                        },
                        {
                            statusObservable.postValue(targetId)
                            errorObservable.postValue(it)
                        }
                )
    }
}