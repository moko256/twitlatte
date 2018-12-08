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

package com.github.moko256.latte.client.base.entity

import com.github.moko256.latte.html.entity.Link
import java.util.*

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
sealed class StatusObject

data class Status(
        val createdAt: Date,
        val id: Long,

        val userId: Long,

        val text: String,
        val sourceName: String?,
        val sourceWebsite: String?,

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

        val mentions: Array<String>?,
        val urls: Array<Link>?,
        val medias: Array<Media>?,

        val quotedStatusId: Long,

        val url: String,
        val spoilerText: String?,
        val emojis: Array<Emoji>?,
        val visibility: String?
): StatusObject() {
    override fun equals(other: Any?): Boolean {
        return other !== null && (this === other || other is Status && other.id == this.id)
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}

data class Repeat(
        val createdAt: Date,
        val id: Long,

        val userId: Long,

        val repeatedStatusId: Long
): StatusObject() {
    override fun equals(other: Any?): Boolean {
        return other !== null && (this === other || other is Status && other.id == this.id)
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}

fun StatusObject.getId() = when(this) {
    is Status -> this.id
    is Repeat -> this.id
}