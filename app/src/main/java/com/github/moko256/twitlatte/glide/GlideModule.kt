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

package com.github.moko256.twitlatte.glide

import android.content.Context
import android.graphics.drawable.InsetDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.github.moko256.twitlatte.GlobalApplication
import com.github.moko256.twitlatte.R
import java.io.InputStream

/**
 * Created by moko256 on 2017/07/31.
 *
 * @author moko256
 */

@com.bumptech.glide.annotation.GlideModule
class GlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        var error = AppCompatResources.getDrawable(context, R.drawable.ic_cloud_off_black_24dp)
        if (error != null) {
            val padding = Math.round(8 * context.resources.displayMetrics.density)
            DrawableCompat.setTint(error, ContextCompat.getColor(context, R.color.color_accent))
            error = InsetDrawable(error, padding)
            builder.setDefaultRequestOptions(RequestOptions()
                    .error(error)
            )
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(
                GlideUrl::class.java,
                InputStream::class.java,
                OkHttpUrlLoader.Factory {request ->
                    GlobalApplication
                            .getOkHttpClient(context)
                            .newCall(request)
                }
        )
    }

    override fun isManifestParsingEnabled(): Boolean = false

}
