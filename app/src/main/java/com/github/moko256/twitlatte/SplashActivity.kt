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

package com.github.moko256.twitlatte

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.twitlatte.text.TwitterStringUtils
import java.util.*

/**
 * Created by moko256 on 2017/04/16.
 *
 * @author moko256
 */

class SplashActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val switchIntent = switchIntent()
        val toName = switchIntent.component?.className
        if (ShowUserActivity::class.java.name != toName && ShowTweetActivity::class.java.name != toName) {
            startActivity(switchIntent)
        } else {
            showDialog {
                startActivity(switchIntent.setAccountKey(it))
            }
        }
    }

    private fun switchIntent(): Intent {

        val intent = intent
        if (intent != null && (intent.action != null || intent.action != Intent.ACTION_MAIN)) {

            if (intent.action == Intent.ACTION_VIEW) {
                intent.data?.let { data ->
                    try {
                        when (data.scheme) {
                            "twitter" -> when (data.host) {
                                "post" -> {
                                    return PostActivity.getIntent(
                                            this,
                                            data
                                                    .getQueryParameter("in_reply_to_status_id")
                                                    ?.toLong()
                                                    ?:-1,
                                            data.getQueryParameter("message")
                                    )
                                }
                                "status" -> return ShowTweetActivity.getIntent(
                                        this,
                                        data.getQueryParameter("id")?.toLong()?:-1
                                )
                                "user" -> {
                                    val userId = data.getQueryParameter("id")
                                    return if (userId != null) {
                                        ShowUserActivity.getIntent(this, userId.toLong())
                                    } else {
                                        ShowUserActivity.getIntent(this, data.getQueryParameter("screen_name"))
                                    }
                                }
                                else -> return Intent.createChooser(Intent(Intent.ACTION_VIEW, data), "")
                            }
                            "https" -> {
                                val pathSegments = data.pathSegments

                                if (pathSegments.size > 0 && pathSegments[1] == "i") {
                                    TODO()
                                }

                                when (pathSegments.size) {
                                    1 -> return when (pathSegments[0]) {
                                        "share" -> generatePostIntent(data)
                                        "search" -> SearchResultActivity.getIntent(this, data.getQueryParameter("q"))
                                        else -> ShowUserActivity
                                                .getIntent(this, pathSegments[0])
                                    }
                                    2 -> if (pathSegments[0] == "intent" && pathSegments[1] == "tweet") {
                                        return generatePostIntent(data)
                                    } else if (pathSegments[0] == "hashtag") {
                                        return SearchResultActivity.getIntent(this, "#${pathSegments[1]}")
                                    }

                                    3, 5 -> {
                                        if (pathSegments[1] == "status" || pathSegments[1] == "statuses") {
                                            return ShowTweetActivity
                                                    .getIntent(this, pathSegments[2].toLong())

                                        }
                                    }
                                }

                                return if (data.getQueryParameter("status") != null) {
                                    PostActivity.getIntent(this, data.getQueryParameter("status"))
                                } else {
                                    Intent.createChooser(Intent(Intent.ACTION_VIEW, data), "")
                                }

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
                                    append(' ')
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

        return Intent(
                this,
                if (getCurrentClient() == null) {
                    OAuthActivity::class.java
                } else {
                    MainActivity::class.java
                }
        )
    }

    private fun generatePostIntent(data: Uri): Intent {
        val tweet = StringBuilder(data.getQueryParameter("text")?:"")

        data.getQueryParameter("url")?.let {
            tweet.append(' ').append(it)
        }

        data.getQueryParameter("hashtags")
                ?.split(",")
                ?.forEach {
                    tweet.append(" #").append(it)
                }

        data.getQueryParameter("via")?.let {
            tweet.append(" via @").append(it)
        }

        data.getQueryParameter("related")
                ?.split(",")
                ?.forEach {
                    tweet.append(" @").append(it)
                }

        return PostActivity.getIntent(
                this,
                data.getQueryParameter("in-reply-to")?.toLong()?:-1,
                tweet.toString()
        )
    }

    private fun showDialog(callback: (AccessToken) -> Unit) {
        val accessTokens = getAccountsModel()
                .getAccessTokensByType(CLIENT_TYPE_TWITTER)
        when {
            accessTokens.isEmpty() -> getAccountsModel().createGuestAccessToken(CLIENT_TYPE_TWITTER, "twitter.com")

            accessTokens.size == 1 -> callback(accessTokens[0])

            else -> AlertDialog.Builder(this)
                    .setTitle("Open with...")
                    .setItems(
                            accessTokens
                                    .map {
                                        TwitterStringUtils.plusAtMark(it.screenName, it.url).apply {
                                            if (it == getCurrentClient()?.accessToken) {
                                                insert(0, "* ")
                                            }
                                        }
                                    }
                                    .toTypedArray()
                    ) { _, which ->
                        callback(accessTokens[which])
                    }
                    .show()
        }
    }
}