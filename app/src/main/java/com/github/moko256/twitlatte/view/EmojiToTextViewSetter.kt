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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.glide.GlideRequests
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Cancellable
import io.reactivex.internal.disposables.CancellableDisposable
import net.ellerton.japng.android.api.PngAndroid
import java.io.File
import java.util.regex.Pattern

/**
 * Created by moko256 on 2018/07/20.
 *
 * @author moko256
 */

private val containsEmoji = Pattern.compile(":([a-zA-Z0-9_]{2,}):")

class EmojiToTextViewSetter(private val glideRequests: GlideRequests, private val textView: TextView) {

    fun set(text: CharSequence, emojis: List<Emoji>): Array<Disposable> {
        val disposable = ArrayList<Disposable>(2)

        val matcher = containsEmoji.matcher(text)
        val matches = matcher.matches()

        val imageSize = if (matches) {
            Math.round((textView.lineHeight * 2).toFloat())
        } else {
            Math.round(textView.lineHeight.toFloat())
        }

        val map: HashMap<String, ArrayList<Int>>

        if (matches) {
            map = HashMap(1)

            map[matcher.group(1)] = ArrayList<Int>(1).also {
                it.add(matcher.start())
            }
        } else {
            map = HashMap(emojis.size * 2)

            while (matcher.find()) {
                val shortCode = matcher.group(1)
                if (map[shortCode] == null) {
                    map[shortCode] = ArrayList()
                }

                map[shortCode]!!.add(matcher.start())
            }
        }

        val builder = SpannableStringBuilder.valueOf(text)

        val targets = ArrayList<SimpleTarget<out Any>>(emojis.size)

        val handler = Handler()

        emojis.forEach {emoji ->
            val addTextTarget = object: SimpleTarget<Drawable>(imageSize, imageSize) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val drawable = resource.mutate()
                    drawable.setBounds(0, 0, imageSize, imageSize)

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
                }
            }

            val pngConvertTarget = object : SimpleTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val inputStream = resource.inputStream()
                    try {
                        addTextTarget.onResourceReady(
                                PngAndroid.readDrawable(textView.context, inputStream),
                                null
                        )
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        targets.add(addTextTarget)
                        glideRequests
                                .load(emoji.url)
                                .into(addTextTarget)
                    }
                    inputStream.close()
                }

            }
            targets.add(pngConvertTarget)

            glideRequests
                    .asFile()
                    .load(emoji.url)
                    .into(pngConvertTarget)
        }

        disposable.add(CancellableDisposable(Cancellable {
            targets.forEach {
                glideRequests
                        .clear(it)
            }
        }))

        return disposable.toTypedArray()
    }

}