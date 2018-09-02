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

package com.github.moko256.twicalico.entity

import twitter4j.URLEntity
import twitter4j.UserMentionEntity
import twitter4j.Status as Twitter4jStatus

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
class EntityConverter {

    fun convert(status: Twitter4jStatus): Status{
        return Status(
                id = status.id,
                userId = status.user.id,
                text = status.text,
                source = status.source,
                createdAt = status.createdAt.time,
                repeatStatusId = status.retweetedStatus?.id,
                inReplyToStatusId = convertT4JLong(status.inReplyToStatusId),
                inReplyToUserId = convertT4JLong(status.inReplyToUserId),
                inReplyToScreenName = status.inReplyToScreenName,
                isFavorited = status.isFavorited,
                isRepeated = status.isRetweeted,
                favoriteCount = status.favoriteCount,
                repeatCount = status.retweetCount,
                isSensitive = status.isPossiblySensitive,
                lang = status.lang,
                userMentions = convert(status.userMentionEntities),
                urls = convert(status.urlEntities)
        )
    }

    fun convertT4JLong(l: Long?): Long?{
        return if (l == -1L){
            null
        } else {
            l
        }
    }

    fun convert(entities: Array<URLEntity>): Array<Pair<String, IntRange>>{
        var r = emptyList<Pair<String, IntRange>>()
        entities.forEach {
            r += Pair(it.url, it.start..it.end)
        }
        return r.toTypedArray()
    }

    fun convert(entities: Array<UserMentionEntity>): Array<Pair<String, IntRange>>{
        var r = emptyList<Pair<String, IntRange>>()
        entities.forEach {
            r += Pair(it.screenName, it.start..it.end)
        }
        return r.toTypedArray()
    }
}