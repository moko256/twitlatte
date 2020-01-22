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

package com.github.moko256.twitlatte.model

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.User
import com.github.moko256.twitlatte.database.CachedUsersSQLiteOpenHelper
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by moko256 on 2020/01/21.
 *
 * @author moko256
 */
class AccountsModel(
    private val context: Context,
    private val clientModel: ClientModel
) {
    private val usersCache = HashMap<String, User>()

    data class Accounts(
        val accessTokens: List<AccessToken>,
        val users: List<User?>,
        val currentAccessToken: AccessToken?
    )

    val accounts = MutableLiveData<Accounts>()
    val currentUser = MutableLiveData<User>()

    @SuppressLint("CheckResult")
    fun updateAccounts(networkOnly: Boolean = false) {
        val accessTokens = clientModel.getAccessTokens()
        if (accessTokens.isEmpty()) {
            accounts.value = Accounts(emptyList(), emptyList(), null)
        } else if (networkOnly) {
            accounts.value = Accounts(
                accessTokens,
                accessTokens.map { usersCache[it.getKeyString()] },
                clientModel.currentClient?.accessToken
            )
        } else {
            Completable.create {
                val currentAccessToken = clientModel.currentClient?.accessToken

                val users = ArrayList<User?>(accessTokens.size)

                for (accessToken in accessTokens) {
                    val keyString = accessToken.getKeyString()
                    var user = usersCache[keyString]
                    val currentKey = currentAccessToken?.getKeyString()

                    if (user == null) {
                        val id = accessToken.userId
                        val userHelper = CachedUsersSQLiteOpenHelper(context, accessToken)
                        try {
                            user = userHelper.getCachedUser(id)
                            if (user == null) {
                                user =
                                    clientModel.createApiClientInstance(accessToken)
                                        .verifyCredentials()
                                userHelper.addCachedUser(user)
                            }
                            usersCache[keyString] = user
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        } finally {
                            userHelper.close()
                        }
                    }
                    if (keyString == currentKey) {
                        currentUser.postValue(user)
                    }
                    users.add(user)
                }
                accounts.postValue(Accounts(accessTokens, users, currentAccessToken))
                it.onComplete()
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    fun switchAccount(accessToken: AccessToken) {
        clientModel.switchCurrentClient(accessToken)
        val cachedUser = usersCache[accessToken.getKeyString()]
        if (cachedUser != null) {
            currentUser.value = cachedUser
        } else {
            updateAccounts(true)
        }
    }

    fun logout(): Boolean {
        val result = clientModel.logoutAndSwitchCurrentClient()
        updateAccounts()
        return result
    }
}