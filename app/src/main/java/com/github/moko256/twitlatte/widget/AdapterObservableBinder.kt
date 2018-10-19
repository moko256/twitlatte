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

package com.github.moko256.twitlatte.widget

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.moko256.twitlatte.entity.EventType
import com.github.moko256.twitlatte.entity.UpdateEvent

/**
 * Created by moko256 on 2018/10/11.
 *
 * @author moko256
 */
fun RecyclerView.convertObservableConsumer(): (UpdateEvent) -> Unit = {
    when (it.type) {
        EventType.ADD_FIRST -> adapter!!.notifyDataSetChanged()

        EventType.ADD_TOP -> adapter!!.notifyItemRangeInserted(it.position, it.size)

        EventType.ADD_BOTTOM -> adapter!!.notifyItemRangeInserted(it.position, it.size)

        EventType.REMOVE -> adapter!!.notifyItemRangeRemoved(it.position, it.size)

        EventType.INSERT -> {
            val startView = layoutManager!!.findViewByPosition(it.position)
            val offset = if (startView == null) {
                0
            } else {
                startView.top - paddingTop
            }

            val layoutManager = layoutManager
            if (layoutManager is LinearLayoutManager) {
                adapter!!.notifyItemRangeInserted(it.position, it.size)
                layoutManager.scrollToPositionWithOffset(it.position + it.size, offset)
            } else {
                (layoutManager as StaggeredGridLayoutManager).scrollToPositionWithOffset(it.position + it.size, offset)
                adapter!!.notifyItemRangeChanged(it.position, it.size)
            }
        }

        EventType.UPDATE -> adapter!!.notifyItemRangeChanged(it.position, it.size)

        else -> {

        }
    }
}