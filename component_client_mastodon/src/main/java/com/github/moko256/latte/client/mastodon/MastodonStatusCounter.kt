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

package com.github.moko256.latte.client.mastodon

import com.github.moko256.latte.client.base.StatusCounter
import com.github.moko256.latte.client.base.entity.UpdateStatus
import com.twitter.twittertext.TwitterTextParseResults
import com.twitter.twittertext.TwitterTextParser

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
internal class MastodonStatusCounter: StatusCounter {
    private var imageSize: Int = 0

    private var resultCache: TwitterTextParseResults? = null

    override fun setUpdateStatus(updateStatus: UpdateStatus, imageSize: Int) {
        val context = updateStatus.context
        val contentWarning = updateStatus.contentWarning

        resultCache = TwitterTextParser.parseTweet(
                if (contentWarning == null) {
                    context
                } else {
                    context + contentWarning
                },
                TwitterTextParser.TWITTER_TEXT_CODE_POINT_COUNT_CONFIG
        )

        this.imageSize = imageSize
    }

    override fun getContextLength(): Int {
        return resultCache?.weightedLength ?: 0
    }

    override fun isValidStatus(): Boolean {
        return if (resultCache?.weightedLength == 0) {
            imageSize > 0
        } else {
            resultCache?.weightedLength?:0 <= limit
        }
    }

    override val limit: Int = 500

}