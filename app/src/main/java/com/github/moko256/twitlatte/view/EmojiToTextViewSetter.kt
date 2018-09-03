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
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.glide.GlideRequests
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Cancellable
import io.reactivex.internal.disposables.CancellableDisposable
import io.reactivex.schedulers.Schedulers
import net.ellerton.japng.android.api.PngAndroid
import java.io.InputStream
import java.util.regex.Pattern

/**
 * Created by moko256 on 2018/07/20.
 *
 * @author moko256
 */

private val containsEmoji = Pattern.compile(":([a-zA-Z0-9_]{2,}):")

class EmojiToTextViewSetter(private val glideRequests: GlideRequests, private val textView: TextView) {

    fun set(text: CharSequence, emojis: Array<Emoji>): Array<Disposable>? {
        val disposable = ArrayList<Disposable>(2)

        val matcher = containsEmoji.matcher(text)
        val matches = matcher.matches()

        val imageSize = if (matches) {
            textView.lineHeight.toFloat() * 2
        } else {
            textView.lineHeight.toFloat()
        }

        val map: HashMap<String, ArrayList<Int>>

        if (matches) {
            map = HashMap(1)

            map[matcher.group(1)] = ArrayList<Int>(1).also {
                it.add(matcher.start())
            }
        } else {
            var find = matcher.find()
            if (find) {
                map = HashMap(emojis.size * 2)

                while (find) {
                    val shortCode = matcher.group(1)
                    if (map[shortCode] == null) {
                        map[shortCode] = ArrayList()
                    }

                    map[shortCode]!!.add(matcher.start())

                    find = matcher.find()
                }
            } else {
                map = HashMap(0)
            }
        }

        val containedEmoji = emojis.filter { map.containsKey(it.shortCode) }

        if (containedEmoji.isNotEmpty()) {

            val builder = SpannableStringBuilder.valueOf(text)

            val handler = Handler()

            disposable.add(Observable
                    .create<Pair<Emoji, Drawable>> {
                        containedEmoji.forEach { emoji ->
                            var inputStream: InputStream? = null
                            try {
                                inputStream = glideRequests
                                        .asFile()
                                        .load(emoji.url)
                                        .submit()
                                        .get()
                                        .inputStream()
                                it.onNext(emoji to PngAndroid.readDrawable(textView.context, inputStream))
                                inputStream.close()
                            } catch (e: Throwable) {
                                inputStream?.close()
                                try {
                                    it.onNext(
                                            emoji to
                                                    glideRequests
                                                            .load(emoji.url)
                                                            .submit()
                                                            .get()
                                    )
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    it.onNext(
                                            emoji to
                                                    ContextCompat.getDrawable(
                                                            textView.context,
                                                            R.drawable.ic_cloud_off_black_24dp
                                                    )!!
                                    )
                                }
                            }
                        }
                        it.onComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { pair ->
                        val emoji = pair.first
                        val drawable = pair.second.mutate()
                        var w = drawable.intrinsicWidth.toFloat()
                        var h = drawable.intrinsicHeight.toFloat()
                        if (w == h) {
                            w = imageSize
                            h = imageSize
                        } else {
                            if (w > h) {
                                val d = imageSize / w
                                w = imageSize
                                h *= d
                            } else {
                                val d = imageSize / h
                                w *= d
                                h = imageSize
                            }
                        }
                        drawable.setBounds(0, 0, Math.round(w), Math.round(h))

                        map[emoji.shortCode]?.forEach {
                            builder.setSpan(
                                    ImageSpan(drawable),
                                    it,
                                    it + emoji.shortCode.length + 2,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        if (drawable is Animatable) {
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

                        textView.text = builder
                    })

            return disposable.toTypedArray()
        } else {
            return null
        }
    }

}