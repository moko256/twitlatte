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

package com.github.moko256.core.client.base

import com.github.moko256.core.client.base.entity.*
import java.io.InputStream

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */
const val CLIENT_TYPE_NOTHING = -1

interface ApiClient {
    fun <T> getBaseClient(): T

    fun showPost(statusId: Long): Post
    fun showUser(userId: Long): User
    fun showUser(screenName: String): User

    fun getHomeTimeline(paging: Paging): List<Post>
    fun getMentionsTimeline(paging: Paging): List<Post>
    fun getMediasTimeline(userId: Long, paging: Paging): List<Post>
    fun getFavorites(userId: Long, paging: Paging): List<Post>
    fun getUserTimeline(userId: Long, paging: Paging): List<Post>

    fun getPostByQuery(query: String, paging: Paging): List<Post>

    fun getFriendsList(userId: Long, cursor: Long): PageableResponse<User>
    fun getFollowersList(userId: Long, cursor: Long): PageableResponse<User>

    fun verifyCredentials(): User

    fun getClosestTrends(latitude: Double, longitude: Double): List<Trend>

    fun createFavorite(statusId: Long): Post
    fun destroyFavorite(statusId: Long): Post
    fun createRepeat(statusId: Long): Post
    fun destroyRepeat(statusId: Long): Post

    fun createFriendship(userId: Long)
    fun destroyFriendship(userId: Long)

    fun createBlock(userId: Long)
    fun destroyBlock(userId: Long)

    fun createMute(userId: Long)
    fun destroyMute(userId: Long)

    fun reportSpam(userId: Long)

    fun uploadMedia(inputStream: InputStream, name: String, type: String): Long
    fun postStatus(
            inReplyToStatusId: Long,
            contentWarning: String?,
            context: String,
            imageIdList: List<Long>?,
            isPossiblySensitive: Boolean,
            location: Pair<Double, Double>?,
            visibility: String?
    )
}