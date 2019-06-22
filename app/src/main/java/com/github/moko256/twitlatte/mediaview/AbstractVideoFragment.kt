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

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.exoplayer.AudioAndVideoRenderer
import com.github.moko256.twitlatte.net.appOkHttpClientInstance
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder

/**
 * Created by moko256 on 2018/10/07.
 *
 * @author moko256
 */
abstract class AbstractVideoFragment : AbstractMediaFragment(), Player.EventListener {

    companion object {
        val dataSourceFactory = OkHttpDataSourceFactory(
                appOkHttpClientInstance,
                null
        )
    }

    private lateinit var videoPlayView: PlayerView
    private lateinit var player: SimpleExoPlayer

    private var wasFragmentShowing = false
    private var canPlayNext = true

    protected var isLoop = false

    private val trackSelector = DefaultTrackSelector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayerFactory.newSimpleInstance(
                context,
                AudioAndVideoRenderer(requireContext()),
                trackSelector
        ).also {
            it.prepare(
                    getMediaSourceFactory()
                            .createMediaSource(
                                    Uri.parse(media.originalUrl)
                            )
            )

            if (isLoop) {
                it.repeatMode = REPEAT_MODE_ALL
            } else {
                it.addListener(this)
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        activity?.window?.decorView?.keepScreenOn = playWhenReady && playbackState == STATE_READY
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        videoPlayView = view.findViewById(R.id.fragment_image_pager_video)

        videoPlayView.also {

            it.controllerShowTimeoutMs = 3000
            it.useArtwork = true
            it.setControllerVisibilityListener { visibility ->
                if (visibility != VISIBLE) {
                    hideSystemUI()
                } else {
                    showSystemUI()
                }
            }

            setSystemUIVisibilityListener { visibility ->
                if (visibility and SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    showActionbar()
                    it.showController()
                } else {
                    it.hideController()
                }
            }

            it.player = player
        }
    }

    private fun updatePlayingStatus(isFragmentShowing: Boolean) {
        val wasPlaying = player.playWhenReady

        player.playWhenReady = isFragmentShowing && canPlayNext

        if (wasFragmentShowing != isFragmentShowing) {
            canPlayNext = wasPlaying
            wasFragmentShowing = isFragmentShowing
        }
    }

    override fun onResume() {
        super.onResume()
        updatePlayingStatus(true)
    }

    override fun onPause() {
        updatePlayingStatus(false)
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_change_selection) {
            if (trackSelector.currentMappedTrackInfo != null) {
                TrackSelectionDialogBuilder(
                        activity,
                        getString(R.string.action_change_quality),
                        trackSelector,
                        0
                ).build().show()
            } else {
                Toast.makeText(
                        context,
                        R.string.use_only_movie_loaded,
                        LENGTH_SHORT
                ).show()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun returnLayoutId() = R.layout.fragment_video
    override fun returnMenuId() = R.menu.fragment_video_toolbar

    protected abstract fun getMediaSourceFactory(): AdsMediaSource.MediaSourceFactory
}