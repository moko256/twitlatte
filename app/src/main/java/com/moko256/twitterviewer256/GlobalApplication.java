package com.moko256.twitterviewer256;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import java.util.Objects;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on GitHub on 2016/04/30.
 */
public class GlobalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        switch(PreferenceManager.getDefaultSharedPreferences(this).getString("nightModeType","mode_night_no_value")){
            case "mode_night_no":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);break;
            case "mode_night_auto":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);break;
            case "mode_night_follow_system":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);break;
            case "mode_night_yes":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);break;
            default:AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //if (Static.twitter == null) {

            String nowUserNumStr=PreferenceManager.getDefaultSharedPreferences(this).getString("nowUserDataFileInt","-1");

            if(!Objects.equals(nowUserNumStr, "-1")){
                Static.nowUserDataFile="user_token_file_"+nowUserNumStr;

                SharedPreferences sp = getSharedPreferences(Static.nowUserDataFile,MODE_PRIVATE);
                Static.token = sp.getString("token", null);
                Static.tokenSecret = sp.getString("token_secret", null);

                if ((Static.token != null) && (Static.tokenSecret != null)) {

                    Static.twitter = new TwitterFactory().getInstance();

                    AccessToken at = new AccessToken(Static.token, Static.tokenSecret);

                    Static.twitter.setOAuthConsumer(Static.consumerKey, Static.consumerSecret);

                    Static.twitter.setOAuthAccessToken(at);

                }
                else{
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .edit()
                            .putString("nowUserDataFileInt","0")
                            .apply();
                }
            }
            else{
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("nowUserDataFileInt","0")
                        .apply();
            }
        //}
    }
}
