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

    lateinit var readUserCacheRepo: () -> User?
    lateinit var writeUserCacheRepo: (User) -> Unit
    lateinit var remoteUserRepo: () -> User
    lateinit var remoteFriendshipRepo: () -> Friendship

    val user: MutableLiveData<User> = MutableLiveData()
    val friendship: MutableLiveData<Friendship> = MutableLiveData()
    val action: MutableLiveData<String> = MutableLiveData()
    val error: MutableLiveData<Throwable> = MutableLiveData()

    fun loadData() {
        disposable.addAll(
                // get user
                Single.create<User> { subscriber ->
                    try {
                        val cachedUser = readUserCacheRepo()
                        if (cachedUser != null) {
                            subscriber.onSuccess(cachedUser)
                        } else {
                            val remoteUser = remoteUserRepo()
                            writeUserCacheRepo(remoteUser)
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
                        subscriber.onSuccess(remoteFriendshipRepo())
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

    fun updateData() {
        disposable.addAll(
                // refresh user
                Single.create<User> { subscriber ->
                    try {
                        val remoteUser = remoteUserRepo()
                        writeUserCacheRepo(remoteUser)
                        subscriber.onSuccess(remoteUser)
                    } catch (e: Throwable) {
                        subscriber.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { user.setValue(it) },
                                { error.setValue(it) }
                        ),

                // refresh friendship
                Single.create<Friendship> { subscriber ->
                    try {
                        subscriber.onSuccess(remoteFriendshipRepo())
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

    fun doAction(actionFunc: () -> Any, name: String) {
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

    override fun onCleared() {
        disposable.clear()
    }
}