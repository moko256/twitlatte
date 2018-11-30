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

package com.github.moko256.twitlatte.model.impl.mastodon

import android.os.Bundle
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.ClientType
import com.github.moko256.twitlatte.model.base.OAuthModel
import com.github.moko256.twitlatte.net.appOkHttpClientInstance
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Apps
import io.reactivex.Single

/**
 * Created by moko256 on 2017/10/29.
 *
 * @author moko256
 */
private const val STATE_MODEL_URL = "state_model_url"
private const val STATE_MODEL_CLIENT_ID = "state_client_id"
private const val STATE_MODEL_SECRET = "state_model_secret"
private const val STATE_MODEL_REDIRECT_URL = "state_model_redirect_url"

class OAuthModelImpl(override var isRestartable: Boolean = false) : OAuthModel {
    private lateinit var clientBuilder: MastodonClient.Builder
    private lateinit var apps: Apps

    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var redirectUrl: String

    override fun restoreInstanceState(savedInstanceState: Bundle) {
        clientId = savedInstanceState.getString(STATE_MODEL_CLIENT_ID)!!
        clientSecret = savedInstanceState.getString(STATE_MODEL_SECRET)!!
        redirectUrl = savedInstanceState.getString(STATE_MODEL_REDIRECT_URL)!!

        clientBuilder = MastodonClient.Builder(
                savedInstanceState.getString(STATE_MODEL_URL)!!,
                appOkHttpClientInstance.newBuilder(),
                Gson()
        )
        apps = Apps(clientBuilder.build())
        isRestartable = true
    }

    override fun saveInstanceState(outState: Bundle) {
        if (isRestartable) {
            outState.apply {
                putString(STATE_MODEL_URL, clientBuilder.build().getInstanceName())
                putString(STATE_MODEL_CLIENT_ID, clientId)
                putString(STATE_MODEL_SECRET, clientSecret)
                putString(STATE_MODEL_REDIRECT_URL, redirectUrl)
            }
        }
    }

    override fun getAuthUrl(url: String, callbackUrl: String?): Single<String> {
        clientBuilder = MastodonClient.Builder(
                url,
                appOkHttpClientInstance.newBuilder(),
                Gson()
        )
        apps = Apps(clientBuilder.build())
        return Single.create {
            try {
                val appRegistration = if (callbackUrl != null) {
                    apps.createApp(
                            "twitlatte",
                            callbackUrl,
                            Scope(Scope.Name.ALL),
                            "https://github.com/moko256/twitlatte"
                    )
                } else {
                    apps.createApp(
                            clientName = "twitlatte",
                            scope = Scope(Scope.Name.ALL),
                            website = "https://github.com/moko256/twitlatte"
                    )
                }.execute()

                clientId = appRegistration.clientId
                clientSecret = appRegistration.clientSecret
                redirectUrl = appRegistration.redirectUri

                it.onSuccess(if (callbackUrl != null) {
                    apps.getOAuthUrl(
                            clientId,
                            Scope(Scope.Name.ALL),
                            callbackUrl
                    )
                } else {
                    apps.getOAuthUrl(
                            clientId = clientId,
                            scope = Scope(Scope.Name.ALL)
                    )
                })
                isRestartable = true
            } catch (e: Mastodon4jRequestException) {
                it.tryOnError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                val accessToken = apps.getAccessToken(
                        clientId,
                        clientSecret,
                        redirectUrl,
                        pin
                ).execute()
                val url = clientBuilder.build().getInstanceName()
                val account = Accounts(clientBuilder.accessToken(accessToken.accessToken).build()).getVerifyCredentials().execute()
                val storeToken = AccessToken(
                        ClientType.MASTODON,
                        url,
                        account.id,
                        account.acct,
                        accessToken.accessToken,
                        null
                )
                it.onSuccess(storeToken)
                isRestartable = false
            } catch (e: Mastodon4jRequestException) {
                it.tryOnError(e)
            }

        }
    }

}