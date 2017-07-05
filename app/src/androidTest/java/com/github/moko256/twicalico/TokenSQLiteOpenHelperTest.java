/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import twitter4j.auth.AccessToken;

import static org.junit.Assert.*;

/**
 * Created by moko256 on 2017/03/04.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class TokenSQLiteOpenHelperTest {

    private TokenSQLiteOpenHelper helper = new TokenSQLiteOpenHelper(InstrumentationRegistry.getTargetContext());

    private static final long TEST_USER_1_USER_ID = 1L;

    private static final String TEST_USER_1_USER_SCREEN_NAME_1 = "testUser1";
    private static final String TEST_USER_1_USER_TOKEN_1 = "token1";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_1 = "token_secret1";

    private static final String TEST_USER_1_USER_SCREEN_NAME_2 = "testUser1_renamed";
    private static final String TEST_USER_1_USER_TOKEN_2 = "token2";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_2 = "token_secret2";

    @Test
    public void test() throws Exception {
        addToken();
        updateToken();
        deleteToken();
        helper.close();
    }

    private void addToken(){
        final long addAccessTokenResult = helper.addAccessToken(
                generateAccessToken(
                        TEST_USER_1_USER_ID,
                        TEST_USER_1_USER_SCREEN_NAME_1,
                        TEST_USER_1_USER_TOKEN_1,
                        TEST_USER_1_USER_TOKEN_SECRET_1
                )
        );

        assertEquals(addAccessTokenResult, 1);

        AccessToken addedAccessTokenResult = helper.getAccessToken(0);

        assertEquals(addedAccessTokenResult.getUserId(), TEST_USER_1_USER_ID);
        assertEquals(addedAccessTokenResult.getScreenName(), TEST_USER_1_USER_SCREEN_NAME_1);
        assertEquals(addedAccessTokenResult.getToken(), TEST_USER_1_USER_TOKEN_1);
        assertEquals(addedAccessTokenResult.getTokenSecret(), TEST_USER_1_USER_TOKEN_SECRET_1);
    }

    private void updateToken(){
        final long updateAccessTokenResult = helper.addAccessToken(
                generateAccessToken(
                        TEST_USER_1_USER_ID,
                        TEST_USER_1_USER_SCREEN_NAME_2,
                        TEST_USER_1_USER_TOKEN_2,
                        TEST_USER_1_USER_TOKEN_SECRET_2
                )
        );

        assertEquals(updateAccessTokenResult, 1);

        AccessToken updatedAccessTokenResult = helper.getAccessToken(0);

        assertEquals(updatedAccessTokenResult.getScreenName(), TEST_USER_1_USER_SCREEN_NAME_2);
        assertEquals(updatedAccessTokenResult.getToken(), TEST_USER_1_USER_TOKEN_2);
        assertEquals(updatedAccessTokenResult.getTokenSecret(), TEST_USER_1_USER_TOKEN_SECRET_2);
    }

    private void deleteToken(){
        final long deleteAccessTokenResult = helper.deleteAccessToken(TEST_USER_1_USER_ID);
        assertEquals(deleteAccessTokenResult, 0);
    }

    private AccessToken generateAccessToken(final long userId, final String screenName, final String token, final String tokenSecret){
        return new AccessToken(token, tokenSecret){
            @Override
            public String getScreenName() {
                return screenName;
            }

            @Override
            public long getUserId() {
                return userId;
            }
        };
    }
}
