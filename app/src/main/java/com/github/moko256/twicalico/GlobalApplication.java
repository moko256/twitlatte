/*
 * Copyright 2017 The twicalico authors
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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.github.moko256.mastodon.MastodonTwitterImpl;
import com.github.moko256.twicalico.cacheMap.StatusCacheMap;
import com.github.moko256.twicalico.cacheMap.UserCacheMap;
import com.github.moko256.twicalico.config.AppConfiguration;
import com.github.moko256.twicalico.database.TokenSQLiteOpenHelper;
import com.github.moko256.twicalico.notification.ExceptionNotification;

import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import twitter4j.AlternativeHttpClientImpl;
import twitter4j.HttpClient;
import twitter4j.HttpClientFactory;
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

    public static Twitter twitter;
    static long userId;

    public static AppConfiguration configuration;

    public static UserCacheMap userCache;
    public static StatusCacheMap statusCache;

    public static int statusLimit;
    public static int statusCacheListLimit = 1000;

    @Override
    public void onCreate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "crash_log",
                    getString(R.string.crash_log),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.crash_log_channel_description));
            channel.setLightColor(Color.RED);
            channel.enableLights(true);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        final Thread.UncaughtExceptionHandler defaultUnCaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                new ExceptionNotification().create(e, getApplicationContext());
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

        configuration.setPatternTweetMuteShowOnlyImageEnabled(defaultSharedPreferences.getBoolean("patternTweetMuteShowOnlyImageEnabled",false));
        if(configuration.isPatternTweetMuteShowOnlyImageEnabled()){
            configuration.setTweetMuteShowOnlyImagePattern(Pattern.compile(defaultSharedPreferences.getString("tweetMuteShowOnlyImagePattern","")));
        }

        configuration.setPatternUserScreenNameMuteEnabled(defaultSharedPreferences.getBoolean("patternUserScreenNameMuteEnabled",false));
        if(configuration.isPatternUserScreenNameMuteEnabled()){
            configuration.setUserScreenNameMutePattern(Pattern.compile(defaultSharedPreferences.getString("userScreenNameMutePattern","")));
        }

        configuration.setPatternUserNameMuteEnabled(defaultSharedPreferences.getBoolean("patternUserNameMuteEnabled",false));
        if(configuration.isPatternUserNameMuteEnabled()){
            configuration.setUserNameMutePattern(Pattern.compile(defaultSharedPreferences.getString("userNameMutePattern","")));
        }

        configuration.setPatternTweetSourceMuteEnabled(defaultSharedPreferences.getBoolean("patternTweetSourceMuteEnabled",false));
        if(configuration.isPatternTweetSourceMuteEnabled()){
            configuration.setTweetSourceMutePattern(Pattern.compile(defaultSharedPreferences.getString("tweetSourceMutePattern","")));
        }

        configuration.setTimelineImageLoad(Boolean.valueOf(defaultSharedPreferences.getString("isTimelineImageLoad","true")));

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

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

    public void initTwitter(@NonNull AccessToken accessToken){
        userId = accessToken.getUserId();
        twitter = getTwitterInstance(accessToken);
        userCache = new UserCacheMap(this, userId);
        statusCache = new StatusCacheMap(this, userId);
        statusLimit = twitter instanceof MastodonTwitterImpl? 40: 200;
    }

    @NonNull
    public Twitter getTwitterInstance(@NonNull AccessToken accessToken){
        Twitter t;

        if (accessToken.getTokenSecret().matches(".*\\..*")){
            t = new MastodonTwitterImpl(accessToken);
        } else {
            Configuration conf=new ConfigurationBuilder()
                    .setTweetModeExtended(true)
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(accessToken.getToken())
                    .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                    .build();

            t = new TwitterFactory(conf).getInstance();
        }

        return t;
    }

    @NonNull
    public static OkHttpClient getOkHttpClient(){
            OkHttpClient client = null;

            if (twitter instanceof MastodonTwitterImpl){
                client = ((MastodonTwitterImpl) twitter).okHttpClient;
            } else {
                HttpClient httpClient = HttpClientFactory.getInstance(twitter.getConfiguration().getHttpClientConfiguration());
                if (httpClient instanceof AlternativeHttpClientImpl) {
                    client = ((AlternativeHttpClientImpl) httpClient).getOkHttpClient();
                }
            }

            if (client == null){
                throw new IllegalStateException("Couldn't get OkHttpClient.");
            }

            return client;
    }
}