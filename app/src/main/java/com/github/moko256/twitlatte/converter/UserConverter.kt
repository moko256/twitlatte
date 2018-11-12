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

package com.github.moko256.twitlatte.converter

import com.github.moko256.mastodon.MTUser
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.entity.User
import com.github.moko256.twitlatte.text.link.convertHtmlToContentAndLinks
import com.github.moko256.twitlatte.text.link.convertToContentAndLinks
import com.sys1yagi.mastodon4j.api.entity.Account

/**
 * Created by moko256 on 2018/09/09.
 *
 * @author moko256
 */
fun twitter4j.User.convertToCommonUser(): User = if (this is MTUser) {
    account.convertToCommonUser()
} else {
    convertToCommonUserInternal()
}

private fun twitter4j.User.convertToCommonUserInternal(): User {
    val urls = if (descriptionURLEntities.isNotEmpty()) {
        convertToContentAndLinks(
                description,
                emptyArray(),
                emptyArray(),
                emptyArray(),
                emptyArray(),
                descriptionURLEntities
        )
    } else {
        null
    }

    return User(
            id = id,
            name = name,
            screenName = screenName,
            location = location,
            description = urls?.first ?: description,
            isContributorsEnabled = isContributorsEnabled,
            profileImageURLHttps = profileImageURLHttps,
            isDefaultProfileImage = isDefaultProfileImage,
            url = url,
            isProtected = isProtected,
            followersCount = followersCount,
            profileBackgroundColor = profileBackgroundColor,
            profileTextColor = profileTextColor,
            profileLinkColor = profileLinkColor,
            profileSidebarFillColor = profileSidebarFillColor,
            profileSidebarBorderColor = profileSidebarBorderColor,
            isProfileUseBackgroundImage = isProfileUseBackgroundImage,
            isDefaultProfile = isDefaultProfile,
            friendsCount = friendsCount,
            createdAt = createdAt,
            favoritesCount = favouritesCount,
            utcOffset = utcOffset,
            timeZone = timeZone,
            profileBackgroundImageURLHttps = profileBackgroundImageUrlHttps,
            profileBannerImageUrl = if (profileBannerURL != null) profileBannerURL!!.replace("/web$".toRegex(), "") else null,
            isProfileBackgroundTiled = isProfileBackgroundTiled,
            lang = lang,
            statusesCount = statusesCount,
            isVerified = isVerified,
            isTranslator = isTranslator,
            isFollowRequestSent = isFollowRequestSent,
            descriptionLinks = urls?.second,
            emojis = null,
            isTwitter = true
    )
}

fun Account.convertToCommonUser(): User {
    val urls = note.convertHtmlToContentAndLinks()

    return User(
            id = id,
            name = if (displayName.isEmpty()) {
                userName
            } else {
                displayName
            },
            screenName = acct,
            location = null,
            description = urls.first,
            isContributorsEnabled = false,
            profileImageURLHttps = avatar,
            isDefaultProfileImage = true,
            url = null,
            isProtected = isLocked,
            followersCount = followersCount,
            profileBackgroundColor = null,
            profileTextColor = null,
            profileLinkColor = null,
            profileSidebarFillColor = null,
            profileSidebarBorderColor = null,
            isProfileUseBackgroundImage = true,
            isDefaultProfile = true,
            friendsCount = followingCount,
            createdAt = createdAt.toISO8601Date(),
            favoritesCount = -1,
            utcOffset = 0,
            timeZone = null,
            profileBackgroundImageURLHttps = null,
            profileBannerImageUrl = header,
            isProfileBackgroundTiled = false,
            lang = null,
            statusesCount = statusesCount,
            isVerified = false,
            isTranslator = false,
            isFollowRequestSent = false,
            descriptionLinks = if (urls.second.isEmpty()) {
                null
            } else {
                urls.second
            },
            emojis = if (emojis.isEmpty()) {
                null
            } else {
                emojis.map { Emoji(it.shortcode, it.url) }.toTypedArray()
            },
            isTwitter = false
    )
}