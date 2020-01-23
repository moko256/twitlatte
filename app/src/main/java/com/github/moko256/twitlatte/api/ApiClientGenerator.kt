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

package com.github.moko256.twitlatte.api

import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.mastodon.CLIENT_TYPE_MASTODON
import com.github.moko256.latte.client.mastodon.MastodonApiClient
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.latte.client.twitter.TwitterApiClient
import com.github.moko256.twitlatte.BuildConfig
import com.github.moko256.twitlatte.net.appOkHttpClientInstance

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */

fun generateApiClient(accessToken: AccessToken): ApiClient {
    return when (accessToken.clientType) {
        CLIENT_TYPE_TWITTER ->
            TwitterApiClient(
                    accessToken.consumerKey ?: String(BuildConfig.p, 1, 25),
                    accessToken.consumerSecret ?: String(BuildConfig.p, 27, 50),
                    accessToken.token,
                    accessToken.tokenSecret
            )
        CLIENT_TYPE_MASTODON ->
            MastodonApiClient(
                    appOkHttpClientInstance,
                    accessToken.url,
                    accessToken.token
            )
        else -> error("Invalid clientType: ${accessToken.clientType}")
    }
}