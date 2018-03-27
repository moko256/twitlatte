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

package com.github.moko256.twicalico.array;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by moko256 on 2018/01/04.
 *
 * @author moko256
 */

public class ArrayUtils {
    @SafeVarargs
    @NonNull
    public static <T> List<T> convertToList(@NonNull T... array){
        return Arrays.asList(array);
    }

    @NonNull
    public static List<Long> convertToLongList(@NonNull long... array){
        List<Long> list = new ArrayList<>(array.length);
        for (long l : array) {
            list.add(l);
        }
        return list;
    }

    @NonNull
    public static CharSequence toCommaSplitString(@Nullable String[] array){
        if (array != null && array.length > 0) {
            StringBuilder builder = new StringBuilder(array.length * 10);
            for (int i = 0; ; i++) {
                builder.append(array[i]);
                if (i < array.length - 1) {
                    builder.append(",");
                } else {
                    return builder;
                }
            }
        } else {
            return "";
        }
    }
}
