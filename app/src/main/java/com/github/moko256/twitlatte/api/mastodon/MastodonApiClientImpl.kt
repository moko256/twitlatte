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

package com.github.moko256.twitlatte.api.mastodon

import com.github.moko256.twitlatte.api.base.ApiClient
import com.github.moko256.twitlatte.entity.*
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.method.*
import com.sys1yagi.mastodon4j.api.method.Media
import okhttp3.MediaType
import okhttp3.MultipartBody
import twitter4j.AlternativeHttpClientImpl
import java.io.InputStream

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */
class MastodonApiClientImpl(private val client: MastodonClient): ApiClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBaseClient(): T = client as T

    override fun showPost(statusId: Long): Post {
        return Statuses(client).getStatus(statusId).execute().convertToCommonStatus()
    }

    override fun showUser(userId: Long): User {
        return Accounts(client).getAccount(userId).execute().convertToCommonUser()
    }

    override fun showUser(screenName: String): User {
        return Accounts(client)
                .getAccountSearch(screenName, 1, null)
                .execute()[0]
                .convertToCommonUser()
    }

    override fun getHomeTimeline(paging: Paging): List<Post> {
        return Timelines(client)
                .getHomeTimeline(paging.convertToMastodonRange())
                .execute()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getMentionsTimeline(paging: Paging): List<Post> {
        return Timelines(client)
                .getDirectMessageTimeline(paging.convertToMastodonRange())
                .execute()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getMediasTimeline(userId: Long, paging: Paging): List<Post> {
        return Accounts(client)
                .getStatuses(
                        accountId = userId,
                        onlyMedia = true,
                        range = paging.convertToMastodonRange()
                )
                .execute()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getFavorites(userId: Long, paging: Paging): List<Post> {
        return Favourites(client)
                .getFavourites(paging.convertToMastodonRange())
                .execute()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getUserTimeline(userId: Long, paging: Paging): List<Post> {
        return Accounts(client)
                .getStatuses(accountId = userId, range = paging.convertToMastodonRange())
                .execute()
                .part
                .map { it.convertToCommonStatus() }    }

    override fun getPostByQuery(query: String, paging: Paging): List<Post> {
        return Timelines(client)
                .getHashtagTimeline(hashtag = query, range = paging.convertToMastodonRange())
                .execute()
                .part
                .map { it.convertToCommonStatus() }    }

    override fun getFriendsList(userId: Long, cursor: Long): PageableResponse<User> {
        return Accounts(client)
                .getFollowing(userId, Range(cursor))
                .execute()
                .let { pageable ->
                    PageableResponse(
                            pageable.link?.sinceId?:-1,
                            pageable.link?.maxId?:-1,
                            pageable.part.map { it.convertToCommonUser() }
                    )
                }
    }

    override fun getFollowersList(userId: Long, cursor: Long): PageableResponse<User> {
        return Accounts(client)
                .getFollowers(userId, Range(cursor))
                .execute()
                .let { pageable ->
                    PageableResponse(
                            pageable.link?.sinceId?:-1,
                            pageable.link?.maxId?:-1,
                            pageable.part.map { it.convertToCommonUser() }
                    )
                }
    }

    override fun verifyCredentials(): User {
        return Accounts(client).getVerifyCredentials().execute().convertToCommonUser()
    }

    override fun getClosestTrends(latitude: Double, longitude: Double): List<Trend> {
        throw UnsupportedOperationException()
    }

    override fun createFavorite(statusId: Long): Post {
        return Statuses(client).postFavourite(statusId).execute().convertToCommonStatus()
    }

    override fun destroyFavorite(statusId: Long): Post {
        return Statuses(client).postUnfavourite(statusId).execute().convertToCommonStatus()
    }

    override fun createRepeat(statusId: Long): Post {
        return Statuses(client).postReblog(statusId).execute().convertToCommonStatus()
    }

    override fun destroyRepeat(statusId: Long): Post {
        return Statuses(client).postUnreblog(statusId).execute().convertToCommonStatus()
    }

    override fun createFriendship(userId: Long) {
        Accounts(client).postFollow(userId).execute()
    }

    override fun destroyFriendship(userId: Long) {
        Accounts(client).postUnFollow(userId).execute()
    }

    override fun createBlock(userId: Long) {
        Accounts(client).postBlock(userId).execute()
    }

    override fun destroyBlock(userId: Long) {
        Accounts(client).postUnblock(userId).execute()
    }

    override fun createMute(userId: Long) {
        Accounts(client).postMute(userId).execute()
    }

    override fun destroyMute(userId: Long) {
        Accounts(client).postUnmute(userId).execute()
    }

    override fun reportSpam(userId: Long) {
        throw UnsupportedOperationException()
    }

    override fun uploadMedia(inputStream: InputStream, name: String, type: String): Long {
        return Media(client)
                .postMedia(
                        MultipartBody.Part.createFormData(
                                "file",
                                name,
                                AlternativeHttpClientImpl.createInputStreamRequestBody(
                                        MediaType.parse(type),
                                        inputStream
                                )
                        ), null, null)
                .execute()
                .id
    }

    override fun postStatus(inReplyToStatusId: Long, contentWarning: String?, context: String, imageIdList: List<Long>?, isPossiblySensitive: Boolean, location: Pair<Double, Double>?, visibility: String?) {
        Statuses(client).postStatus(
                context,
                inReplyToStatusId.takeIf { it > 0 },
                imageIdList,
                isPossiblySensitive.takeIf { it },
                contentWarning?.takeIf { it.isNotEmpty() },
                visibility?.let {
                    com.sys1yagi.mastodon4j.api.entity.Status.Visibility.valueOf(it)
                }, null
        ).execute()
    }

}