/*
 * Copyright 2018 The twicalico authors
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

package com.github.moko256.twicalico.intent

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.github.moko256.twicalico.R

/**
 * Created by moko256 on 2018/01/21.
 *
 * @author moko256
 */

/**
 * Launch URL from context with Chrome Custom Tabs
 *
 * @param context context that launch uri
 * @param uri uri string
 */
fun launchChromeCustomTabs(context: Context, uri: String){
    CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.color_primary))
            .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.color_primary_dark))
            .addDefaultShareMenuItem()
            .build()
            .launchUrl(context, Uri.parse(uri))
}