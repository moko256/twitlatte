/*
 * Copyright 2018 The twicalico authors
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

package com.github.moko256.twicalico.model.impl.mastodon

import com.github.moko256.twicalico.entity.AccessToken
import com.github.moko256.twicalico.entity.Type
import com.github.moko256.twicalico.model.base.OAuthModel
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Apps
import okhttp3.OkHttpClient
import rx.Single

/**
 * Created by moko256 on 2017/10/29.
 *
 * @author moko256
 */
class OAuthModelImpl : OAuthModel {
    var clientBuilder: MastodonClient.Builder? = null
    var apps: Apps? = null
    var appRegistration: AppRegistration? = null

    override fun getCallbackAuthUrl(url: String, consumerKey: String, consumerSecret: String, callbackUrl: String): Single<String> {
        clientBuilder = MastodonClient.Builder(url, OkHttpClient.Builder(), Gson())
        apps = Apps(clientBuilder?.build()!!)
        return Single.create {
            try {
                appRegistration = apps?.createApp(
                        "twicalico",
                        callbackUrl + "://OAuthActivity",
                        Scope(Scope.Name.ALL),
                        "https://github.com/moko256/twicalico"
                )?.execute()

                it.onSuccess(
                        apps?.getOAuthUrl(
                                appRegistration?.clientId!!,
                                Scope(Scope.Name.ALL),
                                appRegistration?.redirectUri!!
                        ))
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }
        }
    }

    override fun getCodeAuthUrl(url: String, consumerKey: String, consumerSecret: String): Single<String> {
        clientBuilder = MastodonClient.Builder(url, OkHttpClient.Builder(), Gson())
        apps = Apps(clientBuilder?.build()!!)
        return Single.create {
            try {
                appRegistration = apps?.createApp(
                        "twicalico",
                        "urn:ietf:wg:oauth:2.0:oob",
                        Scope(Scope.Name.ALL),
                        "https://github.com/moko256/twicalico"
                )?.execute()

                it.onSuccess(
                        apps?.getOAuthUrl(
                                appRegistration?.clientId!!,
                                Scope(Scope.Name.ALL),
                                appRegistration?.redirectUri!!
                ))
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                val accessToken = apps?.getAccessToken(
                        appRegistration?.clientId!!,
                        appRegistration?.clientSecret!!,
                        appRegistration?.redirectUri!!,
                        pin,
                        "authorization_code"
                )?.execute()
                val url = clientBuilder?.build()?.getInstanceName() ?: ""
                val account = Accounts(clientBuilder?.accessToken(accessToken?.accessToken!!)?.build()!!).getVerifyCredentials().execute()
                val storeToken = AccessToken(
                        Type.MASTODON,
                        url,
                        account.id,
                        account.acct,
                        accessToken?.accessToken ?: "",
                        null
                )
                it.onSuccess(storeToken)
            } catch (e: Mastodon4jRequestException) {
                it.onError(e)
            }

        }
    }

}