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

package com.github.moko256.latte.client.mastodon.okhttp

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.source
import java.io.IOException
import java.io.InputStream

/**
 * Created by moko256 on 2018/12/08.
 *
 * @author moko256
 */
internal class InputStreamRequestBody(private val mediaType: MediaType?, private val inputStream: InputStream): RequestBody() {
    override fun contentType(): MediaType {
        return mediaType!!
    }

    override fun contentLength(): Long {
        return try {
            inputStream.available().toLong()
        } catch (e: IOException) {
            0
        }
    }

    override fun writeTo(sink: BufferedSink) {
        val source = inputStream.source()
        try {
            sink.writeAll(source)
        } finally {
            Util.closeQuietly(source)
        }
    }
}