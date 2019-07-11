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

package com.github.moko256.twitlatte.testutils

import com.github.moko256.latte.client.base.entity.AccessToken

/**
 * Created by moko256 on 2019/07/11.
 *
 * @author moko256
 */

@JvmField val emptyAccessToken = AccessToken(
        Int.MAX_VALUE,
        "test.example.com",
        4423,
        "test_sn",
        "test_ck",
        "test_cs",
        "test_token",
        "test_tokenSecret"

)