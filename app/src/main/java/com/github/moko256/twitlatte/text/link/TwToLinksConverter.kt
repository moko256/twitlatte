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
import twitter4j.Status
import twitter4j.URLEntity

/**
 * Created by moko256 on 2018/08/31.
 *
 * @author moko256
 */
object TwToLinksConverter {

    fun convertToContentAndLinks(status: Status): Pair<CharSequence, List<Link>> {
        val links = ArrayList<Link>(6)
        val stringBuilder = StringBuilder(300)

        stringBuilder.setLength(0)
        for (symbolEntity in status.symbolEntities) {
            links.add(Link(
                    "twitlatte://symbol" + symbolEntity.text,
                    symbolEntity.start,
                    symbolEntity.end
            ))
        }

        for (hashtagEntity in status.hashtagEntities) {
            links.add(Link(
                    "twitlatte://hashtag" + hashtagEntity.text,
                    hashtagEntity.start,
                    hashtagEntity.end
            ))
        }

        for (userMentionEntity in status.userMentionEntities) {
            links.add(Link(
                    "twitlatte://user" + userMentionEntity.text,
                    userMentionEntity.start,
                    userMentionEntity.end
            ))
        }

        val hasMedia = status.mediaEntities.isNotEmpty()
        val urlEntities = ArrayList<URLEntity>(status.urlEntities.size + if (hasMedia) 1 else 0)
        urlEntities.addAll(status.urlEntities.asList())
        if (hasMedia) {
            urlEntities.add(status.mediaEntities[0])
        }

        val tweetLength = status.text.length
        var sp = 0

        for (entity in urlEntities) {
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
}