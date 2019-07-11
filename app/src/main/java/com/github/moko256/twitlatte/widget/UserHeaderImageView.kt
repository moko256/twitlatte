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
import androidx.appcompat.widget.AppCompatImageView

/**
 * Created by moko256 on 2017/07/15.
 *
 * @author moko256
 */

class UserHeaderImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    var widthPerHeight: Int = 1

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modifiedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(widthMeasureSpec),
                View.MeasureSpec.EXACTLY
        )
        val modifiedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(widthMeasureSpec) / widthPerHeight,
                View.MeasureSpec.EXACTLY
        )
        setMeasuredDimension(modifiedWidthMeasureSpec, modifiedHeightMeasureSpec)
    }
}