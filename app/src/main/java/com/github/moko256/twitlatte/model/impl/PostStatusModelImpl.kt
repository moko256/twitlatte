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

package com.github.moko256.twitlatte.model.impl

import android.content.ContentResolver
import android.net.Uri
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Emoji
import com.github.moko256.latte.client.base.entity.UpdateStatus
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

    override val updateStatus: UpdateStatus = UpdateStatus(
            inReplyToStatusId = -1,
            isPossiblySensitive = false,
            context = "",
            contentWarning = null,
            location = null,
            visibility = null,
            imageIdList = null,
            pollList = null,
            isPollSelectableMultiple = false,
            isPollHideTotalsUntilExpired = false,
            pollExpiredSecond = 0
    )

    override val statusTextLimit: Int = counter.limit
    override val uriListSizeLimit: Int = 4

    override val uriList: List<Uri> = ArrayList(uriListSizeLimit)

    override fun isReply(): Boolean {
        return updateStatus.inReplyToStatusId > 0
    }

    override fun getTweetLength(): Int {
        counter.setUpdateStatus(updateStatus, uriList.size)
        return counter.getContextLength()
    }

    override fun isValid(): Boolean {
        counter.setUpdateStatus(updateStatus, uriList.size)
        return counter.isValidStatus()
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
                updateStatus.imageIdList = ids
                apiClient.postStatus(updateStatus)
                subscriber.onComplete()
                updateStatus.imageIdList = null
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