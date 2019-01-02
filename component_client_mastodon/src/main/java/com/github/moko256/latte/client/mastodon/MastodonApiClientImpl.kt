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

package com.github.moko256.latte.client.mastodon

import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.StatusCounter
import com.github.moko256.latte.client.base.entity.*
import com.github.moko256.latte.client.mastodon.gson.gson
import com.github.moko256.latte.client.mastodon.okhttp.InputStreamRequestBody
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.method.*
import com.sys1yagi.mastodon4j.api.method.Media
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import java.io.InputStream

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */
const val CLIENT_TYPE_MASTODON = 1

class MastodonApiClientImpl(okHttpClient: OkHttpClient, url: String, token: String): ApiClient {

    private val client: MastodonClient = MastodonClient
            .Builder(url, okHttpClient.newBuilder(), gson)
            .accessToken(token)
            .build()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBaseClient(): T = client as T

    override fun generateCounter(): StatusCounter {
        return MastodonStatusCounter()
    }

    override fun showPost(statusId: Long): Post {
        return Statuses(client).getStatus(statusId).executeAndConvertError().convertToCommonStatus()
    }

    override fun showUser(userId: Long): User {
        return Accounts(client).getAccount(userId).executeAndConvertError().convertToCommonUser()
    }

    override fun showUser(screenName: String): User {
        return Accounts(client)
                .getAccountSearch(screenName, 1, null)
                .executeAndConvertError()[0]
                .convertToCommonUser()
    }

    override fun getHomeTimeline(paging: Paging): List<Post> {
        return Timelines(client)
                .getHomeTimeline(paging.convertToMastodonRange())
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getMentionsTimeline(paging: Paging): List<Post> {
        return Timelines(client)
                .getDirectMessageTimeline(paging.convertToMastodonRange())
                .executeAndConvertError()
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
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getFavorites(userId: Long, paging: Paging): List<Post> {
        return Favourites(client)
                .getFavourites(paging.convertToMastodonRange())
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getUserTimeline(userId: Long, paging: Paging): List<Post> {
        return Accounts(client)
                .getStatuses(accountId = userId, range = paging.convertToMastodonRange())
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getPostByQuery(query: String, paging: Paging): List<Post> {
        return Timelines(client)
                .getHashtagTimeline(hashtag = query, range = paging.convertToMastodonRange())
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getFriendsList(userId: Long, cursor: Long): PageableResponse<User> {
        return Accounts(client)
                .getFollowing(userId, Range(cursor))
                .executeAndConvertError()
                .let { pageable ->
                    PageableResponse(
                            pageable.link?.sinceId ?: -1,
                            pageable.link?.maxId ?: -1,
                            pageable.part.map { it.convertToCommonUser() }
                    )
                }
    }

    override fun getFollowersList(userId: Long, cursor: Long): PageableResponse<User> {
        return Accounts(client)
                .getFollowers(userId, Range(cursor))
                .executeAndConvertError()
                .let { pageable ->
                    PageableResponse(
                            pageable.link?.sinceId ?: -1,
                            pageable.link?.maxId ?: -1,
                            pageable.part.map { it.convertToCommonUser() }
                    )
                }
    }

    override fun verifyCredentials(): User {
        return Accounts(client).getVerifyCredentials().executeAndConvertError().convertToCommonUser()
    }

    override fun getClosestTrends(latitude: Double, longitude: Double): List<Trend> {
        throw UnsupportedOperationException()
    }

    override fun createFavorite(statusId: Long): Post {
        return Statuses(client).postFavourite(statusId).executeAndConvertError().convertToCommonStatus()
    }

    override fun destroyFavorite(statusId: Long): Post {
        return Statuses(client).postUnfavourite(statusId).executeAndConvertError().convertToCommonStatus()
    }

    override fun createRepeat(statusId: Long): Post {
        return Statuses(client).postReblog(statusId).executeAndConvertError().convertToCommonStatus()
    }

    override fun destroyRepeat(statusId: Long): Post {
        return Statuses(client).postUnreblog(statusId).executeAndConvertError().convertToCommonStatus()
    }

    override fun createFriendship(userId: Long) {
        Accounts(client).postFollow(userId).executeAndConvertError()
    }

    override fun destroyFriendship(userId: Long) {
        Accounts(client).postUnFollow(userId).executeAndConvertError()
    }

    override fun createBlock(userId: Long) {
        Accounts(client).postBlock(userId).executeAndConvertError()
    }

    override fun destroyBlock(userId: Long) {
        Accounts(client).postUnblock(userId).executeAndConvertError()
    }

    override fun createMute(userId: Long) {
        Accounts(client).postMute(userId).executeAndConvertError()
    }

    override fun destroyMute(userId: Long) {
        Accounts(client).postUnmute(userId).executeAndConvertError()
    }

    override fun reportSpam(userId: Long) {
        throw UnsupportedOperationException()
    }

    override fun getLists(userId: Long): List<ListEntry> {
        return Lists(client).getLists().executeAndConvertError()
                .map { ListEntry(it.id, it.title, null, false) }
    }

    override fun getListTimeline(listId: Long, paging: Paging): List<Post> {
        return Timelines(client).getListTimeline(listId, paging.convertToMastodonRange())
                .executeAndConvertError()
                .part
                .map { it.convertToCommonStatus() }
    }

    override fun getCustomEmojis(): List<Emoji> {
        return Instances(client).getCustomEmojis().executeAndConvertError().map {
            Emoji(it.shortcode, it.staticUrl)
        }
    }

    override fun uploadMedia(inputStream: InputStream, name: String, type: String): Long {
        return Media(client)
                .postMedia(
                        MultipartBody.Part.createFormData(
                                "file",
                                name,
                                InputStreamRequestBody(MediaType.parse(type), inputStream)
                        ), null, null)
                .executeAndConvertError()
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
        ).executeAndConvertError()
    }

}