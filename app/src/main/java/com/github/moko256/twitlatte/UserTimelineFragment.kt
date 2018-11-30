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

import android.os.Bundle
import com.github.moko256.twitlatte.entity.Paging
import com.github.moko256.twitlatte.entity.Post

/**
 * Created by moko256 on 2016/06/30.
 *
 * @author moko256
 */
class UserTimelineFragment : BaseTweetListFragment(), ToolbarTitleInterface {

    private var userId: Long = -1L

    override val titleResourceId: Int
        get() = R.string.post

    override val cachedIdsDatabaseName: String
        get() = "UserTimeline_" + userId.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (userId == -1L) {
            userId = arguments!!.getLong("userId", -1L)
        }

        super.onCreate(savedInstanceState)
    }

    @Throws(Throwable::class)
    override fun getResponseList(paging: Paging): List<Post> {
        return client.apiClient.getUserTimeline(userId, paging)
    }

    companion object {

        fun newInstance(userId: Long): UserTimelineFragment {
            return UserTimelineFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                }
            }
        }
    }
}