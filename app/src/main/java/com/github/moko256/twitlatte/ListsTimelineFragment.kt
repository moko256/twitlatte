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
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.viewmodel.ListViewModel

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */
class ListsTimelineFragment : BaseTweetListFragment(), ToolbarStringTitleInterface {

    override val titleString by lazy { arguments?.getString("title", "") ?: "" }

    override val listRepository = object : ListViewModel.ListRepository() {
        var listId = 0L
        override fun onCreate(client: Client, bundle: Bundle) {
            super.onCreate(client, bundle)
            listId = bundle.getLong("listId")
        }

        override fun name() = "ListsTimeline_$listId"
        override fun request(paging: Paging) = client.apiClient.getListTimeline(listId, paging)
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