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

package com.github.moko256.twitlatte.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.moko256.latte.client.base.entity.Friendship
import com.github.moko256.latte.client.base.entity.User
import com.github.moko256.twitlatte.entity.Client
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by moko256 on 2019/03/13.
 *
 * @author moko256
 */

//TODO: Consider to use whether post/setValues method in MutableLiveData. It is different about using thread

class UserInfoViewModel : ViewModel() {

    private val disposable = CompositeDisposable()

    var userId: Long = -1
    var userName: String? = null
    lateinit var client: Client

    val user: MutableLiveData<User> = MutableLiveData()
    val friendship: MutableLiveData<Friendship> = MutableLiveData()
    val action: MutableLiveData<String> = MutableLiveData()
    val error: MutableLiveData<Throwable> = MutableLiveData()

    fun loadData(useCache: Boolean) {
        disposable.addAll(
                // get user
                Single.create<User> { subscriber ->
                    try {
                        val cachedUser = if (useCache && userId != -1L) {
                            client.userCache.get(userId)
                        } else {
                            null
                        }
                        if (cachedUser != null) {
                            subscriber.onSuccess(cachedUser)
                        } else {
                            val name = userName
                            val remoteUser = when {
                                userId != -1L -> client.apiClient.showUser(userId)
                                name != null -> client.apiClient.showUser(name)
                                else -> throw IllegalStateException("Unreachable")
                            }
                            client.userCache.add(remoteUser)
                            subscriber.onSuccess(remoteUser)
                        }
                    } catch (e: Throwable) {
                        subscriber.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { user.setValue(it) },
                                { error.setValue(it) }
                        ),

                // get friendship
                Single.create<Friendship> { subscriber ->
                    try {
                        subscriber.onSuccess(client.apiClient.getFriendship(userId))
                    } catch (e: Throwable) {
                        subscriber.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { friendship.setValue(it) },
                                { error.setValue(it) }
                        )
        )
    }

    private fun doAction(name: String, actionFunc: () -> Any) {
        disposable.add(
                Single.create<Any> {
                    try {
                        it.onSuccess(actionFunc())
                    } catch (e: Throwable) {
                        it.onError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    if (it is Friendship) {
                                        friendship.setValue(it)
                                    }
                                    action.setValue(name)
                                },
                                { error.setValue(it) }
                        )
        )
    }


    fun requestCreateFollow(didAction: String) {
        doAction(didAction) {
            client.apiClient.createFriendship(userId)
        }
    }

    fun requestDestroyFollow(didAction: String) {
        doAction(didAction) {
            client.apiClient.destroyFriendship(userId)
        }
    }

    fun requestCreateMute(didAction: String) {
        doAction(didAction) {
            client.apiClient.createMute(userId)
        }
    }

    fun requestDestroyMute(didAction: String) {
        doAction(didAction) {
            client.apiClient.destroyMute(userId)
        }
    }

    fun requestCreateBlock(didAction: String) {
        doAction(didAction) {
            client.apiClient.createBlock(userId)
        }
    }

    fun requestDestroyBlock(didAction: String) {
        doAction(didAction) {
            client.apiClient.destroyBlock(userId)
        }
    }

    fun requestDestroyF2F(didAction: String) {
        doAction(didAction) {
            client.apiClient.createBlock(userId)
            client.apiClient.destroyBlock(userId)
        }
    }

    fun requestAddToList(didAction: String, listId: Long) {
        doAction(didAction) {
            client.apiClient.addToLists(listId, userId)
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}