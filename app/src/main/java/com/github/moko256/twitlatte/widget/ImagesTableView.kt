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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.latte.client.base.CLIENT_TYPE_NOTHING
import com.github.moko256.latte.client.base.entity.Media
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.ShowMediasActivity
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.view.dpToPx
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.math.min

/**
 * Created by moko256 on 2019/01/21.
 */

class ImagesTableView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private companion object {
        private val a0000 = intArrayOf(0, 0, 0, 0)
        private val a0111 = intArrayOf(0, 1, 1, 1)
        private val a1111 = intArrayOf(1, 1, 1, 1)
        private val a0021 = intArrayOf(0, 0, 2, 1)

        private val a0022 = intArrayOf(0, 0, 2, 2)
        private val a0121 = intArrayOf(0, 1, 2, 1)
        private val a0011 = intArrayOf(0, 0, 1, 1)
        private val a1011 = intArrayOf(1, 0, 1, 1)

        /* {row,column,rowSpan,colSpan} */
        private val params = arrayOf(
                arrayOf(a0022, a0000, a0000, a0000),
                arrayOf(a0021, a0121, a0000, a0000),
                arrayOf(a0021, a0111, a1111, a0000),
                arrayOf(a0011, a0111, a1011, a1111)
        )

        private const val maxMediaSize = 4

        private val imageFilter = PorterDuffColorFilter(0x33000000, PorterDuff.Mode.SRC_ATOP)
    }

    private val dp = context.resources.displayMetrics.density
    private val dividerSize = dpToPx(4, dp)
    private val markSize = dpToPx(48, dp)

    private val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_play_arrow_white_24dp)!!
            .apply {
                setBounds(0, 0, markSize, markSize)
            }

    private val gifMark = AppCompatResources.getDrawable(context, R.drawable.ic_gif_white_24dp)!!
            .apply {
                setBounds(0, 0, markSize, markSize)
            }

    private var clientType = CLIENT_TYPE_NOTHING

    private var isOpen = true

    private lateinit var imageLoadMode: String

    private var imageTableData: ImageTableData? = null
    private var displayingMediaSize = 0

    private val longClickListener = OnLongClickListener { this.performLongClick() }

    private var containerViews = arrayOfNulls<ImageTableImageView>(maxMediaSize)

    private fun getContainer(index: Int): ImageTableImageView {
        return containerViews[index]
                ?: generateChild(index)
                        .also {
                            containerViews[index] = it
                            addViewInLayout(it, index, generateDefaultLayoutParams())
                        }
    }

    private fun generateChild(index: Int): ImageTableImageView {
        val imageView = ImageTableImageView(
                context,
                drawable,
                gifMark,
                markSize
        )

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setOnLongClickListener(longClickListener)
        imageView.setOnClickListener {
            imageTableData?.let { imageTableData ->
                if (isOpen) {
                    context.startActivity(ShowMediasActivity.getIntent(context, imageTableData.medias, clientType, index))
                } else {
                    isOpen = true
                    updateImages(imageTableData)
                }
            }
        }

        return imageView
    }

    fun setMedias(
            requestManager: RequestManager,
            newMedias: Array<Media>,
            clientType: Int,
            sensitive: Boolean,
            imageLoadMode: String,
            isHideSensitiveMedia: Boolean
    ) {
        this.imageLoadMode = imageLoadMode
        this.clientType = clientType
        isOpen = imageLoadMode != "none" && !(sensitive && isHideSensitiveMedia)

        val oldSize = displayingMediaSize
        val newSize = newMedias.size

        if (oldSize < newSize) {
            for (i in oldSize until newSize) {
                getContainer(i).visibility = View.VISIBLE
            }
        } else if (newSize < oldSize) {
            for (i in newSize until oldSize) {
                getContainer(i).visibility = View.GONE
            }
        }

        val data = ImageTableData(newMedias, requestManager)
        imageTableData = data
        displayingMediaSize = min(newSize, maxMediaSize)
        invalidate()
        updateImages(data)
    }

    private fun updateImages(imageTableData: ImageTableData) {
        imageTableData.medias.forEachIndexed { index, media ->
            setMediaToView(media, getContainer(index), imageTableData.requestManager)
        }
    }

    private fun setMediaToView(media: Media, imageView: ImageTableImageView, requestManager: RequestManager) {
        val thumbnailUrl = media.thumbnailUrl
        val originalUrl = media.originalUrl

        val url = thumbnailUrl ?: originalUrl

        if (isOpen) {
            requestManager
                    .load(
                            if (imageLoadMode == "normal")
                                TwitterStringUtils.convertSmallImageUrl(clientType, url)
                            else
                                TwitterStringUtils.convertThumbImageUrl(clientType, url)
                    )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
            when (media.mediaType) {
                "audio", "video_one", "video_multi" -> {
                    imageView.setFilterIfNotExist()
                    imageView.drawPlay = true
                    imageView.drawGif = false
                }
                "gif" -> {
                    imageView.setFilterIfNotExist()
                    imageView.drawPlay = true
                    imageView.drawGif = true
                }
                else -> {
                    imageView.removeFilterIfExist()
                    imageView.drawPlay = false
                    imageView.drawGif = false
                }
            }
        } else {
            val timelineImageLoadMode = imageLoadMode
            if (timelineImageLoadMode != "none") {
                requestManager
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
            imageView.drawPlay = false
            imageView.drawGif = false
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
        imageTableData?.requestManager?.apply {
            repeat(displayingMediaSize) {
                clear(getContainer(it))
            }
            imageTableData = null
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        imageTableData?.medias?.let { medias ->
            repeat(displayingMediaSize) { i ->
                val view = getContainer(i)
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

        imageTableData?.medias?.let { medias ->
            val mediasSizesParams = params[medias.size - 1]
            repeat(displayingMediaSize) { i ->
                val param = mediasSizesParams[i]
                val view = getContainer(i)
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

    class ImageTableData(
            val medias: Array<Media>,
            val requestManager: RequestManager
    )

    @SuppressLint("ViewConstructor")
    class ImageTableImageView(
            context: Context,
            private val play: Drawable,
            private val gif: Drawable,
            private val drawableSize: Int
    ) : AppCompatImageView(context) {
        var drawPlay = false
        var drawGif = false

        private var playWidth = 0f
        private var playHeight = 0f
        private var gifHeight = 0f

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (drawGif) {
                canvas.translate(0f, gifHeight)
                gif.draw(canvas)
                canvas.translate(0f, -gifHeight)
            }

            if (drawPlay) {
                canvas.translate(playWidth, playHeight)
                play.draw(canvas)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            val width = measuredWidth
            val height = measuredHeight

            val diffWidthAndSize = width - drawableSize
            val diffHeightAndSize = height - drawableSize
            val halfDiffWidthAndSize = diffWidthAndSize / 2
            val halfDiffHeightAndSize = diffHeightAndSize / 2

            playWidth = halfDiffWidthAndSize.toFloat()
            playHeight = halfDiffHeightAndSize.toFloat()
            gifHeight = diffHeightAndSize.toFloat()
        }
    }
}