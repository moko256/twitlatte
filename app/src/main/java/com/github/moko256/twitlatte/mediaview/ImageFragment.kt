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

package com.github.moko256.twitlatte.mediaview

import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.github.chrisbanes.photoview.PhotoView
import com.github.chuross.flinglayout.FlingLayout
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.glide.GlideApp
import com.github.moko256.twitlatte.preferenceRepository
import com.github.moko256.twitlatte.repository.KEY_TIMELINE_IMAGE_LOAD_MODE
import com.github.moko256.twitlatte.text.TwitterStringUtils

/**
 * Created by moko256 on 2018/10/07.
 *
 * @author moko256
 */
class ImageFragment: AbstractMediaFragment() {

    private lateinit var imageView: PhotoView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSystemUIVisibilityListener { visibility ->
            if (visibility and SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                showActionbar()
            }
        }

        imageView = view.findViewById(R.id.fragment_image_pager_image)
        imageView.setOnClickListener {
            if (isShowingSystemUI()) {
                hideSystemUI()
            } else {
                showSystemUI()
            }
        }
        imageView.setOnScaleChangeListener { scaleFactor: Float, _: Float, _: Float ->
            (view as FlingLayout).isDragEnabled = scaleFactor <= 1.1f
        }
        val requests = GlideApp.with(this)
        val url = media.originalUrl

        requests
                .load(TwitterStringUtils.convertLargeImageUrl(clientType, url))
                .override(SIZE_ORIGINAL)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .thumbnail(
                        requests.load(
                                if (preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal") == "normal") {
                                    TwitterStringUtils.convertSmallImageUrl(clientType, url)
                                } else {
                                    TwitterStringUtils.convertThumbImageUrl(clientType, url)
                                }
                        ).override(SIZE_ORIGINAL)
                )
                .into(imageView)
    }

    override fun returnLayoutId() = R.layout.fragment_image
    override fun returnMenuId() = R.menu.fragment_image_toolbar

}