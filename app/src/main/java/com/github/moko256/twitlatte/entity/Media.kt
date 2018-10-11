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

package com.github.moko256.twitlatte.entity

import java.io.Serializable

/**
 * Created by moko256 on 2017/12/22.
 *
 * @author moko256
 */
data class Media (
        val thumbnailUrl: String? = null,
        val originalUrl: String,
        val downloadVideoUrl: String? = null,
        val imageType: String
): Serializable {
    enum class ImageType(val value: String) {
        PICTURE("picture"),
        GIF("gif"),
        VIDEO_ONE("video_one"),
        VIDEO_MULTI("video_multi"),
    }
}