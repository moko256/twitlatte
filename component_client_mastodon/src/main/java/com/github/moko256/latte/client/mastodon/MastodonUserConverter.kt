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

import com.github.moko256.latte.client.base.entity.Emoji
import com.github.moko256.latte.client.base.entity.User
import com.github.moko256.latte.client.mastodon.date.toISO8601Date
import com.github.moko256.latte.html.convertHtmlToContentAndLinks
import com.sys1yagi.mastodon4j.api.entity.Account

/**
 * Created by moko256 on 2018/12/01.
 *
 * @author moko256
 */

internal fun Account.convertToCommonUser(): User {
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
            profileImageURLHttps = avatar,
            url = null,
            isProtected = isLocked,
            followersCount = followersCount,
            friendsCount = followingCount,
            createdAt = createdAt.toISO8601Date(),
            favoritesCount = -1,
            profileBannerImageUrl = header,
            statusesCount = statusesCount,
            isVerified = false,
            descriptionLinks = urls.second?.takeIf { it.isNotEmpty() },
            emojis = if (emojis.isEmpty()) {
                null
            } else {
                emojis.map { Emoji(it.shortcode, it.url) }.toTypedArray()
            }
    )
}