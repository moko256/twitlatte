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

import com.github.moko256.twitlatte.entity.Paging
import com.sys1yagi.mastodon4j.api.Range

/**
 * Created by moko256 on 2018/12/01.
 *
 * @author moko256
 */

fun Paging.convertToMastodonRange(): Range {
    return Range(
            limit = count,
            sinceId = sinceId.takeIf { it > 0 },
            maxId = maxId.takeIf { it > 0 }
    )
}