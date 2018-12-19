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

package com.github.moko256.twitlatte.model.impl

import android.content.ContentResolver
import android.net.Uri
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Emoji
import com.github.moko256.twitlatte.model.base.PostStatusModel
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by moko256 on 2017/10/23.
 *
 * @author moko256
 */

class PostStatusModelImpl(
        private val contentResolver: ContentResolver,
        private val apiClient: ApiClient
): PostStatusModel {
    private val counter = apiClient.generateCounter()

    override var inReplyToStatusId: Long = -1
    override var isPossiblySensitive: Boolean = false
    override var statusText: String = ""
    override var contentWarning: String? = null

    override val statusTextLimit: Int = counter.limit
    override val uriListSizeLimit: Int = 4

    override val uriList: List<Uri> = ArrayList(uriListSizeLimit)
    override var location: Pair<Double, Double>? = null
    override var visibility: String? = null

    override fun isReply(): Boolean {
        return inReplyToStatusId > 0
    }

    override fun getTweetLength(): Int {
        return counter.getLength(
                if (contentWarning != null) {
                    contentWarning + statusText
                } else {
                    statusText
                }
        )
    }

    override fun isValid(): Boolean {
        val text = if (contentWarning != null) {
            contentWarning + statusText
        } else {
            statusText
        }
        return if (text.isEmpty()) {
            uriList.isNotEmpty()
        } else {
            counter.isValid(text)
        }
    }

    override fun post(): Completable {
        return Completable.create { subscriber ->
            try {
                val ids = if (uriList.isNotEmpty()) {
                    LongArray(uriList.size) {
                        val uri = uriList[it]
                        contentResolver.openInputStream(uri)?.let { image ->
                            apiClient.uploadMedia(image, uri.lastPathSegment?:"media", contentResolver.getType(uri)?:"")
                        } ?: -1
                    }.toList()
                } else {
                    null
                }
                apiClient.postStatus(
                        inReplyToStatusId,
                        contentWarning,
                        statusText,
                        ids,
                        isPossiblySensitive,
                        location,
                        visibility
                )
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.tryOnError(e)
            }
        }
    }

    override fun requestCustomEmojis(): Single<List<Emoji>> {
        return Single.create {
            try {
                it.onSuccess(apiClient.getCustomEmojis())
            } catch (e: Throwable) {
                it.tryOnError(e)
            }
        }
    }
}