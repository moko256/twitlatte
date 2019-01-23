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
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.latte.client.base.entity.Media
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.twitlatte.glide.GlideApp
import com.github.moko256.twitlatte.text.TwitterStringUtils

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

    private val dividerSize = Math.round(context.resources.displayMetrics.density * 4)
    private val glideRequest = GlideApp.with(this)

    private var medias : Array<Media>? = null
    private var containerViews : Array<ImageView> = Array(4) {
        val imageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            visibility = View.GONE
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        addView(imageView)
        return@Array imageView
    }

    fun setMedias(newMedias: Array<Media>) {
        if (newMedias !== medias) {
            val oldSize = medias?.size?:0
            val newSize = newMedias.size

            if (oldSize < newSize) {
                for (i in oldSize until newSize) {
                    containerViews[i].visibility = View.VISIBLE
                }
            } else if (newSize < oldSize) {
                for (i in newSize until oldSize) {
                    containerViews[i].visibility = View.GONE
                }
            }

            medias = newMedias
            invalidate()
            newMedias.forEachIndexed { index, media ->
                setMediaToView(media, containerViews[index])
            }
        }
    }

    private fun setMediaToView(media: Media, view: ImageView) {
        val url = if (media.thumbnailUrl == null) media.originalUrl else media.thumbnailUrl
        glideRequest
                .load(
                        TwitterStringUtils.convertSmallImageUrl(CLIENT_TYPE_TWITTER, url)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        medias?.let { medias ->
            for (i in 0 until medias.size) {
                val view = containerViews[i]
                val param = params[medias.size - 1][i]

                val width = measuredWidth
                val height = measuredHeight

                val childWidth = view.measuredWidth
                val childHeight = view.measuredHeight

                val left = generateChildPosition(width, childWidth, param[1])
                val top = generateChildPosition(height, childHeight, param[0])

                view.layout(left, top, left + childWidth, top + childHeight)
            }
        }
    }

    private fun generateChildPosition(parentSize: Int, childSize: Int, positionId: Int): Int {
        return if (positionId == 0) {
            0
        } else {
            parentSize - childSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = heightSize / 9 * 16
        } else if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = widthSize / 16 * 9
        }

        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(widthSize + paddingLeft + paddingRight, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize + paddingTop + paddingBottom, MeasureSpec.EXACTLY)
        )

        medias?.let { medias ->
            for (i in 0 until medias.size) {
                val param = params[medias.size - 1][i]
                val view = containerViews[i]
                view.measure(generateChildSpec(widthSize, param[3]), generateChildSpec(heightSize, param[2]))
            }
        }
    }

    private fun generateChildSpec(parentSize: Int, spanSize: Int): Int {
        return MeasureSpec.makeMeasureSpec(
                if (spanSize == 1) {
                    (parentSize - dividerSize) / 2
                } else {
                    parentSize
                },
                MeasureSpec.EXACTLY
        )
    }
}