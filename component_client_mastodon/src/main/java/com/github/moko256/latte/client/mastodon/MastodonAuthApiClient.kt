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

package com.github.moko256.latte.client.mastodon

import com.github.moko256.latte.client.base.AuthApiClient
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.RequestToken
import com.github.moko256.latte.client.mastodon.gson.gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Apps
import okhttp3.OkHttpClient

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
class MastodonAuthApiClient(okHttpClient: OkHttpClient): AuthApiClient {
    private val okHttpClientBuilder = okHttpClient.newBuilder()

    override fun getOAuthRequestToken(
            optionalConsumerKey: String?,
            optionalConsumerSecret: String?,
            serverUrl: String,
            callbackUrl: String?
    ): RequestToken {
        val apps = Apps(
                MastodonClient.Builder(
                        serverUrl,
                        okHttpClientBuilder,
                        gson
                ).build()
        )

        val callbackUrlNonNull = callbackUrl ?: "urn:ietf:wg:oauth:2.0:oob"
        val scopes = Scope(Scope.Name.READ, Scope.Name.WRITE, Scope.Name.FOLLOW)
        return apps.createApp(
                "twitlatte",
                callbackUrlNonNull,
                scopes,
                "https://github.com/moko256/twitlatte"
        ).executeAndConvertError().let {
            RequestToken(
                    serverUrl,
                    apps.getOAuthUrl(it.clientId, scopes, callbackUrlNonNull),
                    callbackUrlNonNull,
                    null,
                    null,
                    it.clientId,
                    it.clientSecret
            )
        }
    }

    override fun initializeToken(requestToken: RequestToken, key: String): AccessToken {
        val builder = MastodonClient.Builder(
                requestToken.serverUrl,
                okHttpClientBuilder,
                gson
        )

        val accessToken = Apps(builder.build()).getAccessToken(
                requestToken.token,
                requestToken.tokenSecret,
                requestToken.redirectUrl,
                key
        ).executeAndConvertError().accessToken

        val account = Accounts(
                builder.accessToken(accessToken).build()
        ).getVerifyCredentials().executeAndConvertError()

        return AccessToken(
                CLIENT_TYPE_MASTODON,
                requestToken.serverUrl,
                account.id,
                account.acct,
                null,
                null,
                accessToken,
                null
        )
    }
}