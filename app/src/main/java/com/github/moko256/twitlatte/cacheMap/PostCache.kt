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

package com.github.moko256.twitlatte.cacheMap

import com.github.moko256.twitlatte.entity.Post
import com.github.moko256.twitlatte.entity.Repeat
import com.github.moko256.twitlatte.entity.Status
import com.github.moko256.twitlatte.entity.User

/**
 * Created by moko256 on 2018/10/05.
 *
 * @author moko256
 */
class PostCache(
        private val statusCache: StatusCacheMap,
        private val userCache: UserCacheMap
) {
    fun getPost(postId: Long): Post?{
        return statusCache.get(postId)?.let { statusObject ->
            val repeat: Repeat?
            val repeatedUser: User?

            val status: Status?
            val user: User?

            val quotedStatus: Status?
            val quoteUser: User?

            when (statusObject) {
                is Repeat -> {
                    repeat = statusObject
                    repeatedUser = userCache.get(statusObject.userId)
                    status = statusCache.get(statusObject.repeatedStatusId) as Status?
                }
                is Status -> {
                    repeat = null
                    repeatedUser = null
                    status = statusObject
                }
            }

            if (status != null) {
                user = userCache.get(status.userId)
                quotedStatus = statusCache.get(status.quotedStatusId) as Status?
                quoteUser = quotedStatus?.let {
                    userCache.get(it.userId)
                }
            } else {
                user = null
                quotedStatus = null
                quoteUser = null
            }

            Post(
                    id = postId,
                    repeat = repeat,
                    repeatedUser = repeatedUser,
                    status = status,
                    user = user,
                    quotedRepeatingStatus = quotedStatus,
                    quotedRepeatingUser = quoteUser
            )
        }
    }
}