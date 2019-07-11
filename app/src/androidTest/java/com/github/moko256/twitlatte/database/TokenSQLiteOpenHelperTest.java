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

package com.github.moko256.twitlatte.database;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.github.moko256.latte.client.base.entity.AccessToken;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Created by moko256 on 2017/03/04.
 *
 * @author moko256
 */
@RunWith(AndroidJUnit4.class)
public class TokenSQLiteOpenHelperTest {

    private TokenSQLiteOpenHelper helper = new TokenSQLiteOpenHelper(
            ApplicationProvider.getApplicationContext(),
            null
    );

    private static final long TEST_USER_1_USER_ID = 1L;

    private static final String TEST_USER_1_USER_SCREEN_NAME_1 = "testUser1";
    private static final String TEST_USER_1_USER_TOKEN_1 = "token1";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_1 = "token_secret1";

    private static final String TEST_USER_1_USER_SCREEN_NAME_2 = "testUser1_renamed";
    private static final String TEST_USER_1_USER_TOKEN_2 = "token2";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_2 = "token_secret2";

    @Test
    public void test() {
        try {
            helper.getWritableDatabase().delete("AccountTokenList", null, null);
        } catch (Exception e) {
            //Do nothing
        }

        addToken();
        updateToken();
        deleteToken();
        helper.close();
    }

    private void addToken(){
        AccessToken accessToken = generateAccessToken(
                TEST_USER_1_USER_ID,
                TEST_USER_1_USER_SCREEN_NAME_1,
                TEST_USER_1_USER_TOKEN_1,
                TEST_USER_1_USER_TOKEN_SECRET_1
        );

        helper.addAccessToken(accessToken);

        final long addAccessTokenResult = getSize();

        assertEquals(addAccessTokenResult, 1);

        AccessToken addedAccessTokenResult = helper.getAccessToken(accessToken.getKeyString());

        assertEquals(addedAccessTokenResult.getUserId(), TEST_USER_1_USER_ID);
        assertEquals(addedAccessTokenResult.getScreenName(), TEST_USER_1_USER_SCREEN_NAME_1);
        assertEquals(addedAccessTokenResult.getToken(), TEST_USER_1_USER_TOKEN_1);
        assertEquals(addedAccessTokenResult.getTokenSecret(), TEST_USER_1_USER_TOKEN_SECRET_1);
    }

    private void updateToken(){

        AccessToken accessToken = generateAccessToken(
                TEST_USER_1_USER_ID,
                TEST_USER_1_USER_SCREEN_NAME_2,
                TEST_USER_1_USER_TOKEN_2,
                TEST_USER_1_USER_TOKEN_SECRET_2
        );

        helper.addAccessToken(
                accessToken
        );

        final long updateAccessTokenResult = getSize();

        assertEquals(updateAccessTokenResult, 1);

        AccessToken updatedAccessTokenResult = helper.getAccessToken(accessToken.getKeyString());

        assertEquals(updatedAccessTokenResult.getScreenName(), TEST_USER_1_USER_SCREEN_NAME_2);
        assertEquals(updatedAccessTokenResult.getToken(), TEST_USER_1_USER_TOKEN_2);
        assertEquals(updatedAccessTokenResult.getTokenSecret(), TEST_USER_1_USER_TOKEN_SECRET_2);
    }

    private void deleteToken(){
        helper.deleteAccessToken(
                generateAccessToken(
                        TEST_USER_1_USER_ID,
                        "",
                        "",
                        ""
                )
        );

        final long deleteAccessTokenResult = getSize();

        assertEquals(deleteAccessTokenResult, 0);
    }

    private AccessToken generateAccessToken(final long userId, final String screenName, final String token, final String tokenSecret){
        return new AccessToken(1, "example.com", userId, screenName, "", "", token, tokenSecret);
    }

    private long getSize(){
        long count;
        synchronized (this) {
            SQLiteDatabase database = helper.getReadableDatabase();
            count = DatabaseUtils.queryNumEntries(database, "AccountTokenList");
            database.close();
        }
        return count;
    }
}
