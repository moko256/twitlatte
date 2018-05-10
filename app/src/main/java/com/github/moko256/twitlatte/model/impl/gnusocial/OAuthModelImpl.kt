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

package com.github.moko256.twitlatte.model.impl.gnusocial

import com.github.moko256.twitlatte.entity.AccessToken
import com.github.moko256.twitlatte.entity.Type
import com.github.moko256.twitlatte.model.base.OAuthModel
import rx.Single
import twitter4j.TwitterException
import twitter4j.auth.OAuthAuthorization
import twitter4j.auth.RequestToken
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder

/**
 * Created by moko256 on 2018/05/02.
 *
 * @author moko256
 */
class OAuthModelImpl : OAuthModel {
    private var req: RequestToken? = null
    private lateinit var url: String
    private lateinit var conf: Configuration
    private lateinit var oauth: OAuthAuthorization

    fun createConfig(url: String, consumerKey: String, consumerSecret: String) = ConfigurationBuilder()
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthRequestTokenURL("https://$url/api/oauth/request_token")
            .setOAuthAuthorizationURL("https://$url/api/oauth/authorize")
            .setOAuthAccessTokenURL("https://$url/api/oauth/access_token")
            .setOAuthAuthenticationURL("https://$url/api/oauth/authorize")
            .setOAuth2TokenURL("")
            .setOAuth2InvalidateTokenURL("")
            .setRestBaseURL("https://$url/api/")
            .setStreamBaseURL("")
            .setUserStreamBaseURL("")
            .setSiteStreamBaseURL("")
            .setUploadBaseURL("https://$url/api/statusnet/media/upload/")
            .build()

    override fun getCallbackAuthUrl(url: String, consumerKey: String, consumerSecret: String, callbackUrl: String): Single<String> {
        this.url = url

        conf = createConfig(url, consumerKey, consumerSecret)

        oauth = OAuthAuthorization(conf)
        oauth.setOAuthConsumer(consumerKey, consumerSecret)

        return Single.create {
            try {
                req = oauth.getOAuthRequestToken("$callbackUrl://OAuthActivity")
                it.onSuccess(req?.authenticationURL)
            } catch (e: Throwable) {
                it.onError(e)
            }
        }
    }

    override fun getCodeAuthUrl(url: String, consumerKey: String, consumerSecret: String): Single<String> {
        this.url = url

        conf = createConfig(url, consumerKey, consumerSecret)

        oauth = OAuthAuthorization(conf)
        oauth.setOAuthConsumer(consumerKey, consumerSecret)

        return Single.create {
            try {
                req = oauth.getOAuthRequestToken("oob")
                it.onSuccess(req?.authenticationURL)
            } catch (e: TwitterException) {
                it.onError(e)
            }
        }
    }

    override fun initToken(pin: String): Single<AccessToken> {
        return Single.create {
            try {
                val accessToken = oauth.getOAuthAccessToken(req, pin)
                it.onSuccess(AccessToken(
                        Type.GNU_SOCIAL,
                        url,
                        accessToken.userId,
                        accessToken.screenName,
                        accessToken.token,
                        accessToken.tokenSecret
                ))
            } catch (e: TwitterException) {
                it.onError(e)
            }
        }
    }

}