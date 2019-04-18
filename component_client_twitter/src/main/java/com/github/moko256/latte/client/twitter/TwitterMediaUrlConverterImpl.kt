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

class TwitterMediaUrlConverterImpl : MediaUrlConverter {

    override fun convertProfileIconSmallUrl(user: User) = user.profileImageURLHttps.toResizedURL("_mini")
    override fun convertProfileIconLargeUrl(user: User) = user.profileImageURLHttps.toResizedURL("_400x400")
    override fun convertProfileIconOriginalUrl(user: User) = user.profileImageURLHttps.toResizedURL("")

    private fun String.toResizedURL(sizeSuffix: String): String {
        if (isNotEmpty()) {
            val index = lastIndexOf('_')
            val suffixIndex = lastIndexOf('.')
            val slashIndex = lastIndexOf('/')
            var url = substring(0, index) + sizeSuffix
            if (suffixIndex > slashIndex) {
                url += substring(suffixIndex)
            }
            return url
        }
        return this
    }

    override fun convertProfileBannerSmallUrl(user: User)
            = user.profileBannerImageUrl?.let { "$it/web_retina" }

    override fun convertProfileBannerLargeUrl(user: User)
            = user.profileBannerImageUrl?.let { "$it/1500x500" }
}
