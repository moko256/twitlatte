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

package com.github.moko256.twitlatte.viewmodel

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.moko256.latte.client.base.ApiClient
import com.github.moko256.latte.client.base.entity.Trend
import com.github.moko256.twitlatte.database.CachedTrendsSQLiteOpenHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class TrendsViewModel : ViewModel() {
    private val disposables = CompositeDisposable()

    lateinit var dbRepo: CachedTrendsSQLiteOpenHelper
    lateinit var netRepo: ApiClient
    lateinit var geocoder: Geocoder

    val trends = MutableLiveData<List<Trend>>()
    val errors = MutableLiveData<Throwable>()

    fun load(withoutCache: Boolean) {
        disposables.add(
                Single.create<List<Trend>> {
                    try {
                        if (!withoutCache) {
                            val fromDb = dbRepo.trends
                            if (fromDb.isNotEmpty()) {
                                it.onSuccess(fromDb)
                                return@create
                            }
                        }

                        it.onSuccess(getTrendsFromNet())

                    } catch (e: Throwable) {
                        it.tryOnError(e)
                    }
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    trends.value = it
                                },
                                {
                                    errors.value = it
                                }
                        )
        )
    }

    private fun getTrendsFromNet(): List<Trend> {
        val address = getGeoLocation()
        val trends = netRepo.getClosestTrends(address.latitude, address.longitude)

        dbRepo.trends = trends

        return trends
    }

    private fun getGeoLocation(): Address {
        val locale = Locale.getDefault()
        val address = geocoder.getFromLocationName(locale.displayCountry, 1)[0]
        if (address != null) {
            return address
        } else {
            throw Exception("Cannot use trends")
        }
    }

}