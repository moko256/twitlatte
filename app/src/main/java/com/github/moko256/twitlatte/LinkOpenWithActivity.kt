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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.twitlatte.intent.excludeOwnApp
import com.github.moko256.twitlatte.repository.KEY_ACCOUNT_KEY_LINK_OPEN
import com.github.moko256.twitlatte.text.TwitterStringUtils
import java.util.*

/**
 * Created by moko256 on 2017/04/16.
 *
 * @author moko256
 */

class LinkOpenWithActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val switchIntent = switchIntent()
        if (switchIntent != null) {
            val toName = switchIntent.component?.className
            if (ShowUserActivity::class.java.name != toName && ShowTweetActivity::class.java.name != toName) {
                startActivity(switchIntent)
            } else {
                showDialog {
                    startActivity(switchIntent.setAccountKey(it))

                    Toast.makeText(
                            this@LinkOpenWithActivity,
                            TwitterStringUtils.plusAtMark(
                                    it.screenName,
                                    it.url
                            ),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            finish()
        }
    }

    private fun switchIntent(): Intent? {

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
                                else -> return generateAlterIntent(data)
                            }
                            "https", "http" -> {
                                val pathSegments = data.pathSegments

                                if (pathSegments.size > 0 && pathSegments[0] == "i") {
                                    return generateAlterIntent(data)
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
                                    generateAlterIntent(data)
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

        return null
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
            accessTokens.isEmpty() -> {
                startActivity(generateAlterIntent(intent.data!!))
                finish()
            }

            accessTokens.size == 1 -> callback(accessTokens[0])

            else -> {
                val accountsLinkOpenWith = preferenceRepository
                        .getString(KEY_ACCOUNT_KEY_LINK_OPEN, "-1")
                        .takeIf { it != "-1" }
                        ?.run {
                            accessTokens.single { it.getKeyString() == this }
                        }
                if (accountsLinkOpenWith == null) {
                    val dp= resources.displayMetrics.density
                    val dp16 = Math.round(dp * 16)
                    val dp24 = Math.round(dp * 24)
                    AlertDialog.Builder(this)
                            .setTitle(R.string.open_with_accounts)
                            .setView(TextView(this).apply {
                                setText(R.string.settable_account_always_use_in_settings)
                                TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Subtitle1)
                                setPadding(dp24, dp16, dp24, dp16)
                            })
                            .setOnCancelListener { finish() }
                            .setItems(
                                    accessTokens
                                            .map {
                                                TwitterStringUtils.plusAtMark(it.screenName, it.url).apply {
                                                    if (it == getCurrentClient()?.accessToken) {
                                                        insert(0, "Now: ")
                                                    }
                                                }
                                            }
                                            .toTypedArray()
                            ) { _, which ->
                                callback(accessTokens[which])
                            }
                            .show()
                } else {
                    callback(accountsLinkOpenWith)
                }
            }
        }
    }

    private fun generateAlterIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW, uri).excludeOwnApp(this, packageManager)
    }
}