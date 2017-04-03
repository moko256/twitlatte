/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2017/04/03.
 *
 * @author moko256
 */

public class TwitterStringUtilTest {
    @Test
    public void convertToSIUnitStringTest() throws Exception{
        assertEquals(TwitterStringUtil.convertToSIUnitString(0), "0");
        assertEquals(TwitterStringUtil.convertToSIUnitString(100), "100");
        assertEquals(TwitterStringUtil.convertToSIUnitString(10000), "10K");
        assertEquals(TwitterStringUtil.convertToSIUnitString(10000000), "10M");
        assertEquals(TwitterStringUtil.convertToSIUnitString(1000000000), "1G");

        assertEquals(TwitterStringUtil.convertToSIUnitString(-0), "0");
        assertEquals(TwitterStringUtil.convertToSIUnitString(-100), "-100");
        assertEquals(TwitterStringUtil.convertToSIUnitString(-10000), "-10K");
        assertEquals(TwitterStringUtil.convertToSIUnitString(-10000000), "-10M");
        assertEquals(TwitterStringUtil.convertToSIUnitString(-1000000000), "-1G");
    }
}
