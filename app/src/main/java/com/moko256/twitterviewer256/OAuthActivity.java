package com.moko256.twitterviewer256;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * Created by moko256 on GitHub on 2016/04/29.
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
                                Intent intent=new Intent(OAuthActivity.this,LoginActivity.class);
                                intent.putExtra("AccessToken",(Serializable) result);
                                startActivity(intent);
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