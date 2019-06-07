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
import android.widget.RelativeLayout

/**
 * Created by moko256 on 2017/03/05.
 *
 * @author moko256
 */

class WideRatioRelativeLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val heightSize: Int
        val widthSize: Int

        when {
            MeasureSpec.getMode(heightSpec) != MeasureSpec.EXACTLY -> {
                widthSize = MeasureSpec.getSize(widthSpec)
                heightSize = widthSize / 16 * 9
            }
            MeasureSpec.getMode(widthSpec) != MeasureSpec.EXACTLY -> {
                heightSize = MeasureSpec.getSize(heightSpec)
                widthSize = heightSize / 9 * 16
            }
            else -> {
                widthSize = MeasureSpec.getSize(widthSpec)
                heightSize = MeasureSpec.getSize(heightSpec)
            }
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(widthSize + paddingLeft + paddingRight, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize + paddingTop + paddingBottom, MeasureSpec.EXACTLY)
        )
    }

}