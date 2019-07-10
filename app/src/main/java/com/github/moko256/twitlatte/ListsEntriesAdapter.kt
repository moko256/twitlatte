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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.moko256.latte.client.base.entity.ListEntry
import com.github.moko256.twitlatte.text.TwitterStringUtils.plusUserMarks
import io.reactivex.subjects.PublishSubject

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */

class ListsEntriesAdapter(private val context: Context, private val data: List<ListEntry>) : RecyclerView.Adapter<ListsEntriesAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val onClickObservable = PublishSubject.create<ListEntry>()

    override fun getItemId(position: Int): Long {
        return data[position].listId
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].description == null) {
            R.layout.layout_material_list_item_single_line
        } else {
            R.layout.layout_material_list_item_two_line
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(i, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val entry = data[i]

        viewHolder.title.text = plusUserMarks(entry.title, viewHolder.title, !entry.isPublic, false)
        viewHolder.description?.text = entry.description
        viewHolder.itemView.setOnClickListener {
            onClickObservable.onNext(entry)
        }

    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.primary_text)
        val description: TextView? = itemView.findViewById(R.id.secondary_text)
    }
}