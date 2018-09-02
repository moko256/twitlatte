/*
 * Copyright 2017 The twicalico authors
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

import com.github.moko256.twitlatte.text.link.entity.Link

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
sealed class StatusObject(private val innerId: Long) {
    override fun equals(other: Any?): Boolean {
        return other !== null && (this === other || other is Status && other.id == this.innerId)
    }

    override fun hashCode(): Int {
        return innerId.toInt()
    }
}

data class Status(
        val createdAt: Long,
        val id: Long,

        val userId: Long,

        val text: CharSequence,
        val sourceName: CharSequence?,
        val sourceWebsite: CharSequence?,

        val inReplyToStatusId: Long,
        val inReplyToUserId: Long,
        val inReplyToScreenName: String?,

        val isFavorited: Boolean,
        val isRepeated: Boolean,

        val favoriteCount: Int,
        val repeatCount: Int,
        val repliesCount: Int,

        val isSensitive: Boolean,
        val lang: String?,

        val urls: List<Link>?,
        val medias: List<Media>?,

        val quotedStatusId: Long,

        val url: String,
        val spoilerText: String?,
        val emojis: List<Emoji>?,
        val visibility: String?
): StatusObject(id)

data class Retweet(
        val createdAt: Long,
        val id: Long,

        val userId: Long,

        val repeatStatusId: Long
): StatusObject(id)