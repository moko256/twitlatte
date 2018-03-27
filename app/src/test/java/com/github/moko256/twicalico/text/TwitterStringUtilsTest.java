/*
 * Copyright 2018 The twicalico authors
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

package com.github.moko256.twicalico.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2017/04/03.
 *
 * @author moko256
 */

public class TwitterStringUtilsTest {
    @Test
    public void convertToSIUnitStringTest() {
        assertEquals(TwitterStringUtils.convertToSIUnitString(0), "0");
        assertEquals(TwitterStringUtils.convertToSIUnitString(100), "100");
        assertEquals(TwitterStringUtils.convertToSIUnitString(10000), "10K");
        assertEquals(TwitterStringUtils.convertToSIUnitString(10000000), "10M");
        assertEquals(TwitterStringUtils.convertToSIUnitString(1000000000), "1G");

        assertEquals(TwitterStringUtils.convertToSIUnitString(-0), "0");
        assertEquals(TwitterStringUtils.convertToSIUnitString(-100), "-100");
        assertEquals(TwitterStringUtils.convertToSIUnitString(-10000), "-10K");
        assertEquals(TwitterStringUtils.convertToSIUnitString(-10000000), "-10M");
        assertEquals(TwitterStringUtils.convertToSIUnitString(-1000000000), "-1G");
    }
}
