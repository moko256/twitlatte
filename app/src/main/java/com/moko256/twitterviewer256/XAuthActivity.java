package com.moko256.twitterviewer256;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;

import java.io.Serializable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

public class XAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xauth);

        findViewById(R.id.xauth_oauth_sign_in_button)
                .setOnClickListener(button-> startActivity(new Intent(this,OAuthActivity.class)));

        findViewById(R.id.xauth_sign_in_button)
                .setOnClickListener(button->{
            button.setEnabled(false);
            Configuration conf = ConfigurationContext.getInstance();
            OAuthAuthorization oauth =new OAuthAuthorization(conf);
            oauth.setOAuthConsumer(Static.consumerKey,Static.consumerSecret);

            Observable
                    .create(subscriber -> {
                        try {
                            subscriber.onNext(
                                    oauth.getOAuthAccessToken(
                                            ((AppCompatEditText)findViewById(R.id.xauth_user_name_input)).getText().toString(),
                                            ((AppCompatEditText)findViewById(R.id.xauth_user_password_input)).getText().toString()
                                    )
                            );
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result->{
                                Intent intent=new Intent(XAuthActivity.this,LoginActivity.class);
                                intent.putExtra("AccessToken",(Serializable) result);
                                startActivity(intent);
                            },
                            Throwable::printStackTrace,
                            ()->{}
                    );
        });
    }
}
