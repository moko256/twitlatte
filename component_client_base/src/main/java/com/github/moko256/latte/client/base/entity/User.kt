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
        val location: String?,
        val description: String,
        val isContributorsEnabled: Boolean,
        val profileImageURLHttps: String,
        val isDefaultProfileImage: Boolean,
        val url: String?,
        val isProtected: Boolean,
        val followersCount: Int,
        val profileBackgroundColor: String?,
        val profileTextColor: String?,
        val profileLinkColor: String?,
        val profileSidebarFillColor: String?,
        val profileSidebarBorderColor: String?,
        val isProfileUseBackgroundImage: Boolean,
        val isDefaultProfile: Boolean,
        val friendsCount: Int,
        val createdAt: Date,
        val favoritesCount: Int,
        val utcOffset: Int,
        val timeZone: String?,
        val profileBackgroundImageURLHttps: String?,
        val profileBannerImageUrl: String?,
        val isProfileBackgroundTiled: Boolean,
        val lang: String?,
        val statusesCount: Int,
        val isVerified: Boolean,
        val isTranslator: Boolean,
        val isFollowRequestSent: Boolean,
        val descriptionLinks: Array<Link>?,
        val emojis: Array<Emoji>?
) {

    override fun hashCode(): Int {
        return id.toInt()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is User && other.id == this.id
    }

}