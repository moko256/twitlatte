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
import io.reactivex.Completable
import io.reactivex.CompletableOnSubscribe
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

    val user = MutableLiveData<User>()
    val friendship = MutableLiveData<Friendship>()
    val action = MutableLiveData<String>()
    val error = MutableLiveData<Throwable>()

    fun loadData(useCache: Boolean) {
        execute(CompletableOnSubscribe {
            loadDataInternal(useCache)
            it.onComplete()
        })
    }

    private fun loadDataInternal(useCache: Boolean) {
        try {
            val cachedUser = if (useCache && userId != -1L) {
                client.userCache.get(userId)
            } else {
                null
            }
            if (cachedUser != null) {
                user.postValue(cachedUser)
            } else {
                val name = userName
                val remoteUser = when {
                    userId != -1L -> client.apiClient.showUser(userId)
                    name != null -> client.apiClient.showUser(name)
                    else -> error("Unreachable")
                }
                userId = remoteUser.id
                client.userCache.add(remoteUser)
                user.postValue(remoteUser)
            }


            if (client.accessToken.userId != userId) {
                val cachedFriendship = if (useCache && userId != -1L) {
                    client.friendshipCache.get(userId)
                } else {
                    null
                }
                if (cachedFriendship != null) {
                    friendship.postValue(cachedFriendship)
                } else {
                    val remoteFriendship = client.apiClient.getFriendship(userId)
                    client.friendshipCache.put(userId, remoteFriendship)
                    friendship.postValue(remoteFriendship)
                }
            }
        } catch (e: Throwable) {
            error.postValue(e)
        }
    }

    private inline fun doAction(name: String, crossinline actionFunc: () -> Any) {
        execute(CompletableOnSubscribe {
            try {
                if (userId == -1L) {
                    loadDataInternal(true)
                }
                val result = actionFunc()
                if (result is Friendship) {
                    friendship.postValue(result)
                }
                action.postValue(name)
            } catch (e: Throwable) {
                e.printStackTrace()
                error.postValue(e)
            }
            it.onComplete()
        })
    }

    private fun execute(s: CompletableOnSubscribe) {
        disposable.add(
                Completable.create(s).subscribeOn(Schedulers.io()).subscribe()
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