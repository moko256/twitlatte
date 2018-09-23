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

package com.github.moko256.twitlatte.array;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2018/03/27.
 *
 * @author moko256
 */
public class ArrayUtilsTest {

    @Test
    public void toCommaSplitString() {
        assertEquals(
                "", ArrayUtils.toCommaSplitString(null).toString()
        );

        assertEquals(
                "", ArrayUtils.toCommaSplitString(new String[]{}).toString()
        );

        String[] array = new String[]{"0", "1", "2", "aaa", "@@@", "$$$$$$$", ""};
        String result = ArrayUtils.toCommaSplitString(array).toString();
        assertEquals("0,1,2,aaa,@@@,$$$$$$$,", result);
    }

}