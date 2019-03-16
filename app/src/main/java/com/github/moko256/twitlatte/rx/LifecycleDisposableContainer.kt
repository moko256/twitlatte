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

package com.github.moko256.twitlatte.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer
import io.reactivex.internal.util.OpenHashSet

/**
 * Created by moko256 on 2019/03/16.
 *
 * @author moko256
 */

class LifecycleDisposableContainer(lifecycleOwner: LifecycleOwner, vararg initials: Disposable): LifecycleEventObserver, DisposableContainer {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private var disposables: OpenHashSet<Disposable>? =
            if (initials.isEmpty()) {
                OpenHashSet()
            } else {
                OpenHashSet<Disposable>(initials.size).also { set ->
                    initials.forEach { set.add(it) }
                }
            }

    override fun add(d: Disposable): Boolean {
        return disposables?.add(d)?:false
    }

    fun addAll(vararg d: Disposable): Boolean {
        val set = disposables
        return if (set != null) {
            d.forEach { set.add(it) }
            true
        } else {
            false
        }
    }

    override fun remove(d: Disposable): Boolean {
        d.dispose()
        return disposables?.remove(d)?:false
    }

    override fun delete(d: Disposable): Boolean {
        return disposables?.remove(d)?:false
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            disposables
                    ?.keys()
                    ?.filter { it is Disposable && !it.isDisposed }
                    ?.map { it as Disposable }
                    ?.forEach { it.dispose() }
            disposables = null
        }
    }
}