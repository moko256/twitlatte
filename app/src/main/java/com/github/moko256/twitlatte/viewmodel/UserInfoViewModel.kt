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
class UserInfoViewModel: ViewModel() {

    private val disposable = CompositeDisposable()

    lateinit var readCacheRepo: () -> User?
    lateinit var writeCacheRepo: (User) -> Unit
    lateinit var remoteRepo: () -> User

    val user: MutableLiveData<User> = MutableLiveData()
    val error: MutableLiveData<Throwable> = MutableLiveData()

    fun loadUser() {
        disposable.add(
                Single.create<User> { subscriber ->
                    try {
                        val cachedUser = readCacheRepo()
                        if (cachedUser != null) {
                            subscriber.onSuccess(cachedUser)
                        } else {
                            val remoteUser = remoteRepo()
                            writeCacheRepo(remoteUser)
                            subscriber.onSuccess(remoteUser)
                        }
                    } catch (e: Throwable) {
                        subscriber.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { user.postValue(it) },
                                { error.postValue(it) }
                        )
        )
    }

    fun updateUser() {
        disposable.add(
                Single.create<User> { subscriber ->
                    try {
                        val remoteUser = remoteRepo()
                        writeCacheRepo(remoteUser)
                        subscriber.onSuccess(remoteUser)
                    } catch (e: Throwable) {
                        subscriber.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { user.postValue(it) },
                                { error.postValue(it) }
                        )
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}