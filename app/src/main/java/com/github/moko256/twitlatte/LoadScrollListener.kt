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

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Created by moko256 on 2016/06/05.
 *
 * @author moko256
 */
class LoadScrollListener(
    private val layoutManager: RecyclerView.LayoutManager,
    private val onLoadListener: OnLoadListener
) : RecyclerView.OnScrollListener() {

    private var previousTotal = 0
    private var loading = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem =
            if (layoutManager is StaggeredGridLayoutManager) {
                layoutManager.findFirstVisibleItemPositions(null)[0]
            } else {
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
            onLoadListener.onBottomLoad()
            loading = true
        }

    }

    interface OnLoadListener {
        fun onBottomLoad()
    }

}