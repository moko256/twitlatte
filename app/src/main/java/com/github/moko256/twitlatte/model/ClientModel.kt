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

import android.content.Context
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.Friendship
import com.github.moko256.twitlatte.api.generateApiClient
import com.github.moko256.twitlatte.api.generateMediaUrlConverter
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap
import com.github.moko256.twitlatte.collections.LruCache
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.repository.AccountsRepository
import com.github.moko256.twitlatte.repository.KEY_ACCOUNT_KEY
import com.github.moko256.twitlatte.repository.PreferenceRepository

/**
 * Created by moko256 on 2020/01/21.
 *
 * @author moko256
 */
class ClientModel(
    private val context: Context,
    private val accountsRepository: AccountsRepository,
    private val preferenceRepository: PreferenceRepository
) {

    private val apiClientCache = LruCache<Int, ApiClient>(4)
    private val friendshipCache = LruCache<Long, Friendship>(20)

    var currentClient: Client? = null

    init {
        preferenceRepository
            .getString(KEY_ACCOUNT_KEY, "-1")
            .takeIf { it != "-1" }
            ?.let {
                accountsRepository.get(it)
            }
            ?.let {
                initCurrentClient(it)
            }
    }

    fun addAndSwitchCurrentClient(accessToken: AccessToken) {
        accountsRepository.add(accessToken)
        switchCurrentClient(accessToken)
    }

    fun switchCurrentClient(accessToken: AccessToken) {
        preferenceRepository.putString(
            KEY_ACCOUNT_KEY, accessToken.getKeyString()
        )
        initCurrentClient(accessToken)
    }

    private fun initCurrentClient(accessToken: AccessToken) {
        friendshipCache.clearIfNotEmpty()
        val statusCache = currentClient?.statusCache?.apply { close() }
        val userCache = currentClient?.userCache?.apply { close() }
        currentClient = Client(
            accessToken,
            createApiClientInstance(accessToken),
            generateMediaUrlConverter(accessToken.clientType),
            StatusCacheMap(statusCache, context, accessToken),
            UserCacheMap(userCache, context, accessToken),
            friendshipCache
        )
    }

    fun logoutAndSwitchCurrentClient(): Boolean {
        accountsRepository.delete(currentClient?.accessToken)
        val point: Int = accountsRepository.size() - 1
        return if (point != -1) {
            val accessToken = accountsRepository.getAccessTokens()[point]
            switchCurrentClient(accessToken)
            true
        } else {
            preferenceRepository.putString(KEY_ACCOUNT_KEY, "-1")
            clearCurrentClient()
            false
        }
    }

    private fun clearCurrentClient() {
        currentClient?.apply {
            statusCache.close()
            userCache.close()
        }
        currentClient = null
    }

    fun requireClient(key: String?): Client {
        return key?.let { accountsRepository.get(it) }
            ?.let {
                if (it == currentClient?.accessToken) {
                    currentClient
                } else {
                    Client(
                        it,
                        createApiClientInstance(it),
                        generateMediaUrlConverter(it.clientType),
                        StatusCacheMap(null, context, it),
                        UserCacheMap(null, context, it),
                        LruCache(20)
                    )
                }
            } ?: currentClient!!
    }

    fun createApiClientInstance(accessToken: AccessToken): ApiClient {
        val hash = accessToken.getHash()
        return apiClientCache.get(hash)
            ?: generateApiClient(accessToken).also {
                apiClientCache.put(hash, it)
            }
    }

    fun getAccessTokens() = accountsRepository.getAccessTokens()
    fun getAccessTokensByType(type: Int) = accountsRepository.getAccessTokensByType(type)

}