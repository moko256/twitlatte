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

package com.github.moko256.twitlatte.entity

import com.github.moko256.twitlatte.api.base.ApiClient
import com.github.moko256.twitlatte.api.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.twitlatte.cacheMap.PostCache
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.cacheMap.UserCacheMap

/**
 * Created by moko256 on 2018/11/28.
 *
 * @author moko256
 */
data class Client(
        val accessToken: AccessToken,
        val apiClient: ApiClient,
        val statusCache: StatusCacheMap,
        val userCache: UserCacheMap
) {
    val postCache: PostCache = PostCache(statusCache, userCache)

    val statusLimit: Int = if (accessToken.clientType == CLIENT_TYPE_TWITTER) 200 else 40

}