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

package com.github.moko256.twitlatte.cacheMap

import com.github.moko256.latte.client.base.entity.*
import io.reactivex.subjects.BehaviorSubject
import java.util.*

/**
 * Created by moko256 on 2018/10/05.
 *
 * @author moko256
 */
class PostCache(
        private val statusCache: StatusCacheMap,
        private val userCache: UserCacheMap
) {
    val updatedEntities = BehaviorSubject.create<UpdatedEntities>()

    fun getPost(postId: Long): Post? {
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

    fun add(status: Post, incrementCount: Boolean) {
        if (status.repeat == null && status.quotedRepeatingStatus == null) {
            val users: List<User>
            val statuses: List<StatusObject>

            val user = status.user
            if (user != null) {
                userCache.add(user)
                users = listOf(user)
            } else {
                users = emptyList()
            }
            val statusObject = status.status
            if (statusObject != null) {
                statusCache.add(statusObject, incrementCount)
                statuses = listOf(statusObject)
            } else {
                statuses = emptyList()
            }

            updatedEntities.onNext(UpdatedEntities(statuses, users))
        } else {
            addAll(listOf(status), incrementCount)
        }
    }

    fun addAll(c: Collection<Post>, vararg excludeIncrementIds: Long) {
        addAll(c, true, *excludeIncrementIds)
    }

    fun addAll(c: Collection<Post>, incrementCount: Boolean = true, vararg excludeIncrementIds: Long) {
        if (c.isNotEmpty()) {
            val statuses = ArrayList<StatusObject>(c.size * 3)
            val repeats = ArrayList<Repeat>(c.size)
            val quotes = ArrayList<Status>(c.size)

            val users = ArrayList<User>(c.size * 3)

            for (status in c) {

                val mainStatus = status.status
                if (mainStatus != null && !statuses.contains(mainStatus)) {
                    statuses.add(mainStatus)
                }

                val user = status.user
                if (user != null && !users.contains(user)) {
                    users.add(user)
                }

                val repeatedUser = status.repeatedUser
                if (repeatedUser != null && !users.contains(repeatedUser)) {
                    users.add(repeatedUser)
                }

                val quotedRepeatingUser = status.quotedRepeatingUser
                if (quotedRepeatingUser != null && !users.contains(quotedRepeatingUser)) {
                    users.add(quotedRepeatingUser)
                }

                status.repeat?.let {
                    repeats.add(it)
                }

                status.quotedRepeatingStatus?.let {
                    quotes.add(it)
                }
            }

            for (status in quotes) {
                if (!statuses.contains(status)) {
                    statuses.add(status)
                }
            }

            statuses.addAll(repeats)

            userCache.addAll(users)

            statusCache.addAll(statuses, incrementCount, *excludeIncrementIds)

            updatedEntities.onNext(UpdatedEntities(statuses, users))
        }
    }

    data class UpdatedEntities(
            val statuses: List<StatusObject>,
            val users: List<User>
    )
}