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

package com.github.moko256.latte.client.twitter

import com.github.moko256.latte.client.base.MediaUrlConverter
import com.github.moko256.latte.client.base.entity.User

object TwitterMediaUrlConverterImpl : MediaUrlConverter {

    override fun convertProfileIconSmallestUrl(user: User) = user.profileImageURLHttps.toResizedURL("_mini")

    override fun convertProfileIconUriBySize(user: User, sizePx: Int): String {
        return user.profileImageURLHttps.toResizedURL(
                when {
                    sizePx > 300 -> "_400x400"
                    sizePx > 73 -> "_200x200"
                    sizePx > 48 -> "_bigger"
                    sizePx > 24 -> "_normal"
                    else -> "_mini"
                }
        )
    }

    override fun convertProfileIconOriginalUrl(user: User) = user.profileImageURLHttps.toResizedURL("")

    private fun String.toResizedURL(sizeSuffix: String): String {
        return if (isNotEmpty()) {
            val sizeSuffixLength = sizeSuffix.length
            val characters = CharArray(length + sizeSuffixLength)
            val index = lastIndexOf('_')
            val suffixIndex = lastIndexOf('.')
            val slashIndex = lastIndexOf('/')
            var size = 0
            characters.append(this, size, 0, index)
            size += index
            characters.append(sizeSuffix, size, 0, sizeSuffixLength)
            size+=sizeSuffixLength
            if (suffixIndex > slashIndex) {
                characters.append(this, size, suffixIndex, length)
                size += length - suffixIndex
            }
            String(characters, 0, size)
        } else {
            this
        }
    }

    private fun CharArray.append(s: String, size: Int, start: Int, end: Int) {
        s.toCharArray(this, size, start, end)
    }

    override fun convertProfileBannerSmallUrl(user: User) =
            user.profileBannerImageUrl?.let { "$it/web_retina" }

    override fun convertProfileBannerLargeUrl(user: User) =
            user.profileBannerImageUrl?.let { "$it/1500x500" }
}
