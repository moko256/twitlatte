package com.moko256.twitterviewer256;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * Created by moko256 on GitHub on 2015/12/23.
 */
public class CallBackActivity extends AppCompatActivity {

    public static RequestToken req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);

        final Uri uri = getIntent().getData();
        final Context that=this;

        if(uri != null && uri.toString().startsWith("twv256://CallBackActivity")){

            new AsyncTask<Void, Void, Void>() {
                AccessToken token = null;
                @Override
                protected Void doInBackground(Void... params) {

                    try {
                        Configuration conf = ConfigurationContext.getInstance();
                        OAuthAuthorization oauth =new OAuthAuthorization(conf);
                        String verifier = uri.getQueryParameter("oauth_verifier");
                        token = oauth.getOAuthAccessToken(req,verifier);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                        finish();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void n){
                    Static.token=token.getToken();
                    Static.tokenSecret=token.getTokenSecret();
                    that.getSharedPreferences(Static.nowUserDataFile ,MODE_PRIVATE)
                            .edit()
                            .putString("token",Static.token)
                            .putString("token_secret",Static.tokenSecret)
                            .apply();

                    finish();
                }

            }.execute();

        }

    }

}
