package com.github.moko256.twicalico;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.NotificationCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
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
    static User user;

    static AppConfiguration configuration;

    static UserCacheMap userCache=new UserCacheMap();
    static StatusCacheMap statusCache=new StatusCacheMap();

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
                                .addAction(
                                        android.R.drawable.ic_menu_share,
                                        "SHARE",
                                        PendingIntent.getActivity(
                                                this,
                                                401,
                                                new Intent(Intent.ACTION_SEND)
                                                        .setType("text/plain")
                                                        .putExtra(Intent.EXTRA_TEXT,stringWriter.toString()),
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                ))
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

        int timelineImageLoadType;
        switch(defaultSharedPreferences.getString("timelineImageLoadType","mode_normal")){
            case "mode_none":
                timelineImageLoadType=AppConfiguration.IMAGE_LOAD_MODE_NONE;
                break;
            case "mode_low":
                timelineImageLoadType=AppConfiguration.IMAGE_LOAD_MODE_LOW;
                break;
            case "mode_normal":
                timelineImageLoadType=AppConfiguration.IMAGE_LOAD_MODE_NORMAL;
                break;
            case "mode_full":
                timelineImageLoadType=AppConfiguration.IMAGE_LOAD_MODE_FULL;
                break;
            default:
                timelineImageLoadType=AppConfiguration.IMAGE_LOAD_MODE_NORMAL;
        }
        configuration.setTimelineImageLoadMode(timelineImageLoadType);

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

        int nowAccountPoint=Integer.parseInt(defaultSharedPreferences.getString("AccountPoint","-1"),10);

        if (nowAccountPoint==-1)return;

        TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this);
        AccessToken accessToken=tokenOpenHelper.getAccessToken(nowAccountPoint);
        tokenOpenHelper.close();

        if (accessToken==null)return;

        Configuration conf=new ConfigurationBuilder()
                .setTweetModeExtended(true)
                .build();

        twitter = new TwitterFactory(conf).getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(accessToken);

        super.onCreate();
    }
}