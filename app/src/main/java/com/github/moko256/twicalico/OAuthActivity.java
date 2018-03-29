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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.github.moko256.twicalico.entity.AccessToken;
import com.github.moko256.twicalico.model.base.OAuthModel;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by moko256 on 2016/04/29.
 *
 * @author moko256
 */
public class OAuthActivity extends AppCompatActivity {
    private OAuthModel model;
    public boolean requirePin=false;

    private AlertDialog pinDialog;
    public CheckBox useAuthCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        useAuthCode = findViewById(R.id.use_auth_code);
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::storeAccessToken,
                        this::onError
                );
    }

    private void storeAccessToken(AccessToken accessToken){
        SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        GlobalApplication.accountsModel.add(accessToken);

        defaultSharedPreferences
                .edit()
                .putString("AccountKey", accessToken.getKeyString())
                .apply();

        ((GlobalApplication) getApplication()).initTwitter(accessToken);

        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    public void onStartTwitterAuthClick(View view) {
        requirePin = useAuthCode.isChecked();
        model = new com.github.moko256.twicalico.model.impl.twitter.OAuthModelImpl();
        Single<String> authSingle;
        if (requirePin) {
            showPinDialog();
            authSingle = model
                    .getCodeAuthUrl(
                            "twitter.com",
                            BuildConfig.CONSUMER_KEY,
                            BuildConfig.CONSUMER_SECRET
                    );
        } else {
            authSingle = model
                    .getCallbackAuthUrl(
                            "twitter.com",
                            BuildConfig.CONSUMER_KEY,
                            BuildConfig.CONSUMER_SECRET,
                            getString(R.string.app_name)
                    );
        }
        authSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::startBrowser,
                        throwable -> {
                            closePinDialog();
                            onError(throwable);
                        }
                        );
    }

    public void onStartMastodonAuthClick(View view) {
        requirePin = useAuthCode.isChecked();
        model = new com.github.moko256.twicalico.model.impl.mastodon.OAuthModelImpl();
        EditText editText=new EditText(this);
        editText.setHint("URL");
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> {
                            Single<String> authSingle;
                            if (requirePin) {
                                showPinDialog();
                                authSingle = model
                                        .getCodeAuthUrl(
                                                editText.getText().toString(),
                                                "",
                                                ""
                                        );
                            } else {
                                authSingle = model
                                        .getCallbackAuthUrl(
                                                editText.getText().toString(),
                                                "",
                                                "",
                                                getString(R.string.app_name)
                                        );
                            }
                            authSingle
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            this::startBrowser,
                                            throwable -> {
                                                closePinDialog();
                                                onError(throwable);
                                            }
                                    );
                        }
                )
                .setCancelable(false)
                .show();
    }

    private void showPinDialog(){
        EditText editText=new EditText(this);
        editText.setHint("Code");
        editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        pinDialog = new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,(dialog, which) -> initToken(editText.getText().toString()))
                .setCancelable(false)
                .show();
    }

    private void closePinDialog(){
        if (pinDialog != null){
            pinDialog.cancel();
        }
    }

    private void startBrowser(String url){
        new CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setToolbarColor(ContextCompat.getColor(this, R.color.color_primary))
                .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.color_primary_dark))
                .build()
                .launchUrl(OAuthActivity.this, Uri.parse(url));
    }

    private void onError(Throwable e){
        e.printStackTrace();
        Toast.makeText(
                this,
                getString(R.string.error_occurred) + "\n\n" + TwitterStringUtils.convertErrorToText(e),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_oauth_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return true;
    }
}