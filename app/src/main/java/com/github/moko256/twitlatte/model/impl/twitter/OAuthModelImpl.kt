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

package com.github.moko256.twitlatte.model.impl.twitter

import android.os.Bundle
import com.github.moko256.twitlatte.database.TokenSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.Type
import com.github.moko256.twitlatte.model.base.OAuthModel
import io.reactivex.Single
import twitter4j.TwitterException
import twitter4j.auth.OAuthAuthorization
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationContext

/**
 * Created by moko256 on 2017/10/29.
 *
 * @author moko256
 */
class OAuthModelImpl(override var isRestartable: Boolean = false) : OAuthModel {
    private val STATE_MODEL_CLIENT_KEY = "state_model_client_key"
    private val STATE_MODEL_CLIENT_SECRET = "state_model_client_secret"

    private var req: RequestToken? = null
    private val oauth = OAuthAuthorization(ConfigurationContext.getInstance())

    override fun restoreInstanceState(savedInstanceState: Bundle) {
        req = RequestToken(
                savedInstanceState.getString(STATE_MODEL_CLIENT_KEY),
                savedInstanceState.getString(STATE_MODEL_CLIENT_SECRET)
        )
    }

    override fun saveInstanceState(outState: Bundle) {
        if (isRestartable) {
            outState.apply {
                putString(STATE_MODEL_CLIENT_KEY, req?.token)
                putString(STATE_MODEL_CLIENT_SECRET, req?.tokenSecret)
            }
        }
    }

    override fun getCallbackAuthUrl(url: String, consumerKey: String, consumerSecret: String, callbackUrl: String): Single<String>
            = getAuth(consumerKey, consumerSecret, callbackUrl)

    override fun getCodeAuthUrl(url: String, consumerKey: String, consumerSecret: String): Single<String>
            = getAuth(consumerKey, consumerSecret)

    private fun getAuth(consumerKey: String, consumerSecret: String, callbackUrl: String? = "oob"): Single<String> {
        oauth.setOAuthConsumer(consumerKey, consumerSecret)

        return Single.create {
            try {
                req = oauth.getOAuthRequestToken(callbackUrl)
                it.onSuccess(req?.authorizationURL!!)
                isRestartable = true
            } catch (e: TwitterException) {
                it.tryOnError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                val accessToken = oauth.getOAuthAccessToken(req, pin)
                it.onSuccess(AccessToken(
                        Type.TWITTER,
                        TokenSQLiteOpenHelper.TWITTER_URL,
                        accessToken.userId,
                        accessToken.screenName,
                        accessToken.token,
                        accessToken.tokenSecret
                ))
                isRestartable = false
            } catch (e: TwitterException) {
                it.tryOnError(e)
            }
        }
    }

}