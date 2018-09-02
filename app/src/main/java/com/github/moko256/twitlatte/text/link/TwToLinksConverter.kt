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

package com.github.moko256.twitlatte.text.link

import com.github.moko256.twitlatte.text.link.entity.Link
import twitter4j.*

/**
 * Created by moko256 on 2018/08/31.
 *
 * @author moko256
 */
fun convertToContentAndLinks(
        text: String,
        symbolEntities: Array<SymbolEntity>,
        hashtagEntities: Array<HashtagEntity>,
        userMentionEntities: Array<UserMentionEntity>,
        mediaEntities: Array<MediaEntity>,
        urlEntities: Array<URLEntity>
): Pair<CharSequence, List<Link>> {
    val links = ArrayList<Link>(6)
    val stringBuilder = StringBuilder(300)

    stringBuilder.setLength(0)
    for (symbolEntity in symbolEntities) {
        links.add(Link(
                "twitlatte://symbol" + symbolEntity.text,
                symbolEntity.start,
                symbolEntity.end
        ))
    }

    for (hashtagEntity in hashtagEntities) {
        links.add(Link(
                "twitlatte://hashtag" + hashtagEntity.text,
                hashtagEntity.start,
                hashtagEntity.end
        ))
    }

    for (userMentionEntity in userMentionEntities) {
        links.add(Link(
                "twitlatte://user" + userMentionEntity.text,
                userMentionEntity.start,
                userMentionEntity.end
        ))
    }

    val hasMedia = mediaEntities.isNotEmpty()
    val mediaAndUrlEntities = ArrayList<URLEntity>(urlEntities.size + if (hasMedia) 1 else 0)
    mediaAndUrlEntities.addAll(urlEntities.asList())
    if (hasMedia) {
        mediaAndUrlEntities.add(mediaEntities[0])
    }

    val tweetLength = text.length
    var sp = 0

    for (entity in mediaAndUrlEntities) {
        val url = entity.url
        val displayUrl = entity.displayURL

        val urlLength = url.length
        val displayUrlLength = displayUrl.length

        var start = entity.start
        var end = entity.end

        if (start <= tweetLength && end <= tweetLength) {
            val dusp = displayUrlLength - urlLength

            start += sp
            end += sp

            stringBuilder.replace(start, end, displayUrl)
            links.add(Link(
                    entity.expandedURL,
                    start,
                    end + dusp
            ))

            sp += dusp
        }
    }

    return stringBuilder to links
}