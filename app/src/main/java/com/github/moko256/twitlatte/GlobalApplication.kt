/*
 * Copyright 2015-2018 The twitlatte authors
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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.collection.LruCache
import com.github.moko256.mastodon.MastodonTwitterImpl
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap
import com.github.moko256.twitlatte.converter.convertToAppCompatNightThemeMode
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.entity.ClientType
import com.github.moko256.twitlatte.model.AccountsModel
import com.github.moko256.twitlatte.net.replaceSocketFactory
import com.github.moko256.twitlatte.queue.StatusActionQueue
import com.github.moko256.twitlatte.repository.KEY_ACCOUNT_KEY
import com.github.moko256.twitlatte.repository.KEY_NIGHT_MODE
import com.github.moko256.twitlatte.repository.PreferenceRepository
import com.github.moko256.twitlatte.text.TwitterStringUtils
import twitter4j.AlternativeHttpClientImpl
import twitter4j.HttpClientFactory
import twitter4j.Twitter
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import twitter4j.createTwitterInstance

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */

private const val INTENT_CLIENT_KEY = "intent_client_key"
const val LIMIT_OF_SIZE_OF_STATUSES_LIST = 1000

@JvmField val statusActionQueue = StatusActionQueue()

lateinit var preferenceRepository: PreferenceRepository

private val twitterCache = LruCache<Configuration, Twitter>(4)
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

        preferenceRepository
                .getString(KEY_ACCOUNT_KEY, "-1")
                .takeIf { it != "-1" }
                ?.let {
                    accountsModel.get(it)
                }
                ?.let {
                    initTwitter(it)
                }

        super.onCreate()
    }

    fun initTwitter(accessToken: AccessToken) {
        userCache.prepare(this, accessToken)
        statusCache.prepare(this, accessToken, userCache)
        currentClient = Client(
                accessToken,
                createTwitterInstance(accessToken),
                statusCache,
                userCache
        )
    }

    fun clearTwitter() {
        currentClient = null
        userCache.close()
        statusCache.close()
    }

    fun createTwitterInstance(accessToken: AccessToken): Twitter {
        var t: Twitter?

        val conf: Configuration

        if (accessToken.type == ClientType.TWITTER) {
            if (accessToken.token.isEmpty()) {
                Toast.makeText(this, R.string.please_re_login, Toast.LENGTH_LONG).show()
            }
            conf = ConfigurationBuilder()
                    .setTweetModeExtended(true)
                    .setOAuthConsumerKey(String(BuildConfig.p, 1, 25))
                    .setOAuthConsumerSecret(String(BuildConfig.p, 27, 50))
                    .setOAuthAccessToken(accessToken.token)
                    .setOAuthAccessTokenSecret(accessToken.tokenSecret)
                    .build()
                    .replaceCompatibleOkHttpClient()

            t = twitterCache.get(conf)

            if (t == null) {
                t = conf.createTwitterInstance()
                twitterCache.put(conf, t)
            }
        } else {
            conf = ConfigurationBuilder()
                    .setOAuthAccessToken(accessToken.token)
                    .setRestBaseURL(accessToken.url)
                    .build()
                    .replaceCompatibleOkHttpClient()

            t = twitterCache.get(conf)

            if (t == null) {
                t = MastodonTwitterImpl(
                        conf,
                        accessToken.userId,
                        conf.getOkHttpClient().newBuilder()
                )
                twitterCache.put(conf, t)
            }
        }

        return t
    }
}

fun Activity.getClient(): Client? {
    val application = application as GlobalApplication

    return intent.getStringExtra(INTENT_CLIENT_KEY)?.let { key ->
        val accessToken = getAccountsModel().get(key)
        Client(
                accessToken!!,
                application.createTwitterInstance(accessToken),
                StatusCacheMap(),
                UserCacheMap()
        ).apply {
            userCache.prepare(this@getClient, accessToken)
            statusCache.prepare(this@getClient, accessToken, userCache)
            Toast.makeText(
                    this@getClient,
                    TwitterStringUtils.plusAtMark(
                            accessToken.screenName,
                            accessToken.url
                    ),
                    Toast.LENGTH_SHORT
            ).show()
        }
    }?:application.currentClient
}

fun Intent.setAccountKey(accessToken: AccessToken) = apply {
    putExtra(INTENT_CLIENT_KEY, accessToken.getKeyString())
}

fun Activity.getCurrentClient() = (application as GlobalApplication).currentClient

fun Activity.getAccountsModel() = (application as GlobalApplication).accountsModel

private fun Configuration.replaceCompatibleOkHttpClient() = apply {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        getT4jHttpClient().replaceSocketFactory()
    }
}

fun Context.getOkHttpClient() =
        (this as GlobalApplication).currentClient!!.twitter.configuration.getOkHttpClient()

fun Configuration.getOkHttpClient() =
        replaceCompatibleOkHttpClient().getT4jHttpClient().okHttpClient!!

private fun Configuration.getT4jHttpClient() =
        HttpClientFactory.getInstance(this.httpClientConfiguration) as AlternativeHttpClientImpl