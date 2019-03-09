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

package com.github.moko256.latte.client.base.entity

/**
 * Created by moko256 on 2019/02/15.
 *
 * @author moko256
 */

data class UpdateStatus(
        var inReplyToStatusId: Long,
        var contentWarning: String?,
        var context: String,
        var imageIdList: List<Long>?,
        var isPossiblySensitive: Boolean,
        var location: Pair<Double, Double>?,
        var visibility: String?,
        var pollList: List<String>?,
        var isPollSelectableMultiple: Boolean,
        var isPollHideTotalsUntilExpired: Boolean,
        var pollExpiredSecond: Long
)