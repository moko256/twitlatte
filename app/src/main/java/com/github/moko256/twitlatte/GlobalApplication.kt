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

package com.github.moko256.twitlatte

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Looper
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.GlideInitializer
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.twitter.okhttp.replaceOkHttpClient
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.glide.GlideModule
import com.github.moko256.twitlatte.model.ClientModel
import com.github.moko256.twitlatte.net.appOkHttpClientInstance
import com.github.moko256.twitlatte.repository.AccountsRepository
import com.github.moko256.twitlatte.repository.KEY_NIGHT_MODE
import com.github.moko256.twitlatte.repository.PreferenceRepository
import com.github.moko256.twitlatte.text.convertToAppCompatNightThemeMode
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */

private const val INTENT_CLIENT_KEY = "intent_client_key"
const val LIMIT_OF_SIZE_OF_STATUSES_LIST = 1000
const val LIMIT_OF_SIZE_OF_OBJECT_CACHE = 250

lateinit var preferenceRepository: PreferenceRepository

class GlobalApplication : Application() {

    internal lateinit var clientModel: ClientModel

    override fun onCreate() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }

        preferenceRepository = PreferenceRepository(
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(
            preferenceRepository
                .getString(KEY_NIGHT_MODE, "mode_night_no_value")
                .convertToAppCompatNightThemeMode()
        )

        replaceOkHttpClient(appOkHttpClientInstance)

        clientModel = ClientModel(
            this,
            AccountsRepository(this),
            preferenceRepository
        )

        GlideInitializer.setGlideModule(this, GlideModule())

        super.onCreate()
    }

}

fun Activity.getClientsRepository() = (application as GlobalApplication).clientModel

fun Activity.getCurrentClient() = getClientsRepository().currentClient

fun Activity.getClient(): Client? {
    return getClientsRepository().getClient(intent.getStringExtra(INTENT_CLIENT_KEY))
}

fun Intent.setAccountKey(accessToken: AccessToken) = apply {
    putExtra(INTENT_CLIENT_KEY, accessToken.getKeyString())
}

fun Intent.setAccountKeyForActivity(activity: Activity): Intent {
    activity.intent.getStringExtra(INTENT_CLIENT_KEY)?.let {
        putExtra(INTENT_CLIENT_KEY, it)
    }
    return this
}