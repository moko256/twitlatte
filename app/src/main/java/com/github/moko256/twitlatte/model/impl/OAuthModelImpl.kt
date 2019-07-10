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

import android.os.Bundle
import com.github.moko256.latte.client.base.AuthApiClient
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.latte.client.base.entity.RequestToken
import com.github.moko256.twitlatte.model.base.OAuthModel
import io.reactivex.Single

/**
 * Created by moko256 on 2018/12/06.
 *
 * @author moko256
 */
private const val BUNDLE_REQUEST_TOKEN = "request_token"

class OAuthModelImpl(private val client: AuthApiClient): OAuthModel {
    override var isRestartable: Boolean = false

    private lateinit var requestToken: RequestToken

    override fun restoreInstanceState(savedInstanceState: Bundle) {
        requestToken = savedInstanceState.get(BUNDLE_REQUEST_TOKEN) as RequestToken

        isRestartable = true
    }

    override fun saveInstanceState(outState: Bundle) {
        if (isRestartable) {
            outState.apply {
                putSerializable(BUNDLE_REQUEST_TOKEN, requestToken)
            }
        }
    }

    override fun getAuthUrl(
            optionalConsumerKey: String?,
            optionalConsumerSecret: String?,
            url: String,
            callbackUrl: String?
    ): Single<String> {
        return Single.create {
            try {
                requestToken = client.getOAuthRequestToken(
                        optionalConsumerKey,
                        optionalConsumerSecret,
                        url,
                        callbackUrl
                )

                it.onSuccess(requestToken.authUrl)
                isRestartable = true
            } catch (e: Throwable) {
                it.tryOnError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                it.onSuccess(client.initializeToken(requestToken, pin))
                isRestartable = false
            } catch (e: Throwable) {
                it.tryOnError(e)
            }
        }
    }
}