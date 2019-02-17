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

package com.github.moko256.latte.client.twitter

import com.github.moko256.latte.client.base.entity.*
import com.github.moko256.latte.html.convertHtmlToContentAndLinks
import com.github.moko256.latte.html.entity.Link

/**
 * Created by moko256 on 2018/12/01.
 *
 * @author moko256
 */

internal fun twitter4j.Status.convertToPost(): Post {
    val status: Status
    val statusUser: User
    val repeat: Repeat?
    val repeatUser: User?
    val quotedStatus: Status?
    val quotedStatusUser: User?

    val baseRepeat = retweetedStatus
    if (baseRepeat == null) {
        status = convertToStatus()
        statusUser = user.convertToCommonUser()
        repeat = null
        repeatUser = null
        if (getQuotedStatus() != null) {
            quotedStatus = getQuotedStatus().convertToStatus()
            quotedStatusUser = getQuotedStatus().user.convertToCommonUser()
        } else {
            quotedStatus = null
            quotedStatusUser = null
        }
    } else {
        repeat = convertToRepeat()
        repeatUser = user.convertToCommonUser()

        status = baseRepeat.convertToStatus()
        statusUser = baseRepeat.user.convertToCommonUser()

        if (baseRepeat.quotedStatus != null) {
            quotedStatus = baseRepeat.quotedStatus.convertToStatus()
            quotedStatusUser = baseRepeat.quotedStatus.user.convertToCommonUser()
        } else {
            quotedStatus = null
            quotedStatusUser = null
        }
    }

    return Post(
            id = id,
            status = status,
            user = statusUser,
            repeat = repeat,
            repeatedUser = repeatUser,
            quotedRepeatingStatus = quotedStatus,
            quotedRepeatingUser = quotedStatusUser
    )
}

private fun twitter4j.Status.convertToStatus(): Status {
    val parsedSource: Pair<String, Array<Link>?>? = source?.convertHtmlToContentAndLinks()
    val urls = if (symbolEntities.isEmpty()
            && hashtagEntities.isEmpty()
            && userMentionEntities.isEmpty()
            && mediaEntities.isEmpty()
            && urlEntities.isEmpty()
    ) {
        null
    } else {
        convertToContentAndLinks(
                text = text,
                symbolEntities = symbolEntities,
                hashtagEntities = hashtagEntities,
                userMentionEntities = userMentionEntities,
                mediaEntities = mediaEntities,
                urlEntities = urlEntities
        )

    }

    val mentions
            = userMentionEntities.takeIf { it.isNotEmpty() }?.map { it.screenName }?.toTypedArray()

    return Status(
            id = id,
            userId = user.id,
            text = urls?.first ?: text ?: "",
            sourceName = parsedSource?.first,
            sourceWebsite = parsedSource?.second?.firstOrNull()?.url,
            createdAt = createdAt,
            inReplyToStatusId = inReplyToStatusId,
            inReplyToUserId = inReplyToUserId,
            inReplyToScreenName = inReplyToScreenName,
            isFavorited = isFavorited,
            isRepeated = isRetweeted,
            favoriteCount = favoriteCount,
            repeatCount = retweetCount,
            repliesCount = 0,
            isSensitive = isPossiblySensitive,
            lang = lang,
            medias = mediaEntities.takeIf { it.isNotEmpty() }?.map {
                var downloadVideoUrl: String? = null
                var originalUrl: String? = null
                var type: String? = null

                when (it.type) {
                    "video" -> {
                        for (variant in it.videoVariants) {
                            if (variant.contentType == "application/x-mpegURL") {
                                originalUrl = variant.url
                                type = Media.MediaType.VIDEO_MULTI.value
                            } else if (variant.contentType == "video/mp4") {
                                downloadVideoUrl = variant.url
                            }
                        }

                        if (downloadVideoUrl == null) {
                            originalUrl = it.videoVariants[0].url
                            type = Media.MediaType.VIDEO_ONE.value
                        }
                    }
                    "animated_gif" -> {
                        originalUrl = it.videoVariants[0].url
                        type = Media.MediaType.GIF.value
                    }
                    else -> {
                        originalUrl = null
                        type = Media.MediaType.PICTURE.value
                    }
                }

                Media(
                        thumbnailUrl = if (originalUrl != null) {
                            it.mediaURLHttps
                        } else {
                            null
                        },
                        downloadVideoUrl = downloadVideoUrl,
                        originalUrl = originalUrl ?: it.mediaURLHttps,
                        mediaType = type
                                ?: Media.MediaType.PICTURE.value
                )
            }?.toTypedArray(),
            urls = urls?.second,
            mentions = mentions,
            emojis = null,
            url = "https://twitter.com/" + user.screenName + "/status/" + id.toString(),
            spoilerText = null,
            quotedStatusId = quotedStatusId,
            visibility = null,
            card = null
    )
}


private fun twitter4j.Status.convertToRepeat(): Repeat {
    return Repeat(
            id = id,
            userId = user.id,
            repeatedStatusId = retweetedStatus.id,
            createdAt = createdAt
    )
}