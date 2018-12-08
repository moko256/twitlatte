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

package com.github.moko256.twitlatte.core.client.twitter

import com.github.moko256.core.client.base.AuthApiClient
import com.github.moko256.core.client.base.entity.AccessToken
import com.github.moko256.core.client.base.entity.RequestToken
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
class TwitterAuthApiClient(consumerKey: String, consumerSecret: String): AuthApiClient {
    private val oauth = OAuthAuthorization(
            ConfigurationBuilder()
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .build()
    )

    override fun getOAuthRequestToken(serverUrl: String, callbackUrl: String?): RequestToken {
        val redirectUrl = callbackUrl ?: "oob"

        return oauth.getOAuthRequestToken(redirectUrl).let {
            RequestToken(
                    serverUrl,
                    it.authorizationURL,
                    redirectUrl,
                    it.token,
                    it.tokenSecret
            )
        }
    }

    override fun initializeToken(requestToken: RequestToken, key: String): AccessToken {
        return oauth.getOAuthAccessToken(
                twitter4j.auth.RequestToken(
                        requestToken.token,
                        requestToken.tokenSecret
                ),
                key
        ).let {
            AccessToken(
                    CLIENT_TYPE_TWITTER,
                    requestToken.serverUrl,
                    it.userId,
                    it.screenName,
                    it.token,
                    it.tokenSecret
            )
        }
    }
}