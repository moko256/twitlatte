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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.settings, s);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments()==null||getArguments().getString(ARG_PREFERENCE_ROOT)==null){
            SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

            TokenSQLiteOpenHelper helper=new TokenSQLiteOpenHelper(getContext());
            SQLiteDatabase database=helper.getReadableDatabase();
            Cursor c=database.query("AccountTokenList",new String[]{"userName","userId","token","tokenSecret"},null,null,null,null,null);

            CharSequence[] entries=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];
            CharSequence[] entryValues=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];

            while (c.moveToNext()){
                entries[c.getPosition()]=c.getString(0);
                entryValues[c.getPosition()]=String.valueOf(c.getPosition());
            }

            entries[entries.length-1]=getString(R.string.add_account);
            entryValues[entryValues.length-1]="-1";

            c.close();
            database.close();

            ListPreference nowAccountList=(ListPreference) findPreference("AccountPoint");
            nowAccountList.setEntries(entries);
            nowAccountList.setEntryValues(entryValues);
            nowAccountList.setDefaultValue(defaultSharedPreferences.getString("AccountPoint","-1"));
            nowAccountList.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        if (newValue.equals("-1")){
                            GlobalApplication.twitter = null;
                            startActivity(
                                    new Intent(getContext(),OAuthActivity.class)
                            );
                        } else {
                            TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this.getContext());
                            AccessToken accessToken=tokenOpenHelper.getAccessToken(Integer.valueOf((String) newValue));
                            tokenOpenHelper.close();

                            Configuration conf=new ConfigurationBuilder()
                                    .setTweetModeExtended(true)
                                    .setOAuthConsumerKey(GlobalApplication.consumerKey)
                                    .setOAuthConsumerSecret(GlobalApplication.consumerSecret)
                                    .setOAuthAccessToken(accessToken.getToken())
                                    .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                                    .build();

                            GlobalApplication.twitter = new TwitterFactory(conf).getInstance();
                            GlobalApplication.userId = accessToken.getUserId();
                            startActivity(
                                    new Intent(getContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            );
                        }
                        return true;
                    }
            );

            findPreference("logout").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.logout)
                        .setMessage("Logout?")
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, i) -> {
                                    long p = helper.deleteAccessToken(
                                            helper.getAccessToken(
                                                    Integer.valueOf(
                                                            defaultSharedPreferences.getString("AccountPoint","-1")
                                                    ))
                                                    .getUserId()
                                    )-1;
                                    defaultSharedPreferences
                                            .edit()
                                            .putString("AccountPoint", String.valueOf(p)).apply();

                                    TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this.getContext());
                                    AccessToken accessToken=tokenOpenHelper.getAccessToken(Long.valueOf(p).intValue());
                                    tokenOpenHelper.close();

                                    Configuration conf=new ConfigurationBuilder()
                                            .setTweetModeExtended(true)
                                            .setOAuthConsumerKey(GlobalApplication.consumerKey)
                                            .setOAuthConsumerSecret(GlobalApplication.consumerSecret)
                                            .setOAuthAccessToken(accessToken.getToken())
                                            .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                                            .build();

                                    GlobalApplication.twitter = new TwitterFactory(conf).getInstance();
                                    GlobalApplication.userId = accessToken.getUserId();
                                    startActivity(
                                            new Intent(getContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    );
                                }
                        )
                        .setNegativeButton(android.R.string.cancel,(dialog, i) -> dialog.cancel())
                        .show();
                return false;
            });

            ListPreference nowThemeMode=(ListPreference) findPreference("nightModeType");
            nowThemeMode.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        switch(String.valueOf(newValue)){
                            case "mode_night_no":
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);break;
                            case "mode_night_auto":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);break;
                            case "mode_night_follow_system":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);break;
                            case "mode_night_yes":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);break;
                            default:AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        return true;
                    }
            );

            Preference licenseThisApp=findPreference("license_at_this_app");
            licenseThisApp.setOnPreferenceClickListener(preference -> {
                getContext().startActivity(
                        new Intent(getContext(), LicensesActivity.class)
                                .putExtra("title", getResources().getText(R.string.app_name))
                                .putExtra("library_name", "twicalico")
                );
                return true;
            });

            Preference version=findPreference("app_version");
            version.setTitle("Version:"+ BuildConfig.VERSION_NAME);
            version.setOnPreferenceClickListener(preference -> {
                Date birthday=new Date(1446956982000L);
                Toast.makeText(
                        getContext(),
                        "This application was born on "+
                                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(birthday),
                        Toast.LENGTH_SHORT
                ).show();
                Toast.makeText(getContext(), "This application is "+String.valueOf((int)Math.floor((new Date().getTime()-birthday.getTime())/(31557600000L)))+"years old.", Toast.LENGTH_SHORT).show();
                return false;
            });
        } else if(getArguments().getString(ARG_PREFERENCE_ROOT).equals("license")){
            final String license_keys[]=new String[]{
                    "support_v4",
                    "support_v7",
                    "support_v14",
                    "support_design",
                    "exo_player",
                    "glide",
                    "okhttp",
                    "twitter4j",
                    "twitter_text",
                    "rx_java",
                    "rx_android"
            };

            for (String name : license_keys) {
                findPreference("license_lib_" + name).setOnPreferenceClickListener(preference -> {
                    getContext().startActivity(
                            new Intent(getContext(), LicensesActivity.class)
                                    .putExtra("title", preference.getTitle())
                                    .putExtra("library_name", name)
                    );
                    return true;
                });
            }
        }

    }

}
