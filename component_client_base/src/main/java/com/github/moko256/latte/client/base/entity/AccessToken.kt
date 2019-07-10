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

package com.github.moko256.latte.client.base.entity

/**
 * Created by moko256 on 2018/01/13.
 *
 * @author moko256
 */

data class AccessToken(
        val clientType: Int,
        val url: String,
        val userId: Long,
        val screenName: String,
        val consumerKey: String?,
        val consumerSecret: String?,
        val token: String,
        val tokenSecret: String?
) {
    fun getKeyString(): String {
        return "$url@$userId"
    }

    override fun equals(other: Any?): Boolean {
        return if (other !== this) {
            other != null && other is AccessToken && other.url == url && other.userId == userId
        } else {
            true
        }
    }

    override fun hashCode(): Int {
        return getKeyString().hashCode()
    }

    fun getHash(): Int {
        var result = clientType
        result = 31 * result + url.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + screenName.hashCode()
        result = 31 * result + (consumerKey?.hashCode() ?: 0)
        result = 31 * result + (consumerSecret?.hashCode() ?: 0)
        result = 31 * result + token.hashCode()
        result = 31 * result + (tokenSecret?.hashCode() ?: 0)
        return result
    }
}

fun splitAccessTokenKey(accessTokenKey: String): Pair<String, Long> {
    val splitString = accessTokenKey.split("@")
    return splitString[0] to splitString[1].toLong()
}