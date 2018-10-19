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

import com.github.moko256.mastodon.MTStatus
import com.github.moko256.twitlatte.entity.*
import com.github.moko256.twitlatte.text.link.MTHtmlParser
import com.github.moko256.twitlatte.text.link.convertToContentAndLinks
import com.github.moko256.twitlatte.text.link.entity.Link
import com.google.android.exoplayer2.util.MimeTypes
import com.sys1yagi.mastodon4j.api.entity.Attachment

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
fun twitter4j.Status.convertToPost(): Post {
    return if (this is MTStatus) {
        this.status.convertToCommonStatus()
    } else {
        this.convertToCommonStatus()
    }
}


fun twitter4j.Status.convertToCommonStatus(): Post {
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
    val parsedSource: Pair<String, Array<Link>>? = if (source == null) {
        null
    } else {
        MTHtmlParser.convertToContentAndLinks(source)
    }
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
            = if (userMentionEntities.isNotEmpty()) {
        userMentionEntities.map {
            it.screenName
        }.toTypedArray()
    } else {
        null
    }

    return Status(
            id = id,
            userId = user.id,
            text = urls?.first ?: text ?: "",
            sourceName = parsedSource?.first,
            sourceWebsite = parsedSource?.second?.first()?.url,
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
            medias = if (mediaEntities.isNotEmpty()) {
                mediaEntities.map {
                    var downloadVideoUrl: String? = null
                    var originalUrl: String? = null
                    var type: String? = null

                    when (it.type) {
                        "video" -> {
                            for (variant in it.videoVariants) {
                                if (variant.contentType == MimeTypes.APPLICATION_M3U8) {
                                    originalUrl = variant.url
                                    type = Media.ImageType.VIDEO_MULTI.value
                                } else if (variant.contentType == MimeTypes.VIDEO_MP4) {
                                    downloadVideoUrl = variant.url
                                }
                            }

                            if (downloadVideoUrl == null) {
                                originalUrl = it.videoVariants[0].url
                                type = Media.ImageType.VIDEO_ONE.value
                            }
                        }
                        "animated_gif" -> {
                            originalUrl = it.videoVariants[0].url
                            type = Media.ImageType.GIF.value
                        }
                        else -> {
                            originalUrl = null
                            type = Media.ImageType.PICTURE.value
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
                            imageType = type
                                    ?: Media.ImageType.PICTURE.value
                    )
                }.toTypedArray()
            } else {
                null
            },
            urls = urls?.second,
            mentions = mentions,
            emojis = null,
            url = "https://twitter.com/" + user.screenName + "/status/" + id.toString(),
            spoilerText = null,
            quotedStatusId = quotedStatusId,
            visibility = null
    )
}

fun twitter4j.Status.convertToStatusOrRepeat(): StatusObject {
    return if (retweetedStatus != null) {
        convertToRepeat()
    } else {
        convertToStatus()
    }
}


private fun twitter4j.Status.convertToRepeat(): Repeat {
    return Repeat(
            id = id,
            userId = user.id,
            repeatedStatusId = retweetedStatus.id,
            createdAt = createdAt
    )
}

fun com.sys1yagi.mastodon4j.api.entity.Status.convertToCommonStatus(): Post {
    val status: Status
    val statusUser: User
    val repeat: Repeat?
    val repeatUser: User?

    val baseRepeat = reblog
    if (baseRepeat == null) {
        status = convertToStatus()
        statusUser = account.convertToCommonUser()
        repeat = null
        repeatUser = null
    } else {
        repeat = convertToRepeat()
        repeatUser = account.convertToCommonUser()

        status = baseRepeat.convertToStatus()
        statusUser = baseRepeat.account.convertToCommonUser()
    }

    return Post(
            id = id,
            status = status,
            user = statusUser,
            repeat = repeat,
            repeatedUser = repeatUser
    )
}

private fun com.sys1yagi.mastodon4j.api.entity.Status.convertToStatus(): Status {
    val urls = MTHtmlParser.convertToContentAndLinks(text = content)

    return Status(
            id = id,
            userId = account.id,
            text = urls.first,
            sourceName = application?.name,
            sourceWebsite = application?.website,
            createdAt = ISO8601DateConverter.toDate(createdAt),
            inReplyToStatusId = inReplyToId.convertIfZero(),
            inReplyToUserId = inReplyToAccountId.convertIfZero(),
            inReplyToScreenName = if (inReplyToAccountId != 0L) {
                ""
            } else {
                null
            },
            isFavorited = isFavourited,
            isRepeated = isReblogged,
            favoriteCount = favouritesCount,
            repeatCount = reblogsCount,
            repliesCount = repliesCount,
            isSensitive = isSensitive,
            lang = language,
            medias = if (mediaAttachments.isNotEmpty()) {
                mediaAttachments.map {
                    val resultUrl = it.url
                    val thumbnailUrl: String?
                    val type: Media.ImageType

                    when (it.type) {
                        Attachment.Type.Video.value -> {
                            thumbnailUrl = it.previewUrl
                            type = Media.ImageType.VIDEO_ONE
                        }
                        Attachment.Type.Gifv.value -> {
                            thumbnailUrl = it.previewUrl
                            type = Media.ImageType.GIF
                        }
                        else -> {
                            thumbnailUrl = null
                            type = Media.ImageType.PICTURE
                        }
                    }

                    Media(
                            thumbnailUrl = thumbnailUrl,
                            originalUrl = resultUrl,
                            imageType = type.value
                    )
                }.toTypedArray()
            } else {
                null
            },
            urls = urls.second,
            mentions = mentions.map {
                it.acct
            }.toTypedArray(),
            emojis = if (emojis.isNotEmpty()) {
                emojis.map {
                    Emoji(url = it.url, shortCode = it.shortcode)
                }.toTypedArray()
            } else {
                null
            },
            url = url,
            spoilerText = if (spoilerText.isNotEmpty()) {
                spoilerText
            } else {
                null
            },
            quotedStatusId = -1,
            visibility = visibility
    )
}

private fun com.sys1yagi.mastodon4j.api.entity.Status.convertToRepeat(): Repeat {
    return Repeat(
            id = id,
            userId = account.id,
            repeatedStatusId = reblog!!.id,
            createdAt = ISO8601DateConverter.toDate(createdAt)
    )
}

private fun Long.convertIfZero() = if (this == 0L){
    -1
} else {
    this
}