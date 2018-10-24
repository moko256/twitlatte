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

package com.github.moko256.twitlatte.model.base

import android.net.Uri

import io.reactivex.Completable
import twitter4j.GeoLocation

/**
 * Created by moko256 on 2017/07/22.
 *
 * @author moko256
 */

interface PostTweetModel {

    var inReplyToStatusId: Long

    var isPossiblySensitive: Boolean

    var tweetText: String

    var contentWarning: String?

    val tweetLength: Int
    val maxTweetLength: Int

    val isReply: Boolean
    val isValidTweet: Boolean

    val uriList: List<Uri>
    val uriListSizeLimit: Int

    var location: GeoLocation?

    var visibility: String?

    fun postTweet(): Completable

}