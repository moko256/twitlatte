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

package com.github.moko256.twitlatte.api.twitter

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
): Pair<String, Array<Link>> {
    val links = ArrayList<Link>(6)
    val stringBuilder = StringBuilder(text)

    val entities = ArrayList<Pair<String, TweetEntity>>(
            symbolEntities.size
                    + hashtagEntities.size
                    + userMentionEntities.size
                    + urlEntities.size
                    + 1
    )
    entities.addAll(symbolEntities.map { "symbol" to it })
    entities.addAll(hashtagEntities.map { "hashtag" to it })
    entities.addAll(userMentionEntities.map { "user" to it })
    entities.addAll(urlEntities.map { "url" to it })
    mediaEntities.firstOrNull()?.let {
        entities.add("url" to it)
    }

    entities.sortBy { it.second.start }

    var sp = 0

    entities.forEach {
        val start = it.second.start + sp
        val end = it.second.end + sp

        if (it.first == "url") {
            val url = (it.second as URLEntity).url
            val displayUrl = (it.second as URLEntity).displayURL

            val urlLength = url.length
            val displayUrlLength = displayUrl.length

            val dusp = displayUrlLength - urlLength

            val nowLength = stringBuilder.length
            if (start >= nowLength || end > nowLength) {
                stringBuilder.append(CharArray(end - nowLength) { ' ' })
            }

            stringBuilder.replace(start, end, displayUrl)
            links.add(Link(
                    (it.second as URLEntity).expandedURL,
                    start,
                    end + dusp
            ))

            sp += dusp
        } else {
            links.add(Link(
                    "twitlatte://${it.first}/" + it.second.text,
                    start,
                    end
            ))
        }

    }

    return stringBuilder.toString() to links.toTypedArray()
}