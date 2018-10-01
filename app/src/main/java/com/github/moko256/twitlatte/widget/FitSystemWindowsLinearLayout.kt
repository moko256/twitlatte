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
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat


/**
 * Created by moko256 on 2018/08/16.
 *
 * @author moko256
 */
class FitSystemWindowsLinearLayout: LinearLayoutCompat {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->

            if (insets.hasInsets()) {

                insets.stableInsetLeft

                val params = v.layoutParams as ViewGroup.MarginLayoutParams

                params.topMargin = insets.systemWindowInsetTop
                params.bottomMargin = insets.systemWindowInsetBottom
                params.leftMargin = insets.systemWindowInsetLeft
                params.rightMargin = insets.systemWindowInsetRight

                insets.consumeSystemWindowInsets()
            } else {
                insets
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)



}