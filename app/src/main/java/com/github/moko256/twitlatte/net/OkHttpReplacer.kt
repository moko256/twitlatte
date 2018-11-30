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

package com.github.moko256.twitlatte.net

import android.os.Build
import okhttp3.OkHttpClient
import twitter4j.AlternativeHttpClientImpl
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by moko256 on 2018/11/29.
 *
 * @author moko256
 */

fun AlternativeHttpClientImpl.replaceOkHttpClient(newOkHttpClient: OkHttpClient) {
    AlternativeHttpClientImpl::class.java
            .getDeclaredField("okHttpClient")
            .also {
                it.isAccessible = true

                it.set(this, newOkHttpClient)
            }
}

fun OkHttpClient.Builder.replaceSocketFactoryIfNeeded(): OkHttpClient.Builder = apply {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        try {

            val trustManager = systemDefaultTrustManager()

            sslSocketFactory(
                    SSLSocketFactoryCompat(trustManager),
                    trustManager
            )

        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
    }
}

@Throws(NoSuchAlgorithmException::class, KeyStoreException::class, IllegalStateException::class)
private fun systemDefaultTrustManager() =
        TrustManagerFactory
                .getInstance(
                        TrustManagerFactory.getDefaultAlgorithm()
                )
                .apply {
                    init(null as KeyStore?)
                }
                .trustManagers
                .find { it is X509TrustManager}
                .let {
                    if (it != null) {
                        it as X509TrustManager
                    } else {
                        throw IllegalStateException("Unexpected default trust managers")
                    }
                }