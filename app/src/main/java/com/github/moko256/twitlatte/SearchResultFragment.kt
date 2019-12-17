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
 * Created by moko256 on 2016/07/27.
 *
 * @author moko256
 */

private const val BUNDLE_KEY_SEARCH_QUERY = "query"

class SearchResultFragment : BaseTweetListFragment() {

    override val listRepository = object : ListViewModel.ListRepository() {
        private var searchText: String = ""
        override fun onCreate(client: Client, bundle: Bundle) {
            super.onCreate(client, bundle)
            val query = bundle.getString(BUNDLE_KEY_SEARCH_QUERY)
            if (query != null) {
                searchText = query
            }
        }

        override fun name() = searchText.toByteArray().let { bytes ->
            StringBuilder("Search_").apply {
                bytes.forEach {
                    if (it < 0) {
                        append('_').append((-it).toString())
                    } else {
                        append(it.toString())
                    }
                }
            }.toString()
        }

        override fun request(paging: Paging) = if (searchText.isNotEmpty()) {
            client.apiClient.getPostByQuery(searchText, paging)
        } else {
            emptyList()
        }
    }

}