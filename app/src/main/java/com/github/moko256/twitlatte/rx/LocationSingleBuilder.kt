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

package com.github.moko256.twitlatte.rx

import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import io.reactivex.Single
import io.reactivex.SingleEmitter

/**
 * Created by moko256 on 2018/06/07.
 *
 * @author moko256
 */
class LocationSingleBuilder(
        private val locationManager: LocationManager,
        private val criteria: Criteria
): LocationListener {

    private lateinit var emitter: SingleEmitter<Location>

    val single = Single.create<Location> {
        try {
            locationManager.requestSingleUpdate(
                    locationManager.getBestProvider(criteria, true), this, null
            )
        } catch (e: SecurityException) {
            it.tryOnError(e)
            return@create
        }

        emitter = it
    }.doOnDispose {
        locationManager.removeUpdates(this)
    }!!

    override fun onLocationChanged(location: Location?) {
        locationManager.removeUpdates(this)

        if (location != null) {
            emitter.onSuccess(location)
        } else {
            emitter.tryOnError(NullPointerException("Unable to get location: got location was null"))
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){} //Do nothing

    override fun onProviderEnabled(provider: String?) {} //Do nothing

    override fun onProviderDisabled(provider: String?) {} //Do nothing
}