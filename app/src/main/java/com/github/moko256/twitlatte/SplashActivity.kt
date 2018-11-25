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

package com.github.moko256.twitlatte

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * Created by moko256 on 2017/04/16.
 *
 * @author moko256
 */

class SplashActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(switchIntent())
    }

    private fun switchIntent(): Intent {

        if (GlobalApplication.twitter == null) {
            return Intent(this, OAuthActivity::class.java)
        }

        val intent = intent
        if (intent != null && (intent.action != null || intent.action != Intent.ACTION_MAIN)) {

            if (intent.action == Intent.ACTION_VIEW) {
                intent.data?.let { data ->
                    try {
                        when (data.scheme) {
                            "twitter" -> when (data.host) {
                                "post" -> {
                                    val replyId = data.getQueryParameter("in_reply_to_status_id")
                                    return PostActivity.getIntent(
                                            this,
                                            if (replyId != null) java.lang.Long.valueOf(replyId) else -1,
                                            data.getQueryParameter("message")
                                    )
                                }
                                "status" -> return ShowTweetActivity.getIntent(this, java.lang.Long.valueOf(data.getQueryParameter("id")!!))
                                "user" -> {
                                    val userId = data.getQueryParameter("id")
                                    return if (userId != null) {
                                        ShowUserActivity.getIntent(this, java.lang.Long.valueOf(userId))
                                    } else {
                                        ShowUserActivity.getIntent(this, data.getQueryParameter("screen_name"))
                                    }
                                }
                                else -> return Intent.createChooser(Intent(Intent.ACTION_VIEW, data), "")
                            }
                            "https" -> {
                                val pathSegments = data.pathSegments
                                val size = pathSegments.size

                                val lastPathSegment = data.lastPathSegment
                                when (size) {
                                    1 -> return when (lastPathSegment) {
                                        "share" -> generatePostIntent(data)
                                        "search" -> SearchResultActivity.getIntent(this, data.getQueryParameter("q"))
                                        else -> ShowUserActivity.getIntent(this, lastPathSegment)
                                    }
                                    2 -> if (pathSegments[0] == "intent" && lastPathSegment == "tweet") {
                                        return generatePostIntent(data)
                                    } else if (pathSegments[0] == "hashtag") {
                                        return ShowUserActivity.getIntent(this, "#$lastPathSegment")
                                    }

                                    3 -> {
                                        val s = pathSegments[1]
                                        if (s == "status" || s == "statuses") {
                                            return ShowTweetActivity.getIntent(this, java.lang.Long.valueOf(lastPathSegment!!))
                                        }
                                    }
                                }

                                return if (data.getQueryParameter("status") != null) {
                                    PostActivity.getIntent(this, data.getQueryParameter("status"))
                                } else Intent.createChooser(Intent(Intent.ACTION_VIEW, data), "")

                            }
                            "web+mastodon" -> if (data.host == "share") {
                                return PostActivity.getIntent(
                                        this,
                                        data.getQueryParameter("text")
                                )
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            } else {
                intent.extras?.let { extras ->
                    try {
                        val text = extras.getCharSequence(Intent.EXTRA_TEXT)
                        val subject = extras.getCharSequence(Intent.EXTRA_SUBJECT)
                        val list: ArrayList<Uri>? = extras.takeIf { it.containsKey(Intent.EXTRA_STREAM) }?.let {
                            if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                                extras.getParcelableArrayList(Intent.EXTRA_STREAM)
                            } else {
                                extras.getParcelable<Uri?>(Intent.EXTRA_STREAM)?.let { uri ->
                                    arrayListOf(uri)
                                }
                            }
                        }

                        val rawText = if (text != null || subject != null) {
                            StringBuilder().apply {
                                if (subject != null) {
                                    append(subject)
                                    append(" ")
                                }
                                if (text != null) {
                                    append(text)
                                }
                            }.toString()
                        } else {
                            null
                        }

                        if (rawText != null || list != null) {
                            return PostActivity.getIntent(this, -1, rawText, list)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }

        }

        return Intent(this, MainActivity::class.java)
    }

    private fun generatePostIntent(data: Uri): Intent {
        val tweet = StringBuilder(data.getQueryParameter("text")!!)
        val url = data.getQueryParameter("url")
        if (url != null) {
            tweet.append(" ")
                    .append(url)
        }
        val hashtagstr = data.getQueryParameter("hashtags")
        if (hashtagstr != null) {
            val hashtags = hashtagstr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (hashtag in hashtags) {
                tweet.append(" #")
                        .append(hashtag)
            }
        }
        val via = data.getQueryParameter("via")
        if (via != null) {
            tweet.append(" via @")
                    .append(via)
        }
        val relatedstr = data.getQueryParameter("related")
        if (relatedstr != null) {
            val relates = relatedstr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (related in relates) {
                tweet.append(" @")
                        .append(related)
            }
        }

        return PostActivity.getIntent(
                this,
                if (data.getQueryParameter("in-reply-to") != null)
                    java.lang.Long.valueOf(data.getQueryParameter("in-reply-to")!!)
                else
                    -1L,
                tweet.toString()
        )
    }
}