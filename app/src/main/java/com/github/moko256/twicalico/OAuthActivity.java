/*
 * Copyright 2018 The twicalico authors
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.github.moko256.twicalico.database.TokenSQLiteOpenHelper;
import com.github.moko256.twicalico.model.base.OAuthModel;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2016/04/29.
 *
 * @author moko256
 */
public class OAuthActivity extends AppCompatActivity {
    private OAuthModel model;
    public boolean requirePin=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if(!requirePin && uri != null && uri.toString().startsWith(getString(R.string.app_name) + "://OAuthActivity")){
            String string = uri.getQueryParameter("oauth_verifier");
            if (string != null) {
                initToken(string);
            } else {
                initToken(uri.getQueryParameter("code"));
            }
        }
    }

    private void initToken(String verifier){
        model.initToken(verifier)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::storeAccessToken,
                        Throwable::printStackTrace
                );
    }

    private void storeAccessToken(AccessToken accessToken){
        SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this);
        long nowAccountPoint=tokenOpenHelper.addAccessToken(accessToken)-1;
        tokenOpenHelper.close();

        defaultSharedPreferences
                .edit()
                .putString("AccountPoint",String.valueOf(nowAccountPoint))
                .apply();

        ((GlobalApplication) getApplication()).initTwitter(accessToken);

        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    public void onStartTwitterCallbackAuthClick(View view) {
        model = new com.github.moko256.twicalico.model.impl.twitter.OAuthModelImpl();
        model.getCallbackAuthUrl("twitter.com", GlobalApplication.consumerKey, GlobalApplication.consumerSecret, getString(R.string.app_name))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::startBrowser, Throwable::printStackTrace);
    }

    public void onStartTwitterCodeAuthClick(View view) {
        model = new com.github.moko256.twicalico.model.impl.twitter.OAuthModelImpl();
        requirePin = true;
        model.getCodeAuthUrl("twitter.com", GlobalApplication.consumerKey, GlobalApplication.consumerSecret)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::startBrowser, Throwable::printStackTrace);
        showPinDialog();
    }

    public void onStartMastodonCallbackAuthClick(View view) {
        model = new com.github.moko256.twicalico.model.impl.mastodon.OAuthModelImpl();
        EditText editText=new EditText(this);
        editText.setHint("URL");
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> model
                                .getCallbackAuthUrl(
                                        editText.getText().toString(),
                                        "",
                                        "",
                                        getString(R.string.app_name)
                                )
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        this::startBrowser,
                                        Throwable::printStackTrace
                                )
                )
                .setCancelable(false)
                .show();
    }

    public void onStartMastodonCodeAuthClick(View view) {
        model = new com.github.moko256.twicalico.model.impl.mastodon.OAuthModelImpl();
        EditText editText=new EditText(this);
        editText.setHint("URL");
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,(dialog, which) -> {
                    model.getCodeAuthUrl(editText.getText().toString(), "", "")
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    this::startBrowser,
                                    Throwable::printStackTrace
                            );
                    showPinDialog();
                })
                .setCancelable(false)
                .show();
    }

    public void onSettingClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void showPinDialog(){
        EditText editText=new EditText(this);
        editText.setHint("PIN");
        editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,(dialog, which) -> initToken(editText.getText().toString()))
                .setCancelable(false)
                .show();
    }

    private void startBrowser(String url){
        new CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .build()
                .launchUrl(OAuthActivity.this, Uri.parse(url));
    }

}