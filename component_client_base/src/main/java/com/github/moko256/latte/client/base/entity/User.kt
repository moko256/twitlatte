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

package com.github.moko256.latte.client.base.entity

import com.github.moko256.latte.html.entity.Link
import java.util.*

/**
 * Created by moko256 on 2018/09/09.
 *
 * @author moko256
 */
data class User(
        val id: Long,

        val name: String,
        val screenName: String,

        val description: String,
        val descriptionLinks: Array<Link>?,
        val emojis: Array<Emoji>?,

        val location: String?,
        val createdAt: Date,
        val url: String?,

        val isProtected: Boolean,
        val isVerified: Boolean,

        val statusesCount: Int,
        val favoritesCount: Int,
        val followersCount: Int,
        val friendsCount: Int,

        val profileBannerImageUrl: String?,
        val profileImageURLHttps: String
) {

    override fun hashCode(): Int {
        return id.toInt()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is User && other.id == this.id
    }

}