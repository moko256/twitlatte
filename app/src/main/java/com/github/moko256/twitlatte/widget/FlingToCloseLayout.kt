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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.max
import kotlin.math.min

/**
 * Created by moko256 on 2020/02/08.
 *
 * @author moko256
 */
class FlingToCloseLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val callback = Callback(this)
    private var helper: ViewDragHelper? = null

    var onTouchedListener: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var canDrag = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        helper = ViewDragHelper.create(this, callback)

    }

    override fun onDetachedFromWindow() {
        helper = null
        super.onDetachedFromWindow()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent) =
        helper?.shouldInterceptTouchEvent(ev) ?: false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        helper?.processTouchEvent(event)
        onTouchedListener?.invoke()
        return true
    }

    override fun computeScroll() {
        if (helper?.continueSettling(true) == true) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }


    private inner class Callback(
        private val parent: ViewGroup
    ) : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int) = canDrag
        override fun getViewVerticalDragRange(child: View) = 1

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val halfD = parent.measuredHeight / 2
            val halfC = child.measuredHeight / 2

            return if (top + halfC <= halfD) {
                max(top, (halfD * (1 - rangeT)).toInt() - halfC)
            } else {
                min(top, (halfD * (1 + rangeT)).toInt() - halfC)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top = releasedChild.top
            val halfD = parent.measuredHeight / 2
            val halfC = releasedChild.measuredHeight / 2

            if (
                if (top + halfC <= halfD) {
                    (top <= (halfD * (1 - rangeT)).toInt() - halfC)
                } else {
                    (top >= (halfD * (1 + rangeT)).toInt() - halfC)
                }
            ) {
                onClose?.invoke()
            } else {
                helper?.smoothSlideViewTo(
                    releasedChild,
                    0,
                    (parent.measuredHeight - releasedChild.measuredHeight) / 2
                )
                parent.invalidate()
            }
        }
    }

    private companion object {
        private const val rangeT = 0.5
    }
}