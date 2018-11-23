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

import android.content.Context
import android.util.AttributeSet
import android.view.View
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
): RelativeLayout(context, attrs, defStyle) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthSpec)
        var heightSize = View.MeasureSpec.getSize(heightSpec)

        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)

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