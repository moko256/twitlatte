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
        val heightSize: Int
        val widthSize: Int

        if (View.MeasureSpec.getMode(heightSpec) != View.MeasureSpec.EXACTLY) {
            widthSize = View.MeasureSpec.getSize(widthSpec)
            heightSize = widthSize / 16 * 9
        } else if (View.MeasureSpec.getMode(widthSpec) != View.MeasureSpec.EXACTLY) {
            heightSize = View.MeasureSpec.getSize(heightSpec)
            widthSize = heightSize / 9 * 16
        } else {
            widthSize = View.MeasureSpec.getSize(widthSpec)
            heightSize = View.MeasureSpec.getSize(heightSpec)
        }

        super.onMeasure(
                View.MeasureSpec.makeMeasureSpec(widthSize + paddingLeft + paddingRight, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightSize + paddingTop + paddingBottom, View.MeasureSpec.EXACTLY)
        )
    }

}