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

import com.github.moko256.twitlatte.converter.convertToCommonUser
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.entity.User
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import twitter4j.TwitterException

/**
 * Created by moko256 on 2018/08/09.
 *
 * @author moko256
 */
class VerifyCredentialOnSubscribe(
        private val client: Client
): SingleOnSubscribe<User> {
    override fun subscribe(emitter: SingleEmitter<User>) {
        try {
            var me = client.userCache[client.accessToken.userId]
            if (me == null) {
                me = client.twitter.verifyCredentials().convertToCommonUser()
                client.userCache.add(me)
            }
            emitter.onSuccess(me)
        } catch (e: TwitterException) {
            emitter.tryOnError(e)
        }

    }
}