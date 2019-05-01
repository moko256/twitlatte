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

package com.github.moko256.twitlatte.view

import android.content.Context
import android.content.res.Resources
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment

fun oneDpToPx(@FloatRange(from = 1.0) density: Float): Int {
    val res = (density + 0.5f).toInt()
    if (res != 0) return res
    return 1
}

fun dpToPx(@IntRange(from = 1) dp: Int, density: Float): Int {
    val res = (dp * density + 0.5f).toInt()
    if (res != 0) return res
    return 1
}

fun Context.dpToPx(dp: Int) = dpToPx(dp, resources.displayMetrics.density)
fun Fragment.dpToPx(dp: Int) = dpToPx(dp, resources.displayMetrics.density)
fun Resources.dpToPx(dp: Int) = dpToPx(dp, displayMetrics.density)