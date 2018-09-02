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
    val parsedSource = MTHtmlParser.convertToContentAndLinks(source)
    val urls = if (symbolEntities.isNotEmpty()
            && hashtagEntities.isNotEmpty()
            && userMentionEntities.isNotEmpty()
            && mediaEntities.isNotEmpty()
            && urlEntities.isNotEmpty()
            ) {
        convertToContentAndLinks(
                text = text,
                symbolEntities = symbolEntities,
                hashtagEntities = hashtagEntities,
                userMentionEntities = userMentionEntities,
                mediaEntities = mediaEntities,
                urlEntities = urlEntities
        )
    } else {
        null
    }

    return if (retweetedStatus == null) {
        Status(
                id = id,
                userId = user.id,
                text = urls?.first?:text,
                sourceName = parsedSource.first,
                sourceWebsite = parsedSource.second.first().href,
                createdAt = createdAt.time,
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
                        var resultUrl: String? = null
                        var type: Media.ImageType? = null

                        when(it.type) {
                            "video" -> {
                                for (variant in it.videoVariants) {
                                    if (variant.contentType == MimeTypes.APPLICATION_M3U8) {
                                        resultUrl = variant.url
                                        type = Media.ImageType.VIDEO_MULTI
                                    }
                                }

                                if (resultUrl == null) {
                                    resultUrl = it.videoVariants[0].url
                                    type = Media.ImageType.VIDEO_ONE
                                }
                            }
                            "animated_gif" -> {
                                resultUrl = it.videoVariants[0].url
                                type = Media.ImageType.GIF
                            }
                            else -> {
                                resultUrl = it.mediaURLHttps
                                type = Media.ImageType.PICTURE
                            }
                        }

                        Media(
                                url = resultUrl?:it.mediaURLHttps,
                                imageType = type?:Media.ImageType.PICTURE
                        )
                    }
                } else {
                    null
                },
                urls = urls?.second,
                emojis = null,
                url = "https://twitter.com/" + user.screenName + "/status/" + id.toString(),
                spoilerText = null,
                quotedStatusId = quotedStatusId,
                visibility = null
        )
    } else {
        Retweet(
                id = id,
                userId = user.id,
                repeatStatusId = retweetedStatus.id,
                createdAt = createdAt.time
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
                createdAt = ISO8601DateConverter.parseDate(createdAt).time,
                inReplyToStatusId = inReplyToId?:-1,
                inReplyToUserId = inReplyToAccountId?:-1,
                inReplyToScreenName = "",
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
                        val type = when(it.type) {
                            Attachment.Type.Video.value -> {
                                Media.ImageType.VIDEO_ONE
                            }
                            Attachment.Type.Gifv.value -> {
                                Media.ImageType.GIF
                            }
                            else -> {
                                Media.ImageType.PICTURE
                            }
                        }

                        Media(
                                url = resultUrl,
                                imageType = type
                        )
                    }
                } else {
                    null
                },
                urls = urls.second,
                emojis = if (emojis.isNotEmpty()) {
                    emojis.map {
                        Emoji(url = it.url, shortCode = it.shortcode)
                    }
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
        Retweet(
                id = id,
                userId = account?.id?:-1,
                repeatStatusId = reblog!!.id,
                createdAt = ISO8601DateConverter.parseDate(createdAt).time
        )
    }
}