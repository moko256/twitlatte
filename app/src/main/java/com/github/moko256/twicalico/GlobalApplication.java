package com.github.moko256.twicalico;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.NotificationCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */
public class GlobalApplication extends Application {

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
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setWhen(new Date().getTime())
                                .setShowWhen(true))
                        .setBigContentTitle("Error : "+e.toString())
                        .setSummaryText("Error occurred.");
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

        super.onCreate();

        SharedPreferences defaultSharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        switch(defaultSharedPreferences.getString("nightModeType","mode_night_no_value")){
            case "mode_night_no":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);break;
            case "mode_night_auto":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);break;
            case "mode_night_follow_system":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);break;
            case "mode_night_yes":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);break;
            default:AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        int nowAccountPoint=Integer.parseInt(defaultSharedPreferences.getString("AccountPoint","-1"),10);

        if (nowAccountPoint==-1)return;

        TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this);
        AccessToken accessToken=tokenOpenHelper.getAccessToken(nowAccountPoint);
        tokenOpenHelper.close();

        Static.twitter = new TwitterFactory().getInstance();
        Static.twitter.setOAuthConsumer(Static.consumerKey, Static.consumerSecret);
        Static.twitter.setOAuthAccessToken(accessToken);

    }
}