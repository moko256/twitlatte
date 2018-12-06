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

package com.github.moko256.twitlatte.api

import com.github.moko256.twitlatte.BuildConfig
import com.github.moko256.twitlatte.api.base.ApiClient
import com.github.moko256.twitlatte.api.mastodon.MastodonApiClientImpl
import com.github.moko256.twitlatte.api.twitter.TwitterApiClientImpl
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.gson.gson
import com.sys1yagi.mastodon4j.MastodonClient
import okhttp3.OkHttpClient
import twitter4j.conf.ConfigurationBuilder
import twitter4j.createTwitterInstance

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */

fun generateTwitterApiClient(accessToken: AccessToken): ApiClient {
    return TwitterApiClientImpl(
            ConfigurationBuilder()
                    .setTweetModeExtended(true)
                    .setOAuthConsumerKey(String(BuildConfig.p, 1, 25))
                    .setOAuthConsumerSecret(String(BuildConfig.p, 27, 50))
                    .setOAuthAccessToken(accessToken.token)
                    .setOAuthAccessTokenSecret(accessToken.tokenSecret)
                    .build()
                    .createTwitterInstance()
    )
}

fun generateMastodonApiClient(okHttpClient: OkHttpClient, accessToken: AccessToken): ApiClient {
    return MastodonApiClientImpl(
            MastodonClient
                    .Builder(accessToken.url, okHttpClient.newBuilder(), gson)
                    .accessToken(accessToken.token)
                    .build()
    )
}