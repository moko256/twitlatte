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

package com.github.moko256.twitlatte.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created by moko256 on 2019/01/21.
 */

/* {row,column,rowSpan,colSpan} */
private val params = arrayOf(
        arrayOf(intArrayOf(0, 0, 2, 2), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)),
        arrayOf(intArrayOf(0, 0, 2, 1), intArrayOf(0, 1, 2, 1), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)),
        arrayOf(intArrayOf(0, 0, 2, 1), intArrayOf(0, 1, 1, 1), intArrayOf(1, 1, 1, 1), intArrayOf(0, 0, 0, 0)),
        arrayOf(intArrayOf(0, 0, 1, 1), intArrayOf(0, 1, 1, 1), intArrayOf(1, 0, 1, 1), intArrayOf(1, 1, 1, 1))
)

class TweetImageTableView2 @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != View.MeasureSpec.EXACTLY) {
            widthSize = heightSize / 9 * 16
        } else if (heightMode != View.MeasureSpec.EXACTLY) {
            heightSize = widthSize / 16 * 9
        }

        super.onMeasure(
                View.MeasureSpec.makeMeasureSpec(widthSize + paddingLeft + paddingRight, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightSize + paddingTop + paddingBottom, View.MeasureSpec.EXACTLY)
        )
    }
}