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

package com.github.moko256.twitlatte.api.mastodon

import com.github.moko256.twitlatte.api.base.StatusCounter

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
class MastodonStatusCounter: StatusCounter {
    override fun getLength(text: String): Int {
        return text.codePointCount(0, text.length)
    }

    override fun isValid(text: String): Boolean {
        return getLength(text) <= limit
    }

    override val limit: Int = 500

}