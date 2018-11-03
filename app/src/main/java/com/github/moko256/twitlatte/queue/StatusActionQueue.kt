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

package com.github.moko256.twitlatte.queue

import com.github.moko256.twitlatte.entity.StatusAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Created by moko256 on 2018/10/21.
 *
 * @author moko256
 */
class StatusActionQueue(
        private val queueCount: Int = 20,
        private val delay: Long = 2L,
        private val unit: TimeUnit = TimeUnit.MINUTES,
        private val doImmediateFirst: Boolean = true
) {
    private val queue = ArrayBlockingQueue<QueueEntity>(queueCount, true)
    private var disposable: Disposable? = null

    private val doneIds = ArrayList<Pair<Long, Action>>(20)

    fun add(id: Long, statusAction: StatusAction, function: (Long) -> Unit): Completable {
        val (action, willDo) = statusAction.notifyAction { action, b -> action to b }

        val pair = id to action
        if (doImmediateFirst && !doneIds.contains(pair)) {
            if (doneIds.size == 20) {
                doneIds.removeAt(19)
            }
            doneIds.add(0, pair)
            return Completable.create {
                try {
                    function(id)
                    it.onComplete()
                } catch (e: Throwable) {
                    it.tryOnError(e)
                }
            }.subscribeOn(Schedulers.io())
        }

        val subject = addIfNoConflict(id, action, willDo, function)

        if (disposable == null) {
            disposable = Observable.interval(delay, queueCount * delay, unit)
                    .subscribe {
                        queue.poll().let { queueEntity ->
                            if (queueEntity == null) {
                                removeDisposable()
                            } else {
                                Completable.create { emitter ->
                                    try {
                                        queueEntity.function(queueEntity.id)
                                        emitter.onComplete()
                                    } catch (e: Throwable) {
                                        emitter.tryOnError(e)
                                    }
                                }.subscribeOn(Schedulers.io()).subscribe(
                                        {
                                            queueEntity.resultSubject.onComplete()
                                        },
                                        { error ->
                                            addIfNoConflict(queueEntity.id, queueEntity.action, queueEntity.willDo, queueEntity.function)
                                            queueEntity.resultSubject.onError(error)
                                        }
                                )
                            }
                        }
                    }
        } else if (queue.isEmpty()) {
            removeDisposable()
        }

        return subject
    }

    private fun removeDisposable() {
        disposable?.dispose()
        disposable = null
    }

    private fun addIfNoConflict(id: Long, action: Action, willDo: Boolean, function: (Long) -> Unit): CompletableSubject {
        return queue.singleOrNull {
            it.id == id && it.action == action
        }.let { queueEntity ->
            if (queueEntity == null) {
                val subject = CompletableSubject.create()
                try {
                    queue.add(QueueEntity(id, action, willDo, function, subject))
                } catch (e: IllegalStateException) {
                    subject.onError(e)
                }
                subject
            } else {
                if (queueEntity.willDo == willDo) {
                    // Already added same action
                    queueEntity.resultSubject
                } else {
                    // Conflict action
                    queue.remove(queueEntity)
                    queueEntity.resultSubject.onComplete()
                    queueEntity.resultSubject
                }
            }
        }
    }

}

private data class QueueEntity(
        val id: Long,
        val action: Action,
        val willDo: Boolean,
        val function: (Long) -> Unit,
        val resultSubject: CompletableSubject
)

private enum class Action {
    FAVORITE, REPEAT
}

private inline fun <R> StatusAction.notifyAction(callback: (Action, Boolean) -> R): R {
    return when (this) {
        StatusAction.FAVORITE -> callback(Action.FAVORITE, true)
        StatusAction.UNFAVORITE -> callback(Action.FAVORITE, false)
        StatusAction.REPEAT -> callback(Action.REPEAT, true)
        StatusAction.UNREPEAT -> callback(Action.REPEAT, false)
    }
}