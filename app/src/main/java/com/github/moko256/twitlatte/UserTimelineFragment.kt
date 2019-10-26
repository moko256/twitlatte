/*
 * Copyright 2015-2019 The twitlatte authors
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
import com.github.moko256.latte.client.base.entity.Paging
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.viewmodel.ListViewModel

/**
 * Created by moko256 on 2016/06/30.
 *
 * @author moko256
 */
class UserTimelineFragment : BaseTweetListFragment(), ToolbarTitleInterface {

    override val titleResourceId = R.string.post

    override val listRepository = object : ListViewModel.ListRepository() {
        var userId = 0L
        override fun onCreate(client: Client, bundle: Bundle) {
            super.onCreate(client, bundle)
            userId = bundle.getLong("userId")
        }

        override fun name() = "UserTimeline_$userId"
        override fun request(paging: Paging) = client.apiClient.getUserTimeline(userId, paging)
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