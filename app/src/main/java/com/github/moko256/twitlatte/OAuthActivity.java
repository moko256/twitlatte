/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.Type;
import com.github.moko256.twitlatte.model.base.OAuthModel;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.disposables.CancellableDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by moko256 on 2016/04/29.
 *
 * @author moko256
 */
public class OAuthActivity extends AppCompatActivity {

    private static final String STATE_CLIENT_TYPE = "state_client_type";
    private static final String STATE_REQUIRE_PIN = "state_require_pin";
    private static final String STATE_URL_ENTER_DIALOG_SHOWN = "state_url_enter_dialog_shown";
    private static final String STATE_LAST_URL = "state_last_url";
    private static final String STATE_ENTERING_PIN = "state_entering_pin";

    @Type.ClientTypeInt
    private int authClientType = -1;

    private OAuthModel model;

    private boolean requirePin=false;

    private AlertDialog pinDialog;

    private CompositeDisposable compositeDisposable;

    private boolean isUrlEnterDialogShown = false;

    @NonNull
    private String lastUrl = "";

    @NonNull
    private String enteringPin = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        compositeDisposable = new CompositeDisposable();

        if (savedInstanceState != null) {
            lastUrl = savedInstanceState.getString(STATE_LAST_URL, "");

            enteringPin = savedInstanceState.getString(STATE_ENTERING_PIN, "");

            requirePin = savedInstanceState.getBoolean(STATE_REQUIRE_PIN, false);

            int type = savedInstanceState.getInt(STATE_CLIENT_TYPE, -1);

            if (type != -1) {
                initType(type);
                model.restoreInstanceState(savedInstanceState);
                if (requirePin){
                    showPinDialog();
                }
            } else {
                isUrlEnterDialogShown = savedInstanceState.getBoolean(STATE_URL_ENTER_DIALOG_SHOWN, false);
                if (isUrlEnterDialogShown) {
                    onStartMastodonAuthClick(null);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (requirePin) {
            outState.putBoolean(STATE_REQUIRE_PIN, true);
        }
        if (model != null && model.isRestartable()) {
            outState.putInt(STATE_CLIENT_TYPE, authClientType);
            model.saveInstanceState(outState);
        }
        if (isUrlEnterDialogShown) {
            outState.putBoolean(STATE_URL_ENTER_DIALOG_SHOWN, true);
        }
        if (!enteringPin.isEmpty()) {
            outState.putString(STATE_ENTERING_PIN, enteringPin);
        }
        if (!lastUrl.isEmpty()) {
            outState.putString(STATE_LAST_URL, lastUrl);
        }
    }

    @Override
    protected void onDestroy() {
        closePinDialog();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (!requirePin
                && uri != null
                && uri.getScheme().equals(getString(R.string.app_name))
                && uri.getHost().equals("OAuthActivity")
        ) {

            String string = uri.getQueryParameter("oauth_verifier");
            if (string != null) {
                initToken(string);
                return;
            }

            string = uri.getQueryParameter("code");
            if (string != null) {
                initToken(string);
                return;
            }

            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            initType(-1);
        }
    }

    private void initType(@Type.ClientTypeInt int authClientType){
        switch (authClientType) {
            case Type.TWITTER:
                model = new com.github.moko256.twitlatte.model.impl.twitter.OAuthModelImpl();
                break;
            case Type.MASTODON:
                model = new com.github.moko256.twitlatte.model.impl.mastodon.OAuthModelImpl();
                break;
            default:
                model = null;
                authClientType = -1;
                break;
        }
        this.authClientType = authClientType;
    }

    private void initToken(String verifier){
        compositeDisposable.add(
                model.initToken(verifier)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::storeAccessToken,
                                this::onError
                        )
        );
    }

    private void storeAccessToken(AccessToken accessToken){
        GlobalApplication.accountsModel.add(accessToken);

        GlobalApplication.preferenceRepository.putString(GlobalApplication.KEY_ACCOUNT_KEY, accessToken.getKeyString());

        ((GlobalApplication) getApplication()).initTwitter(accessToken);

        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
        initType(-1);
    }

    public void onStartTwitterAuthClick(View view) {
        initType(Type.TWITTER);

        startAuthAndOpenDialogIfNeeded(
                "twitter.com",
                BuildConfig.CONSUMER_KEY,
                BuildConfig.CONSUMER_SECRET
        );
    }

    public void onStartMastodonAuthClick(View view) {
        initType(Type.MASTODON);

        isUrlEnterDialogShown = true;

        EditText editText=new EditText(this);
        editText.setHint("e.g. mastodon.social");
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);

        AlertDialog domainConfirm = new AlertDialog.Builder(this)
                .setTitle(R.string.instance_domain)
                .setView(editText)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> {
                            isUrlEnterDialogShown = false;
                            startAuthAndOpenDialogIfNeeded(
                                    lastUrl,
                                    "",
                                    ""
                            );
                        }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastUrl = s.toString();
                domainConfirm.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editText.setText(lastUrl);
        editText.setSelection(lastUrl.length());

        compositeDisposable.add(new CancellableDisposable(domainConfirm::dismiss));
    }

    private void startAuthAndOpenDialogIfNeeded(@NonNull String url, @NonNull String consumerKey, @NonNull String consumerSecret) {
        String callbackUrl;
        if (requirePin) {
            showPinDialog();
            callbackUrl = null;
        } else {
            callbackUrl = getString(R.string.app_name) + "://OAuthActivity";
        }

        compositeDisposable.add(model
                .getAuthUrl(
                        url,
                        consumerKey,
                        consumerSecret,
                        callbackUrl
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::startBrowser,
                        throwable -> {
                            closePinDialog();
                            onError(throwable);
                        }
                )
        );
    }

    private void showPinDialog(){
        EditText editText=new EditText(this);
        editText.setHint("Code");
        editText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        pinDialog = new AlertDialog.Builder(this)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,(dialog, which) -> initToken(enteringPin))
                .setCancelable(false)
                .show();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteringPin = s.toString();
                pinDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        editText.setText(enteringPin);
    }

    private void closePinDialog(){
        if (pinDialog != null){
            pinDialog.dismiss();
        }
    }

    private void startBrowser(String url){
        Uri uri = Uri.parse(url);
        try {
            if (GlobalApplication.preferenceRepository.getBoolean(GlobalApplication.KEY_USE_CHROME_CUSTOM_TAB, true)) {
                new CustomTabsIntent.Builder()
                        .setShowTitle(false)
                        .setToolbarColor(ContextCompat.getColor(this, R.color.color_primary))
                        .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.color_primary_dark))
                        .build()
                        .launchUrl(OAuthActivity.this, uri);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.no_app_can_open, Toast.LENGTH_SHORT).show();
        }
    }

    private void onError(Throwable e){
        initType(-1);

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
        if (requirePin) {
            menu.findItem(R.id.action_use_auth_code).setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_use_auth_code:
                requirePin = !item.isChecked();
                item.setChecked(requirePin);
                break;

            default:
                return false;
        }
        return true;
    }
}