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
import com.github.moko256.latte.client.base.entity.Post

/**
 * Created by moko256 on 2016/07/27.
 *
 * @author moko256
 */

private const val BUNDLE_KEY_SEARCH_QUERY = "query"

class SearchResultFragment : BaseTweetListFragment() {

    private var searchText: String = ""

    override val cachedIdsDatabaseName: String
        get() = searchText.toByteArray().let { bytes ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        searchText = activity?.intent?.getStringExtra(BUNDLE_KEY_SEARCH_QUERY) ?: ""

        super.onCreate(savedInstanceState)
    }

    @Throws(Throwable::class)
    override fun request(paging: Paging): List<Post> {
        return if (searchText.isNotEmpty()) {
            client.apiClient.getPostByQuery(searchText, paging)
        } else {
            emptyList()
        }
    }

}