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

package com.github.moko256.twitlatte.testdata

import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.latte.client.base.entity.Repeat
import com.github.moko256.latte.client.base.entity.Status
import com.github.moko256.latte.client.base.entity.User
import java.util.*

fun testStatus(testId: Long, testText: String = "", testUserId: Long = 0) = Status(
        Date(0), testId, testUserId, testText, null, null,
        0, 0, "", false,
        false,
        0, 0, 0, false, "",
        null, null, null, 0, "", "",
        null, "", null, null
)

fun testRepeat(testId: Long, testUserId: Long = 0, repeatedStatusId: Long) = Repeat(
        Date(0), testId, testUserId, repeatedStatusId
)

fun testUser(testId: Long, testName: String = "") = User(
        testId, testName,
        "", "", "", false, "",
        false, "", false, 0, "",
        "", "",
        "", "", false,
        false, 0, Date(0), 0, 0, "",
        "", "",
        false, "", 0, false, false,
        false, null, null
)

fun testPost(
        repeatId: Long = 0,
        repeatedUserId: Long = 0,
        statusId: Long = 0,
        userId: Long = 0,
        quotedRepeatingStatusId: Long = 0,
        quotedRepeatingUserId: Long = 0,
        id: Long = if (repeatId != 0L) repeatId else statusId
) = Post(
        id,
        if (repeatId == 0L) null else testRepeat(repeatId, repeatedUserId, statusId),
        if (repeatedUserId == 0L) null else testUser(repeatedUserId),
        if (statusId == 0L) null else testStatus(statusId, "", userId),
        if (userId == 0L) null else testUser(userId),
        if (quotedRepeatingStatusId == 0L) null else testStatus(quotedRepeatingStatusId, "", quotedRepeatingUserId),
        if (quotedRepeatingUserId == 0L) null else testUser(quotedRepeatingUserId)
)