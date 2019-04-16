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

package com.github.moko256.twitlatte.view

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.github.moko256.latte.client.base.entity.Emoji
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.glide.GlideRequests
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Cancellable
import io.reactivex.internal.disposables.CancellableDisposable
import io.reactivex.schedulers.Schedulers
import net.ellerton.japng.android.api.PngAndroid
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by moko256 on 2018/07/20.
 *
 * @author moko256
 */

class EmojiToTextViewSetter(
        glideRequests: GlideRequests,
        private val textView: TextView,
        text: CharSequence,
        emojis: Array<Emoji>
) : Disposable, LifecycleEventObserver, Drawable.Callback {

    private companion object {
        private val containsEmoji = ":([a-zA-Z0-9_]{2,}):".toPattern()
    }

    private var disposable: List<Disposable>?

    init {

        val matcher = containsEmoji.matcher(text)
        val matches = matcher.matches()

        val imageSize = if (matches) {
            textView.lineHeight * 2
        } else {
            textView.lineHeight
        }.toFloat()

        val map: Map<String, List<Int>> = if (matches) {
            Collections.singletonMap(matcher.group(1), listOf(matcher.start()))
        } else {
            var find = matcher.find()
            if (find) {
                HashMap<String, ArrayList<Int>>(emojis.size * 2).also { map ->
                    while (find) {
                        val shortCode = matcher.group(1)

                        map[shortCode].let {
                            it ?: ArrayList<Int>().also { newList ->
                                map[shortCode] = newList
                            }
                        }.add(matcher.start())

                        find = matcher.find()
                    }
                }
            } else {
                emptyMap()
            }
        }

        val containedEmoji = emojis.filter { map.containsKey(it.shortCode) }

        disposable = if (containedEmoji.isNotEmpty()) {
            val disposable = ArrayList<Disposable>(2)

            val builder = if (text is Spannable) {
                text
            } else {
                SpannableString(text)
            }

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
                                it.onNext(
                                        emoji to
                                                try {
                                                    glideRequests
                                                            .load(emoji.url)
                                                            .submit()
                                                            .get()
                                                } catch (e: Throwable) {
                                                    e.printStackTrace()
                                                    ContextCompat.getDrawable(
                                                            textView.context,
                                                            R.drawable.ic_cloud_off_black_24dp
                                                    )!!
                                                }
                                )
                            }
                        }
                        it.onComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { pair ->
                        val emoji = pair.first
                        val drawable = pair.second.mutate()

                        val aspect = drawable.intrinsicWidth / drawable.intrinsicHeight
                        drawable.setBounds(
                                0, 0,
                                Math.round(
                                        if (aspect == 1) {
                                            imageSize
                                        } else {
                                            imageSize * aspect
                                        }
                                ),
                                Math.round(imageSize)
                        )

                        map[emoji.shortCode]?.forEach {
                            builder.setSpan(
                                    ImageSpan(drawable),
                                    it,
                                    it + emoji.shortCode.length + 2,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        if (drawable is Animatable) {
                            drawable.callback = this
                            disposable.add(CancellableDisposable(Cancellable {
                                drawable.stop()
                                drawable.callback = null
                            }))
                            drawable.start()
                        }

                        textView.text = builder
                    })
            disposable
        } else {
            null
        }
    }


    private val handler = Handler(Looper.getMainLooper())

    override fun invalidateDrawable(who: Drawable) {
        textView.invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        handler.postAtTime(what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        handler.removeCallbacks(what)
    }


    override fun isDisposed() = disposable == null

    override fun dispose() {
        disposable?.let { disposable ->
            disposable.forEach {
                it.dispose()
            }
            this.disposable = null
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            dispose()
        }
    }

    fun bindToLifecycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }
}