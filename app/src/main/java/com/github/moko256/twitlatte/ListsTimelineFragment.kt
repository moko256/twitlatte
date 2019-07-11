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
import com.github.moko256.latte.client.base.entity.ListEntry
import com.github.moko256.latte.client.base.entity.Paging
import com.github.moko256.latte.client.base.entity.Post

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */
class ListsTimelineFragment : BaseTweetListFragment(), ToolbarStringTitleInterface {

    private var listId = -1L
    private lateinit var title: String

    override val titleString: String
        get() = title

    override val cachedIdsDatabaseName: String
        get() = "ListsTimeline_$listId"

    override fun onCreate(savedInstanceState: Bundle?) {
        if (listId == -1L) {
            val arguments = arguments!!
            listId = arguments.getLong("listId", -1L)
            title = arguments.getString("title", "")
        }

        super.onCreate(savedInstanceState)
    }

    override fun request(paging: Paging): List<Post> {
        return client.apiClient.getListTimeline(listId, paging)
    }

    companion object {
        fun newInstance(listEntry: ListEntry): ListsTimelineFragment {
            return ListsTimelineFragment().apply {
                arguments = Bundle().apply {
                    putLong("listId", listEntry.listId)
                    putString("title", listEntry.title)
                }
            }
        }
    }
}