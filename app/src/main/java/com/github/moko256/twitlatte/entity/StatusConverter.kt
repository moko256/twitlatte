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

import com.github.moko256.twitlatte.text.link.MTHtmlParser
import com.github.moko256.twitlatte.text.link.convertToContentAndLinks
import com.google.android.exoplayer2.util.MimeTypes
import com.sys1yagi.mastodon4j.api.entity.Attachment
import twitter4j.Status as Twitter4jStatus

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
fun twitter4j.Status.convertToCommonStatus(): StatusObject {
    return if (!isRetweet) {
        val parsedSource = MTHtmlParser.convertToContentAndLinks(source)
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

        Status(
                id = id,
                userId = user.id,
                text = urls?.first?:text,
                sourceName = parsedSource.first,
                sourceWebsite = parsedSource.second.first().url,
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

                        when(it.type) {
                            "video" -> {
                                for (variant in it.videoVariants) {
                                    if (variant.contentType == MimeTypes.APPLICATION_M3U8) {
                                        originalUrl = variant.url
                                        type = Media.ImageType.VIDEO_MULTI.value
                                    } else if(variant.contentType == MimeTypes.VIDEO_MP4) {
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
                                originalUrl = originalUrl?:it.mediaURLHttps,
                                imageType = type?:Media.ImageType.PICTURE.value
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
    } else {
        Repeat(
                id = id,
                userId = user.id,
                repeatedStatusId = retweetedStatus.id,
                createdAt = createdAt
        )
    }
}

fun com.sys1yagi.mastodon4j.api.entity.Status.convertToCommonStatus(): StatusObject {
    val urls = MTHtmlParser.convertToContentAndLinks(text = content)

    return if (reblog == null) {
        Status(
                id = id,
                userId = account?.id?:-1,
                text = urls.first,
                sourceName = application?.name,
                sourceWebsite = application?.website,
                createdAt = ISO8601DateConverter.toDate(createdAt),
                inReplyToStatusId = inReplyToId?:-1,
                inReplyToUserId = inReplyToAccountId?:-1,
                inReplyToScreenName = if (inReplyToAccountId != null) {
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

                        when(it.type) {
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
    } else {
        Repeat(
                id = id,
                userId = account?.id?:-1,
                repeatedStatusId = reblog!!.id,
                createdAt = ISO8601DateConverter.toDate(createdAt)
        )
    }
}