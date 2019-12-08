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

package com.github.moko256.twitlatte.exoplayer

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer
import com.google.android.exoplayer2.video.VideoRendererEventListener

/**
 * Created by moko256 on 2018/06/08.
 *
 * @author moko256
 */
class AudioAndVideoRenderer(private val context: Context, private val videoOnly: Boolean) : RenderersFactory {
    override fun createRenderers(
        eventHandler: Handler?,
        videoRendererEventListener: VideoRendererEventListener?,
        audioRendererEventListener: AudioRendererEventListener?,
        textRendererOutput: TextOutput?, metadataRendererOutput: MetadataOutput?,
        drmSessionManager: DrmSessionManager<FrameworkMediaCrypto>?
    ): Array<Renderer> {
        val videoRenderer = MediaCodecVideoRenderer(
            context,
            MediaCodecSelector.DEFAULT,
            DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS,
            eventHandler,
            videoRendererEventListener,
            -1
        )

        return if (videoOnly) {
            arrayOf(videoRenderer)
        } else {
            arrayOf(
                videoRenderer,
                MediaCodecAudioRenderer(
                    context,
                    MediaCodecSelector.DEFAULT,
                    eventHandler,
                    audioRendererEventListener
                )
            )
        }
    }
}