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

package com.github.moko256.latte.client.twitter

import com.github.moko256.latte.client.base.StatusCounter
import com.twitter.twittertext.TwitterTextParseResults
import com.twitter.twittertext.TwitterTextParser

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
internal class TwitterStatusCounter: StatusCounter {
    private var resultCacheString: String? = null
    private var resultCache: TwitterTextParseResults? = null

    override fun getLength(text: String): Int {
        updateCounter(text)
        return resultCache?.weightedLength ?: 0
    }

    override fun isValid(text: String): Boolean {
        updateCounter(text)
        return resultCache?.isValid ?: false
    }

    private fun updateCounter(text: String) {
        if (text != resultCacheString) {
            resultCacheString = text
            resultCache = TwitterTextParser.parseTweet(text, TwitterTextParser.TWITTER_TEXT_EMOJI_CHAR_COUNT_CONFIG)
        }
    }
    override val limit: Int = TwitterTextParser.TWITTER_TEXT_EMOJI_CHAR_COUNT_CONFIG.maxWeightedTweetLength
}