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

@file:JvmName(name = "ArrayUtils")
package com.github.moko256.twitlatte.array

/**
 * Created by moko256 on 2018/01/04.
 *
 * @author moko256
 */


fun Array<out String?>?.toCommaSplitString(): CharSequence {
    if (this != null && isNotEmpty()) {
        val builder = StringBuilder(size * 10)
        var i = 0
        while (true) {
            builder.append(get(i))
            if (i < size - 1) {
                builder.append(",")
            } else {
                return builder
            }
            i++
        }
    } else {
        return ""
    }
}

fun Array<out Array<out String?>?>?.toCommaAndPipeSplitString(): CharSequence {
    if (this != null && isNotEmpty()) {
        val builder = StringBuilder(size * 10)
        var x = 0
        while (true) {
            val childX = get(x)
            if (childX != null && childX.isNotEmpty()) {
                var y = 0
                while (true) {
                    builder.append(childX[y])
                    if (y < childX.size - 1) {
                        builder.append("|")
                    } else {
                        break
                    }
                    y++
                }
            }
            if (x < size - 1) {
                builder.append(",")
            } else {
                return builder
            }
            x++
        }
    } else {
        return ""
    }
}