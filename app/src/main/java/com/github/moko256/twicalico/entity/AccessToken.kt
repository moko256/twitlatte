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

package com.github.moko256.twicalico.entity

import android.support.annotation.IntDef
import kotlin.annotation.Retention

/**
 * Created by moko256 on 2018/01/13.
 *
 * @author moko256
 */

data class AccessToken(
        @Type.ClientTypeInt val type: Int,
        val url: String,
        val userId: Long,
        val screenName: String,
        val token: String,
        val tokenSecret: String? = null
) {
    fun getKeyString(): String {
        return url + "@" + userId.toString()
    }
}

fun splitAccessTokenKey(accessTokenKey: String): Pair<String, Long> {
    val splitString = accessTokenKey.split("@")
    return Pair(splitString[0], splitString[1].toLong())
}

class Type{

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Type.TWITTER.toLong(), Type.MASTODON.toLong())
    annotation class ClientTypeInt

    companion object{
        const val TWITTER: Int = 0
        const val MASTODON: Int = 1
    }
}