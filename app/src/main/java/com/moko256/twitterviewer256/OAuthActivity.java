package com.moko256.twitterviewer256;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * Created by moko256 on GitHub on 2016/04/29.
 */
public class OAuthActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Configuration conf = ConfigurationContext.getInstance();
        final OAuthAuthorization oauth =new OAuthAuthorization(conf);

        oauth.setOAuthConsumer(Static.consumerKey,Static.consumerSecret);

        new AsyncTask<Void, Void, RequestToken>() {

            @Override
            protected RequestToken doInBackground(Void... params) {
                RequestToken _req=null;
                try {
                    _req = oauth.getOAuthRequestToken("twv256://CallBackActivity");
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return _req;
            }

            @Override
            protected void onPostExecute(RequestToken _req) {
                CallBackActivity.req=_req;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_req.getAuthorizationURL())));
            }
        }.execute();
    }
}
