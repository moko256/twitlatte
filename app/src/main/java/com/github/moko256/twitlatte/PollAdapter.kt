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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.moko256.latte.client.base.entity.Poll

/**
 * Created by moko256 on 2019/03/07.
 *
 * @author moko256
 */
class PollAdapter(private val context: Context): RecyclerView.Adapter<Holder>() {

    private var poll: Poll? = null

    fun setPoll(poll: Poll) {
        selections.clear()
        this.poll = poll
        notifyDataSetChanged()
    }

    val selections = ArrayList<Int>(4)

    override fun getItemViewType(position: Int): Int {
        return poll?.let { poll ->
            if (poll.expired) {
                R.layout.layout_material_list_item_single_line
            } else {
                if (poll.multiple) {
                    R.layout.layout_material_list_item_one_line_checkbox
                } else {
                    R.layout.layout_material_list_item_one_line_radiobutton
                }
            }
        }?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(LayoutInflater.from(context)
                .inflate(viewType, parent, false) as ViewGroup
        )

        val poll = poll
        if(poll != null && !poll.expired) {
            holder.itemView.setOnClickListener {
                val position = holder.layoutPosition

                if (poll.multiple) {
                    if (selections.contains(position)) {
                        selections.remove(position)
                    } else {
                        selections.add(position)
                    }
                    notifyItemChanged(position)
                } else {
                    val notSame = !selections.contains(position)
                    selections.clear()
                    if (notSame) {
                        selections.add(position)
                    }
                    notifyItemRangeChanged(0, poll.optionTitles.size)
                }
            }
        }

        return holder
    }

    override fun getItemCount(): Int {
        return poll?.optionTitles?.size?:0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        poll?.let { poll ->
            holder.bind(
                    poll.optionTitles[position],
                    ((1000 * poll.optionCounts[position] / poll.votesCount) / 10f).toString(),
                    selections.contains(position)
            )
        }
    }
}

class Holder(itemView: ViewGroup): RecyclerView.ViewHolder(itemView) {
    private val textView = itemView.findViewById<TextView>(R.id.primary_text)!!
    private val selection = itemView.findViewById<CompoundButton>(R.id.selection)

    @SuppressLint("SetTextI18n")
    fun bind(text: String, percentage: String, isSelected: Boolean) {
        textView.text = "$percentage%  $text"
        selection?.isChecked = isSelected
    }
}