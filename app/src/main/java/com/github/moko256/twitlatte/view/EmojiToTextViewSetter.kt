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

package com.github.moko256.twitlatte.view

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v4.util.ArrayMap
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import com.github.moko256.twitlatte.GlideRequests
import com.github.moko256.twitlatte.entity.Emoji
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Cancellable
import io.reactivex.internal.disposables.CancellableDisposable
import io.reactivex.schedulers.Schedulers
import net.ellerton.japng.android.api.PngAndroid
import java.util.regex.Pattern

/**
 * Created by moko256 on 2018/07/20.
 *
 * @author moko256
 */

private val containsEmoji = Pattern.compile(":([a-zA-Z0-9_]{2,}):")

class EmojiToTextViewSetter(private val glideRequests: GlideRequests, private val textView: TextView) {
    fun set(html: CharSequence, emojis: List<Emoji>): Array<Disposable> {
        val disposable = ArrayList<Disposable>(2)

        val matcher = containsEmoji.matcher(html)
        val matches = matcher.matches()

        val imageSize = if (matches) {
            Math.round((textView.lineHeight * 2).toFloat())
        } else {
            Math.round(textView.lineHeight.toFloat())
        }

        disposable.add(Single.create<Map<String, Drawable>> {
            val map = ArrayMap<String, Drawable>()

            for (emoji in emojis) {
                var value: Drawable
                try {
                    val inputStream = glideRequests
                            .asFile()
                            .load(emoji.url)
                            .submit()
                            .get()
                            .inputStream()

                    value = PngAndroid
                            .readDrawable(textView.context, inputStream)
                            .mutate()
                    inputStream.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    value = glideRequests
                            .load(emoji.url)
                            .submit()
                            .get()
                            .mutate()
                }

                value.setBounds(0, 0, imageSize, imageSize)
                map[emoji.shortCode] = value
            }
            it.onSuccess(map)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    val builder = SpannableStringBuilder.valueOf(html)

                    var found = matches || matcher.find()
                    while (found) {
                        val shortCode = matcher.group(1)
                        val drawable = it[shortCode]
                        if (drawable != null) {
                            builder.setSpan(
                                    ImageSpan(drawable),
                                    matcher.start(), matcher.end(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            if (drawable is Animatable) {
                                val handler = Handler()
                                drawable.callback = object : Drawable.Callback {

                                    override fun invalidateDrawable(who: Drawable) {
                                        textView.invalidate()
                                    }

                                    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                        handler.postAtTime(what, `when`)
                                    }

                                    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                        handler.removeCallbacks(what)
                                    }
                                }
                                disposable.add(CancellableDisposable(Cancellable {
                                    drawable.stop()
                                }))
                                drawable.start()
                            }
                        }
                        found = matcher.find()
                    }
                    textView.text = builder
                })

        return disposable.toTypedArray()
    }
}