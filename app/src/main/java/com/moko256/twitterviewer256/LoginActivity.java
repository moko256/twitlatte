package com.moko256.twitterviewer256;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedHashSet;
import java.util.Set;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by moko256 on GitHub on 2015/12/23.
 */
public class LoginActivity extends AppCompatActivity {

    public static RequestToken req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AccessToken accessToken=(AccessToken) getIntent().getSerializableExtra("AccessToken");
        if (accessToken==null){
            finish();
            return;
        }

        SharedPreferences defaultSharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        int nowAccountPoint=Integer.valueOf(defaultSharedPreferences.getString("AccountPoint","-1"));
        Set<String> accountsIdLongStrSet=defaultSharedPreferences.getStringSet("AccountsList", new LinkedHashSet<>());

        String token = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();

        nowAccountPoint++;

        accountsIdLongStrSet.add(String.valueOf(nowAccountPoint));

        defaultSharedPreferences
                .edit()
                .putString("AccountPoint",String.valueOf(nowAccountPoint))
                .putStringSet("AccountsList",accountsIdLongStrSet)
                .apply();

        getSharedPreferences(String.valueOf(accessToken.getUserId()), MODE_PRIVATE)
                .edit()
                .putString("token", token)
                .putString("token_secret", tokenSecret)
                .apply();

        Static.twitter = new TwitterFactory().getInstance();
        Static.twitter.setOAuthConsumer(Static.consumerKey, Static.consumerSecret);
        Static.twitter.setOAuthAccessToken(
                new AccessToken(token, tokenSecret)
        );

        startActivity(new Intent(this,MainActivity.class));
    }

}
