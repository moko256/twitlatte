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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.latte.client.base.CLIENT_TYPE_NOTHING
import com.github.moko256.latte.client.base.entity.Media
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.ShowMediasActivity
import com.github.moko256.twitlatte.glide.GlideRequests
import com.github.moko256.twitlatte.preferenceRepository
import com.github.moko256.twitlatte.repository.KEY_HIDE_SENSITIVE_MEDIA
import com.github.moko256.twitlatte.text.TwitterStringUtils
import jp.wasabeef.glide.transformations.BlurTransformation

/**
 * Created by moko256 on 2019/01/21.
 */

class ImagesTableView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private companion object {
        /* {row,column,rowSpan,colSpan} */
        private val a0000 = intArrayOf(0, 0, 0, 0)
        private val a0111 = intArrayOf(0, 1, 1, 1)
        private val a1111 = intArrayOf(1, 1, 1, 1)
        private val a0021 = intArrayOf(0, 0, 2, 1)

        private val params = arrayOf(
                arrayOf(intArrayOf(0, 0, 2, 2), a0000,                  a0000,                  a0000),
                arrayOf(a0021,                  intArrayOf(0, 1, 2, 1), a0000,                  a0000),
                arrayOf(a0021,                  a0111,                  a1111,                  a0000),
                arrayOf(intArrayOf(0, 0, 1, 1), a0111,                  intArrayOf(1, 0, 1, 1), a1111)
        )
    }

    private val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_play_arrow_white_24dp)
    private val gifMark = AppCompatResources.getDrawable(context, R.drawable.ic_gif_white_24dp)

    private val dp = context.resources.displayMetrics.density
    private val dividerSize = Math.round(4 * dp)
    private val markSize = Math.round(48 * dp)

    private val imageFilter = PorterDuffColorFilter(0x33000000, PorterDuff.Mode.SRC_ATOP)
    private val maxMediaSize = 4

    lateinit var glideRequests: GlideRequests

    private var clientType = CLIENT_TYPE_NOTHING

    private var isOpen = true

    private lateinit var imageLoadMode: String

    private var medias : Array<Media>? = null
    private var mediaSize = 0

    private var containerViews : Array<FrameLayout> = Array(maxMediaSize) { index ->
        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val playButton = ImageView(context)
        val playButtonParams = FrameLayout.LayoutParams(markSize, markSize)
        playButtonParams.gravity = Gravity.CENTER
        playButton.layoutParams = playButtonParams
        playButton.setImageDrawable(drawable)

        val markImage = ImageView(context)
        val markImageParams = FrameLayout.LayoutParams(markSize, markSize)
        markImageParams.gravity = Gravity.BOTTOM or Gravity.START
        markImage.layoutParams = markImageParams
        markImage.setImageDrawable(gifMark)

        val container = FrameLayout(context)
        container.visibility = View.GONE
        container.addView(imageView)
        container.addView(playButton)
        container.addView(markImage)
        container.setOnLongClickListener { this@ImagesTableView.performLongClick() }

        container.setOnClickListener {
            medias?.let { medias ->
                if (isOpen){
                    getContext().startActivity(ShowMediasActivity.getIntent(getContext(), medias, clientType, index))
                } else {
                    isOpen = true
                    updateImages(medias)
                }
            }
        }

        addView(container)

        return@Array container
    }

    fun setMedias(newMedias: Array<Media>, clientType: Int, sensitive: Boolean, imageLoadMode: String) {
        this.imageLoadMode = imageLoadMode
        this.clientType = clientType
        isOpen = imageLoadMode != "none"
                && !(sensitive && preferenceRepository.getBoolean(KEY_HIDE_SENSITIVE_MEDIA, true))

        val oldSize = mediaSize
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
        mediaSize = Math.min(newSize, maxMediaSize)
        invalidate()
        updateImages(newMedias)
    }

    private fun updateImages(medias: Array<Media>) {
        medias.forEachIndexed { index, media ->
            setMediaToView(media, containerViews[index])
        }
    }

    private fun setMediaToView(media: Media, view: FrameLayout) {
        val thumbnailUrl = media.thumbnailUrl
        val originalUrl = media.originalUrl

        val url = thumbnailUrl ?: originalUrl
        val imageView = view.getChildAt(0) as ImageView
        val playButton = view.getChildAt(1)
        val markImage = view.getChildAt(2)

        if (isOpen) {
            glideRequests
                    .load(
                            if (imageLoadMode == "normal")
                                TwitterStringUtils.convertSmallImageUrl(clientType, url)
                            else
                                TwitterStringUtils.convertThumbImageUrl(clientType, url)
                    )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
            when (media.mediaType) {
                "video_one", "video_multi" -> {
                    imageView.setFilterIfNotExist()
                    playButton.visibility = View.VISIBLE
                    markImage.visibility = View.GONE
                }
                "gif" -> {
                    imageView.setFilterIfNotExist()
                    playButton.visibility = View.VISIBLE
                    markImage.visibility = View.VISIBLE
                }
                else -> {
                    imageView.removeFilterIfExist()
                    playButton.visibility = View.GONE
                    markImage.visibility = View.GONE
                }
            }
        } else {
            val timelineImageLoadMode = imageLoadMode
            if (timelineImageLoadMode != "none") {
                glideRequests
                        .load(
                                if (timelineImageLoadMode == "normal")
                                    TwitterStringUtils.convertSmallImageUrl(clientType, url)
                                else
                                    TwitterStringUtils.convertThumbImageUrl(clientType, url)
                        )
                        .transform(BlurTransformation())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.border_frame)
            }

            imageView.removeFilterIfExist()
            playButton.visibility = View.GONE
            markImage.visibility = View.GONE
        }
    }

    private fun ImageView.setFilterIfNotExist() {
        if (colorFilter == null) {
            colorFilter = imageFilter
        }
    }

    private fun ImageView.removeFilterIfExist() {
        if (colorFilter != null) {
            colorFilter = null
        }
    }

    fun clearImages() {
        repeat(mediaSize) {
            glideRequests.clear(containerViews[it].getChildAt(0) as ImageView)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        medias?.let { medias ->
            for (i in 0 until mediaSize) {
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
            for (i in 0 until mediaSize) {
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