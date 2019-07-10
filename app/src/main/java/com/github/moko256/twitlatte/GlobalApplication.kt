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

package com.github.moko256.twitlatte

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.GlideInitializer
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.twitter.okhttp.replaceOkHttpClient
import com.github.moko256.twitlatte.api.generateApiClient
import com.github.moko256.twitlatte.api.generateMediaUrlConverter
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap
import com.github.moko256.twitlatte.collections.LruCache
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.glide.GlideModule
import com.github.moko256.twitlatte.model.AccountsModel
import com.github.moko256.twitlatte.net.appOkHttpClientInstance
import com.github.moko256.twitlatte.repository.KEY_ACCOUNT_KEY
import com.github.moko256.twitlatte.repository.KEY_NIGHT_MODE
import com.github.moko256.twitlatte.repository.PreferenceRepository
import com.github.moko256.twitlatte.text.convertToAppCompatNightThemeMode

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */

private const val INTENT_CLIENT_KEY = "intent_client_key"
const val LIMIT_OF_SIZE_OF_STATUSES_LIST = 1000

lateinit var preferenceRepository: PreferenceRepository

private val apiClientCache = LruCache<Int, ApiClient>(4)
private val userCache = UserCacheMap()
private val statusCache = StatusCacheMap()

class GlobalApplication : Application() {

    internal var currentClient: Client? = null

    internal lateinit var accountsModel: AccountsModel

    override fun onCreate() {
        preferenceRepository = PreferenceRepository(
                PreferenceManager.getDefaultSharedPreferences(this)
        )
        accountsModel = AccountsModel(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(
                preferenceRepository
                        .getString(KEY_NIGHT_MODE, "mode_night_no_value")
                        .convertToAppCompatNightThemeMode()
        )

        replaceOkHttpClient(appOkHttpClientInstance)

        preferenceRepository
                .getString(KEY_ACCOUNT_KEY, "-1")
                .takeIf { it != "-1" }
                ?.let {
                    accountsModel.get(it)
                }
                ?.let {
                    initCurrentClient(it)
                }

        GlideInitializer.setGlideModule(this, GlideModule())

        super.onCreate()
    }

    fun initCurrentClient(accessToken: AccessToken) {
        userCache.prepare(this, accessToken)
        statusCache.prepare(this, accessToken)
        currentClient = Client(
                accessToken,
                createApiClientInstance(accessToken),
                generateMediaUrlConverter(accessToken.clientType),
                statusCache,
                userCache
        )
    }

    fun clearCurrentClient() {
        currentClient = null
        userCache.close()
        statusCache.close()
    }

    fun createApiClientInstance(accessToken: AccessToken): ApiClient {
        val hash = accessToken.getHash()
        return apiClientCache.get(hash)
                ?: generateApiClient(accessToken).also {
                    apiClientCache.put(hash, it)
                }
    }
}

fun Activity.getClient(): Client? {
    val application = application as GlobalApplication

    return intent
            .getStringExtra(INTENT_CLIENT_KEY)
            ?.let { getAccountsModel().get(it) }
            ?.let {
                if (it == application.currentClient?.accessToken) {
                    application.currentClient
                } else {
                    Client(
                            it,
                            application.createApiClientInstance(it),
                            generateMediaUrlConverter(it.clientType),
                            StatusCacheMap(),
                            UserCacheMap()
                    ).apply {
                        userCache.prepare(this@getClient, it)
                        statusCache.prepare(this@getClient, it)
                    }
                }
            }
            ?: application.currentClient
}

fun Intent.setAccountKey(accessToken: AccessToken) = apply {
    putExtra(INTENT_CLIENT_KEY, accessToken.getKeyString())
}

fun Intent.setAccountKeyForActivity(activity: Activity): Intent {
    activity.intent.getStringExtra(INTENT_CLIENT_KEY)?.let {
        putExtra(INTENT_CLIENT_KEY, it)
    }
    return this
}

fun Activity.getCurrentClient() = (application as GlobalApplication).currentClient

fun Activity.getAccountsModel() = (application as GlobalApplication).accountsModel