package com.moko256.twitterviewer256;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * Created by moko256 on GitHub on 2016/02/01.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        Static.token=sp.getString("tt", null);
        Static.tokenSecret=sp.getString("tts", null);

        if ((Static.token!=null)&&(Static.tokenSecret!=null)){

            Static.twitter = new TwitterFactory().getInstance();

            AccessToken at = new AccessToken(Static.token, Static.tokenSecret);

            Static.twitter.setOAuthConsumer(Static.consumerKey, Static.consumerSecret);

            Static.twitter.setOAuthAccessToken(at);

            final Uri uri = getIntent().getData();

            Intent intent=null;

            if (uri!=null){
                switch(uri.getScheme()){
                    case "twitter":
                    switch(uri.getHost()){
                        case "timeline":
                            intent=new Intent(SplashActivity.this , MainActivity.class);
                            break;
                        case "status":
                            intent=new Intent(SplashActivity.this , ShowTweetActivity.class);
                            intent.putExtra("statusId",Long.parseLong(uri.getQueryParameter("status_id")));
                            break;
                        case "user":
                            intent=new Intent(SplashActivity.this , ShowUserActivity.class);
                            intent.putExtra("userName",uri.getQueryParameter("screen_name"));
                            break;
                        default:
                            intent=new Intent();
                            intent.setData(uri);
                            intent.setPackage("com.twitter.android");
                    }
                        break;

                    case "https":
                        List<String> path=uri.getPathSegments();
                        switch(path.size()){
                            case 1:
                                intent = new Intent(SplashActivity.this, ShowUserActivity.class);
                                intent.putExtra("userName", path.get(0));
                                break;
                            case 2:
                                switch (path.get(1)) {
                                    case "status":
                                        intent = new Intent(SplashActivity.this, ShowTweetActivity.class);
                                        intent.putExtra("statusId", Long.parseLong(uri.getPathSegments().get(2)));
                                        break;
                                }
                                break;
                        }
                        break;
                }
            }else{
                intent=new Intent(SplashActivity.this , MainActivity.class);
            }

            startActivity(intent);

        }else{

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
}
