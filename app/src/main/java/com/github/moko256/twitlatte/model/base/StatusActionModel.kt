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

package com.github.moko256.twitlatte.model.base

import androidx.lifecycle.LiveData
import com.github.moko256.latte.client.base.entity.StatusAction

/**
 * Created by moko256 on 2018/10/20.
 *
 * @author moko256
 */
interface StatusActionModel {
    fun getDidActionObservable(): LiveData<StatusAction>
    fun getStatusObservable(): LiveData<Long>
    fun getErrorObservable(): LiveData<Throwable>

    fun updateStatus(targetStatusId: Long)
    fun createFavorite(targetStatusId: Long)
    fun removeFavorite(targetStatusId: Long)
    fun createRepeat(targetStatusId: Long)
    fun removeRepeat(targetStatusId: Long)

    fun sendVote(targetStatusId: Long, targetPollId: Long, options: List<Int>)
}