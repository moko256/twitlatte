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

package com.github.moko256.twitlatte

import com.github.moko256.twitlatte.entity.Paging
import com.github.moko256.twitlatte.entity.Post

/**
 * Created by moko256 on 2016/03/23.
 *
 * @author moko256
 */
class HomeTimeLineFragment : BaseTweetListFragment(), ToolbarTitleInterface, NavigationPositionInterface {

    override val titleResourceId = R.string.timeline

    override val navigationPosition = R.id.nav_timeline

    override val cachedIdsDatabaseName = "HomeTimeline"

    @Throws(Throwable::class)
    override fun getResponseList(paging: Paging): List<Post> {
        return client.apiClient.getHomeTimeline(paging)
    }
}