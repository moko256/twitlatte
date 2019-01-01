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
import com.github.moko256.latte.client.mastodon.MastodonApiClientImpl
import com.github.moko256.latte.client.twitter.TwitterApiClientImpl
import com.github.moko256.twitlatte.BuildConfig
import okhttp3.OkHttpClient

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */

fun generateTwitterApiClient(accessToken: AccessToken): ApiClient {
    return TwitterApiClientImpl(
            String(BuildConfig.p, 1, 25),
            String(BuildConfig.p, 27, 50),
            accessToken.token,
            accessToken.tokenSecret
    )
}

fun generateMastodonApiClient(okHttpClient: OkHttpClient, accessToken: AccessToken): ApiClient {
    return MastodonApiClientImpl(
            okHttpClient,
            accessToken.url,
            accessToken.token
    )
}