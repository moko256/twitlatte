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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by moko256 on 2018/03/27.
 *
 * @author moko256
 */
public class ArrayUtilsTest {

    @Test
    public void convertToList() {
        String[] array = new String[]{"0", "1", "2", "aaa", "@@@", "$$$$$$$"};
        List<String> list = Arrays.asList(array);
        for (int i = 0, length = array.length; i < length; i++){
            assertTrue(array[i].equals(list.get(i)));
        }
    }

    @Test
    public void toCommaSplitString() {
        assertTrue(
                "".contentEquals(ArrayUtils.toCommaSplitString(null))
        );

        assertTrue(
                "".contentEquals(ArrayUtils.toCommaSplitString(new String[]{}))
        );

        String[] array = new String[]{"0", "1", "2", "aaa", "@@@", "$$$$$$$"};
        CharSequence result = ArrayUtils.toCommaSplitString(array);
        assertTrue("0,1,2,aaa,@@@,$$$$$$$".contentEquals(result));
    }

    @Test
    public void toCommaAndPipeSplitString() {
        assertTrue(
                "".contentEquals(ArrayUtils.toCommaAndPipeSplitString(null))
        );

        assertTrue(
                "".contentEquals(ArrayUtils.toCommaAndPipeSplitString(new String[][]{new String[]{}}))
        );

        String[][] array = new String[][]{new String[]{"0", "1", "2"}, new String[]{"aaa", "@@@", "$$$$$$$"}};
        CharSequence result = ArrayUtils.toCommaAndPipeSplitString(array);
        assertTrue("0|1|2,aaa|@@@|$$$$$$$".contentEquals(result));
    }}