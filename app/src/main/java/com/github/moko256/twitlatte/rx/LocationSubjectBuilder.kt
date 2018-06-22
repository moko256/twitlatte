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
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by moko256 on 2018/06/07.
 *
 * @author moko256
 */
class LocationSubjectBuilder(private val locationManager: LocationManager?): LocationListener {
    val subject = PublishSubject.create<Location>()

    fun start(criteria: Criteria): Observable<Location> {
        if (locationManager != null) {
            try {
                locationManager.requestSingleUpdate(
                        locationManager.getBestProvider(criteria, true), this, null
                )
            } catch (e: SecurityException) {
                subject.onError(e)
            }

            subject.doOnDispose {
                locationManager.removeUpdates(this)
            }
        } else {
            subject.onError(NullPointerException("Unable to get location: got service was null"))
        }

        return subject
    }

    override fun onLocationChanged(location: Location?) {
        if (locationManager != null && location != null) {
            locationManager.removeUpdates(this)
            subject.onNext(location)
        } else {
            subject.onError(NullPointerException("Unable to get location: got location was null"))
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){} //Do nothing

    override fun onProviderEnabled(provider: String?) {} //Do nothing

    override fun onProviderDisabled(provider: String?) {} //Do nothing
}