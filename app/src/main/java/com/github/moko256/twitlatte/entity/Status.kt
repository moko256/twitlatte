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

package com.github.moko256.twicalico.entity

import java.util.*

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
data class Status(
        val id: Long,
        val userId: Long,

        val text: String,
        //val source: Source?,
        val source: String?,

        val createdAt: Long,

        val repeatStatusId: Long?,

        val inReplyToStatusId: Long?,
        val inReplyToUserId: Long?,
        val inReplyToScreenName: String?,

        val isFavorited: Boolean,
        val isRepeated: Boolean,

        val favoriteCount: Int,
        val repeatCount: Int,

        val isSensitive: Boolean,
        val lang: String?,

        val userMentions: Array<Pair<String, IntRange>>?,
        val urls: Array<Pair<String, IntRange>>?,
        val hashtags: Array<Pair<String, IntRange>>?,
        val medias: Array<Pair<Media, IntRange>>?,
        val symbols: Array<Pair<String, IntRange>>?,

        val quotedStatusId: Long?
) {
    override fun equals(other: Any?): Boolean {
        return other !== null && (this === other || other is Status && other.id == this.id)
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}