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
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

/**
 * Created by moko256 on 2017/03/04.
 *
 * @author moko256
 */
class UserLikeFragment : BaseTweetListFragment(), ToolbarTitleInterface {

    private var userId: Long = -1L

    override val titleResourceId: Int
        get() = R.string.like

    override val cachedIdsDatabaseName: String
        get() = "UserLike_" + userId.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (userId == -1L) {
            userId = arguments!!.getLong("userId", -1L)
        }

        super.onCreate(savedInstanceState)
    }

    @Throws(TwitterException::class)
    override fun getResponseList(paging: Paging): ResponseList<Status> {
        return client.twitter.getFavorites(userId, paging)
    }

    companion object {

        fun newInstance(userId: Long): UserLikeFragment {
            return UserLikeFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                }
            }
        }
    }
}