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

package com.github.moko256.twitlatte.model.impl

import android.content.Context
import com.github.moko256.latte.client.base.entity.Status
import com.github.moko256.latte.client.base.entity.StatusAction
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap
import com.github.moko256.twitlatte.intent.launchChromeCustomTabs
import com.github.moko256.twitlatte.model.base.StatusActionModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class OpenInBrowserStatusActionModelImpl(
        private val statusCacheMap: StatusCacheMap,
        private val context: Context
) : StatusActionModel {
    private val observable = PublishSubject.create<Long>()

    override fun getDidActionObservable(): Observable<StatusAction> = Observable.empty()

    override fun getStatusObservable(): Observable<Long> = observable

    override fun getErrorObservable(): Observable<Throwable> = Observable.empty()

    override fun updateStatus(targetStatusId: Long) {
        openInBrowser(targetStatusId)
    }

    override fun createFavorite(targetStatusId: Long) {
        openInBrowser(targetStatusId)
    }

    override fun removeFavorite(targetStatusId: Long) {
        openInBrowser(targetStatusId)
    }

    override fun createRepeat(targetStatusId: Long) {
        openInBrowser(targetStatusId)
    }

    override fun removeRepeat(targetStatusId: Long) {
        openInBrowser(targetStatusId)
    }

    override fun sendVote(targetStatusId: Long, targetPollId: Long, options: List<Int>) {
        openInBrowser(targetStatusId)
    }

    private fun openInBrowser(targetStatusId: Long) {
        observable.onNext(targetStatusId)
        (statusCacheMap.get(targetStatusId) as Status?)?.url?.let {
            launchChromeCustomTabs(context, it, true)
        }
    }
}
