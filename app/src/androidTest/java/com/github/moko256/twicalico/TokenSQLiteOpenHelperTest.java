package com.github.moko256.twicalico;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2017/03/04.
 *
 * @author moko256
 */

public class TokenSQLiteOpenHelperTest extends ApplicationTestCase<Application> {

    private static final long TEST_USER_1_USER_ID = 1L;

    private static final String TEST_USER_1_USER_SCREEN_NAME_1 = "testUser1";
    private static final String TEST_USER_1_USER_TOKEN_1 = "token1";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_1 = "token_secret1";

    private static final String TEST_USER_1_USER_SCREEN_NAME_2 = "testUser1_renamed";
    private static final String TEST_USER_1_USER_TOKEN_2 = "token2";
    private static final String TEST_USER_1_USER_TOKEN_SECRET_2 = "token_secret2";

    public TokenSQLiteOpenHelperTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TokenSQLiteOpenHelper helper = new TokenSQLiteOpenHelper(new RenamingDelegatingContext(getContext(), "test_"));

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



        final long deleteAccessTokenResult = helper.deleteAccessToken(TEST_USER_1_USER_ID);
        assertEquals(deleteAccessTokenResult, 0);

        helper.close();
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
