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

package com.github.moko256.twitlatte.view

import org.junit.Test

import org.junit.Assert.*

class DpToPxKtTest {


    @Test
    fun oneDpToPxTest() {
        assertEquals(oneDpToPx(234f), 234)
        assertEquals(oneDpToPx(23.4f), 23)
        assertEquals(oneDpToPx(23.5f), 24)
    }

    @Test
    fun dpToPxTest() {
        assertEquals(dpToPx(10, 23.4f), 234)
        assertEquals(dpToPx(10, 2.34f), 23)
        assertEquals(dpToPx(10, 2.35f), 24)
    }

}