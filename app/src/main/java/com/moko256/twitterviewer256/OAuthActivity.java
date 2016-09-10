package com.moko256.twitterviewer256;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * Created by moko256 on 2016/04/29.
 *
 * @author moko256
 */
public class OAuthActivity extends AppCompatActivity {
    public static RequestToken req;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Uri uri=getIntent().getData();
        if(uri != null && uri.toString().startsWith("twv256://OAuthActivity")){
            OAuthAuthorization oauth;

            Configuration conf = ConfigurationContext.getInstance();
            oauth =new OAuthAuthorization(conf);
            String verifier = uri.getQueryParameter("oauth_verifier");
            Observable
                    .create(subscriber -> {
                        try {
                            subscriber.onNext(oauth.getOAuthAccessToken(req,verifier));
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result->{
                                AccessToken accessToken=(AccessToken) result;
                                SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

                                String token = accessToken.getToken();
                                String tokenSecret = accessToken.getTokenSecret();

                                TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this);
                                long nowAccountPoint=tokenOpenHelper.setAccessToken(accessToken)-1;
                                tokenOpenHelper.close();

                                defaultSharedPreferences
                                        .edit()
                                        .putString("AccountPoint",String.valueOf(nowAccountPoint))
                                        .apply();

                                Static.twitter = new TwitterFactory().getInstance();
                                Static.twitter.setOAuthConsumer(Static.consumerKey, Static.consumerSecret);
                                Static.twitter.setOAuthAccessToken(
                                        new AccessToken(token, tokenSecret)
                                );

                                finish();
                                startActivity(new Intent(this,MainActivity.class));
                            },
                            Throwable::printStackTrace,
                            ()->{}
                    );
        }
        else {
            Configuration conf = ConfigurationContext.getInstance();
            final OAuthAuthorization oauth =new OAuthAuthorization(conf);

            oauth.setOAuthConsumer(Static.consumerKey,Static.consumerSecret);

            new AsyncTask<Void, Void, Void>() {
                @Override
                public Void doInBackground(Void... params) {
                    try {
                        req = oauth.getOAuthRequestToken("twv256://OAuthActivity");
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public void onPostExecute(Void n) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(req.getAuthorizationURL())));
                }
            }.execute();
        }
    }
}