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

package com.github.moko256.twitlatte.cacheMap

import androidx.collection.LongSparseArray
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Post
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by moko256 on 2019/08/08.
 *
 * @author moko256
 */
class PostDispatcher(
        private val apiClient: ApiClient,
        private val postCache: PostCache
) {
    private val callbacks = LongSparseArray<BehaviorSubject<Post>>(8)

    private val disposable = postCache.updatedEntities.subscribe {
        for (i in 0 until callbacks.size()) {
            val subject = callbacks.valueAt(i)
            val statusObject = subject.value
            if (statusObject != null) {
                if (
                        it.statuses.containsAny(
                                statusObject.status,
                                statusObject.repeat,
                                statusObject.quotedRepeatingStatus
                        ) &&
                        it.users.containsAny(
                                statusObject.user,
                                statusObject.repeatedUser,
                                statusObject.quotedRepeatingUser
                        )
                ) {
                    postCache.getPost(statusObject.id)?.let { post ->
                        subject.onNext(post)
                    }
                }
            }
        }
    }

    private fun <T> List<T>.containsAny(vararg c: T): Boolean {
        c.forEach {
            if (contains(it)) {
                return true
            }
        }
        return false
    }

    fun getObserverAndRequestPost(postId: Long): Observable<Post> {
        val havingSubject = callbacks.get(postId)
        if (havingSubject != null) {
            return havingSubject
        }
        val newSubject = BehaviorSubject.create<Post>()
        callbacks.put(postId, newSubject)
        val disposable = Maybe
                .create<Post> {
                    try {
                        it.onSuccess(postCache.getPost(postId) ?: apiClient.showPost(postId))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        it.onComplete()
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe { newSubject.onNext(it) }
        return newSubject.doOnDispose {
            disposable.dispose()
            callbacks.get(postId)?.onComplete()
            callbacks.remove(postId)
        }.observeOn(AndroidSchedulers.mainThread())

    }

    fun close() {
        disposable.dispose()
        for (i in 0 until callbacks.size()) {
            callbacks.valueAt(i).onComplete()
            callbacks.clear()
        }
    }
}
