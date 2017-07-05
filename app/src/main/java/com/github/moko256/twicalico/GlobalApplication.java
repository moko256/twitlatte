/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.NotificationCompat;

import com.github.moko256.twicalico.cacheMap.StatusCacheMap;
import com.github.moko256.twicalico.cacheMap.UserCacheMap;
import com.github.moko256.twicalico.database.TokenSQLiteOpenHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */
public class GlobalApplication extends Application {

    static final String consumerKey=BuildConfig.CONSUMER_KEY;
    static final String consumerSecret=BuildConfig.CONSUMER_SECRET;

    static Twitter twitter;
    static long userId;

    static AppConfiguration configuration;

    public static UserCacheMap userCache;
    public static StatusCacheMap statusCache;

    @Override
    public void onCreate() {
        final Thread.UncaughtExceptionHandler defaultUnCaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                StringWriter stringWriter=new StringWriter();
                PrintWriter printWriter=new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                printWriter.flush();
                stringWriter.flush();
                printWriter.close();
                stringWriter.close();
                NotificationCompat.InboxStyle inboxStyle=new NotificationCompat.InboxStyle(
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(android.R.drawable.stat_notify_error)
                                .setContentTitle("Error : "+e.toString())
                                .setContentText(e.toString())
                                .setWhen(new Date().getTime())
                                .setShowWhen(true)
                                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                                .setContentIntent(PendingIntent.getActivity(
                                        this,
                                        401,
                                        new Intent(Intent.ACTION_SEND)
                                                .setType("text/plain")
                                                .putExtra(Intent.EXTRA_TEXT,stringWriter.toString()),
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )))
                        .setBigContentTitle("Error : "+e.toString())
                        .setSummaryText(getResources().getString(R.string.error_occurred));
                String[] lines=stringWriter.toString().split("\n");
                for(String s : lines){
                    inboxStyle.addLine(s);
                }
                NotificationManagerCompat.from(getApplicationContext()).notify(NotificationManagerCompat.IMPORTANCE_HIGH, inboxStyle.build());
            }catch (Throwable fe){
                fe.printStackTrace();
            } finally {
                defaultUnCaughtExceptionHandler.uncaughtException(t,e);
            }
        });

        SharedPreferences defaultSharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        configuration=new AppConfiguration();

        configuration.setPatternTweetMuteEnabled(defaultSharedPreferences.getBoolean("patternTweetMuteEnabled",false));
        if(configuration.isPatternTweetMuteEnabled()){
            configuration.setTweetMutePattern(defaultSharedPreferences.getString("tweetMutePattern",""));
        }

        configuration.setPatternUserScreenNameMuteEnabled(defaultSharedPreferences.getBoolean("patternUserScreenNameMuteEnabled",false));
        if(configuration.isPatternUserScreenNameMuteEnabled()){
            configuration.setUserScreenNameMutePattern(defaultSharedPreferences.getString("userScreenNameMutePattern",""));
        }

        configuration.setPatternUserNameMuteEnabled(defaultSharedPreferences.getBoolean("patternUserNameMuteEnabled",false));
        if(configuration.isPatternUserNameMuteEnabled()){
            configuration.setUserNameMutePattern(defaultSharedPreferences.getString("userNameMutePattern",""));
        }

        configuration.setPatternTweetSourceMuteEnabled(defaultSharedPreferences.getBoolean("patternTweetSourceMuteEnabled",false));
        if(configuration.isPatternTweetSourceMuteEnabled()){
            configuration.setTweetSourceMutePattern(defaultSharedPreferences.getString("tweetSourceMutePattern",""));
        }

        configuration.setIsTimelineImageLoad(Boolean.valueOf(defaultSharedPreferences.getString("isTimelineImageLoad","true")));

        @AppCompatDelegate.NightMode
        int mode=AppCompatDelegate.MODE_NIGHT_NO;

        switch(defaultSharedPreferences.getString("nightModeType","mode_night_no_value")){

            case "mode_night_no":
                mode=AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "mode_night_auto":
                mode=AppCompatDelegate.MODE_NIGHT_AUTO;
                break;
            case "mode_night_follow_system":
                mode=AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
            case "mode_night_yes":
                mode=AppCompatDelegate.MODE_NIGHT_YES;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(mode);

        int nowAccountPoint = Integer.parseInt(defaultSharedPreferences.getString("AccountPoint","-1"),10);
        if (nowAccountPoint==-1)return;

        TokenSQLiteOpenHelper tokenOpenHelper =  new TokenSQLiteOpenHelper(this);
        AccessToken accessToken=tokenOpenHelper.getAccessToken(nowAccountPoint);
        tokenOpenHelper.close();

        if (accessToken==null)return;
        initTwitter(accessToken);

        super.onCreate();
    }

    public void initTwitter(AccessToken accessToken){
        userId = accessToken.getUserId();

        Configuration conf=new ConfigurationBuilder()
                .setTweetModeExtended(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken.getToken())
                .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                .build();

        twitter = new TwitterFactory(conf).getInstance();

        userCache=new UserCacheMap(this, userId);
        statusCache=new StatusCacheMap(this, userId);
    }
}