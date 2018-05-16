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

import com.github.moko256.twitlatte.GlobalApplication
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.Type
import com.github.moko256.twitlatte.model.base.OAuthModel
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Apps
import rx.Single
import twitter4j.conf.ConfigurationContext

/**
 * Created by moko256 on 2017/10/29.
 *
 * @author moko256
 */
class OAuthModelImpl : OAuthModel {
    private lateinit var clientBuilder: MastodonClient.Builder
    private lateinit var apps: Apps
    private var appRegistration: AppRegistration? = null

    override fun getCallbackAuthUrl(url: String, consumerKey: String, consumerSecret: String, callbackUrl: String): Single<String> {
        clientBuilder = MastodonClient.Builder(
                url,
                GlobalApplication.getOkHttpClient(ConfigurationContext.getInstance().httpClientConfiguration).newBuilder(),
                Gson()
        )
        apps = Apps(clientBuilder.build())
        return Single.create {
            try {
                appRegistration = apps.createApp(
                        "twitlatte",
                        callbackUrl,
                        Scope(Scope.Name.ALL),
                        "https://github.com/moko256/twitlatte"
                ).execute()

                it.onSuccess(
                        apps.getOAuthUrl(
                                appRegistration?.clientId!!,
                                Scope(Scope.Name.ALL),
                                callbackUrl
                        ))
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }
        }
    }

    override fun getCodeAuthUrl(url: String, consumerKey: String, consumerSecret: String): Single<String> {
        clientBuilder = MastodonClient.Builder(
                url,
                GlobalApplication.getOkHttpClient(ConfigurationContext.getInstance().httpClientConfiguration).newBuilder(),
                Gson()
        )
        apps = Apps(clientBuilder.build())
        return Single.create {
            try {
                appRegistration = apps.createApp(
                        clientName = "twitlatte",
                        scope = Scope(Scope.Name.ALL),
                        website = "https://github.com/moko256/twitlatte"
                ).execute()

                it.onSuccess(
                        apps.getOAuthUrl(
                                clientId = appRegistration?.clientId!!,
                                scope = Scope(Scope.Name.ALL)
                ))
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                val accessToken = apps.getAccessToken(
                        appRegistration?.clientId!!,
                        appRegistration?.clientSecret!!,
                        appRegistration?.redirectUri!!,
                        pin
                ).execute()
                val url = clientBuilder.build().getInstanceName()
                val account = Accounts(clientBuilder.accessToken(accessToken.accessToken).build()).getVerifyCredentials().execute()
                val storeToken = AccessToken(
                        Type.MASTODON,
                        url,
                        account.id,
                        account.acct,
                        accessToken.accessToken,
                        null
                )
                it.onSuccess(storeToken)
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }

        }
    }

}