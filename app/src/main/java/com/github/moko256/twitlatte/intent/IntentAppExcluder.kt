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

package com.github.moko256.twitlatte.intent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.github.moko256.twitlatte.R

/**
 * Created by moko256 on 2018/12/14.
 *
 * @author moko256
 */

@SuppressLint("PrivateResource")
fun Intent.excludeOwnApp(context: Context, packageManager: PackageManager): Intent = run {
    val intents = packageManager
            .queryIntentActivities(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PackageManager.MATCH_ALL
                    } else {
                        PackageManager.MATCH_DEFAULT_ONLY
                    }
            )
            .also {
                if (it.size == 1) {
                    it.addAll(
                            packageManager
                                    .queryIntentActivities(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://")),
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                PackageManager.MATCH_ALL
                                            } else {
                                                PackageManager.MATCH_DEFAULT_ONLY
                                            }
                                    )
                    )
                }
            }
            .asSequence()
            .map { it.activityInfo.packageName }
            .distinct()
            .filter {
                it != context.packageName
            }.map {
                Intent(this).setPackage(it)
            }.toMutableList()

    val openWithText = context.getString(R.string.abc_activitychooserview_choose_application)

    when {
        intents.isEmpty() -> {
            Intent.createChooser(Intent(), openWithText)
        }
        intents.size == 1 -> {
            intents[0]
        }
        else -> {
            Intent.createChooser(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent()
                    } else {
                        intents.removeAt(0)
                    },
                    openWithText
            ).putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        }
    }
}