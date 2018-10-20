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

package com.github.moko256.twitlatte.repository.server.impl

import com.github.moko256.twitlatte.converter.convertToPost
import com.github.moko256.twitlatte.entity.Post
import com.github.moko256.twitlatte.repository.server.base.StatusActionRepository
import twitter4j.Twitter

/**
 * Created by moko256 on 2018/10/20.
 *
 * @author moko256
 */
class TwitterStatusActionRepositoryImpl(private val twitter: Twitter): StatusActionRepository {
    override fun createFavorite(targetStatusId: Long): Post {
        return twitter.createFavorite(targetStatusId).convertToPost()
    }

    override fun removeFavorite(targetStatusId: Long): Post {
        return twitter.destroyFavorite(targetStatusId).convertToPost()
    }

    override fun createRepeat(targetStatusId: Long): Post {
        return twitter.retweetStatus(targetStatusId).convertToPost()
    }

    override fun removeRepeat(targetStatusId: Long): Post {
        return twitter.unRetweetStatus(targetStatusId).convertToPost()
    }
}