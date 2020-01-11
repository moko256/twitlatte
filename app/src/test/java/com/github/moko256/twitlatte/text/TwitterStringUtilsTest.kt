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
package com.github.moko256.twitlatte.text

import org.junit.Assert
import org.junit.Test

/**
 * Created by moko256 on 2017/04/03.
 *
 * @author moko256
 */

class TwitterStringUtilsTest {

    @Test
    fun plusAtMarkTest() {
        assertEquals("@testuser120", TwitterStringUtils.plusAtMark("testuser120"))
        assertEquals("@test@example.com", TwitterStringUtils.plusAtMark("test", "example.com"))
        assertEquals("@a@b@c@d@e", TwitterStringUtils.plusAtMark("a", "b", "c", "d", "e"))
    }


    private val unitsEn = charArrayOf('K', 'M', 'G')

    private fun testEn(num: Int, expected: String) {
        assertEquals(expected, TwitterStringUtils.formatIntInCompactForm(num, 3, 0, unitsEn))
    }

    @Test
    fun convertToSIUnitStringTestEn() {
        testEn(0, "0")
        testEn(10, "10")
        testEn(100, "100")

        testEn(1000, "1K")
        testEn(10000, "10K")
        testEn(100000, "100K")

        testEn(1000000, "1M")
        testEn(10000000, "10M")
        testEn(100000000, "100M")

        testEn(1000000000, "1G")


        testEn(-10, "-10")
        testEn(-100, "-100")

        testEn(-1000, "-1K")
        testEn(-10000, "-10K")
        testEn(-100000, "-100K")

        testEn(-1000000, "-1M")
        testEn(-10000000, "-10M")
        testEn(-100000000, "-100M")

        testEn(-1000000000, "-1G")
    }


    private val unitsJa = charArrayOf('万', '億')

    private fun testJa(num: Int, expected: String) {
        assertEquals(expected, TwitterStringUtils.formatIntInCompactForm(num, 4, 1, unitsJa))
    }

    @Test
    fun convertToSIUnitStringTestJa() {
        testJa(0, "0")
        testJa(10, "10")
        testJa(100, "100")
        testJa(1000, "1000")

        testJa(10000, "1万")
        testJa(100000, "10万")
        testJa(1000000, "100万")

        testJa(10000000, "0.1億")
        testJa(100000000, "1億")
        testJa(1000000000, "10億")


        testJa(-10, "-10")
        testJa(-100, "-100")
        testJa(-1000, "-1000")

        testJa(-10000, "-1万")
        testJa(-100000, "-10万")
        testJa(-1000000, "-100万")

        testJa(-10000000, "-0.1億")
        testJa(-100000000, "-1億")
        testJa(-1000000000, "-10億")
    }


    @Test
    fun convertToReplyTopStringTest() {
        assertEquals(
            "@target ",
            TwitterStringUtils
                .convertToReplyTopString("user01", "target", null)
        )
        assertEquals(
            "",
            TwitterStringUtils
                .convertToReplyTopString("same", "same", null)
        )
        assertEquals(
            "@target @tu1 @tu2 ",
            TwitterStringUtils
                .convertToReplyTopString("user01", "target", arrayOf("tu1", "tu2"))
        )
        assertEquals(
            "@tu1 @tu2 ",
            TwitterStringUtils
                .convertToReplyTopString("same", "same", arrayOf("tu1", "tu2"))
        )
        assertEquals(
            "@target @tu2 ",
            TwitterStringUtils
                .convertToReplyTopString("tu1", "target", arrayOf("tu1", "tu2"))
        )
    }

    private fun assertEquals(expected: String, actual: CharSequence) =
        Assert.assertEquals(expected, actual.toString())

}