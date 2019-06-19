/*
 * Copyright 2015-2019 The twitlatte authors
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.twitlatte.api.OAuthApiClientGeneratorKt;
import com.github.moko256.twitlatte.database.TokenSQLiteOpenHelper;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.base.OAuthModel;
import com.github.moko256.twitlatte.model.impl.OAuthModelImpl;
import com.github.moko256.twitlatte.net.OkHttpHolderKt;
import com.github.moko256.twitlatte.view.DialogContent;
import com.github.moko256.twitlatte.view.EditTextsDialogShowerKt;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

import static com.github.moko256.latte.client.base.ApiClientKt.CLIENT_TYPE_NOTHING;
import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;
import static com.github.moko256.latte.client.twitter.TwitterApiClientImplKt.CLIENT_TYPE_TWITTER;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY;

/**
 * Created by moko256 on 2016/04/29.
 *
 * @author moko256
 */
public class OAuthActivity extends AppCompatActivity {

    private static final String STATE_CLIENT_TYPE = "state_client_type";
    private static final String STATE_REQUIRE_PIN = "state_require_pin";
    private static final String STATE_REQUIRE_USE_ANOTHER_CONSUMER_TOKEN = "state_use_another_token";
    private static final String STATE_URL_ENTER_DIALOG_SHOWN = "state_url_enter_dialog_shown";
    private static final String STATE_LAST_URL = "state_last_url";
    private static final String STATE_ENTERING_PIN = "state_entering_pin";
    private static final String STATE_ENTERING_CONSUMER_KEY = "state_entering_ck";
    private static final String STATE_ENTERING_CONSUMER_SECRET = "state_entering_cs";

    private int authClientType = CLIENT_TYPE_NOTHING;

    private OAuthModel model;

    private boolean requirePin=false;
    private boolean useAnotherConsumerToken =false;

    private Disposable pinDialog;

    private CompositeDisposable compositeDisposable;

    private boolean isUrlEnterDialogShown = false;

    @NonNull
    private String lastUrl = "";

    @NonNull
    private String enteringPin = "";

    @NonNull
    private String enteringConsumerKey = "";

    @NonNull
    private String enteringConsumerSecret = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        compositeDisposable = new CompositeDisposable();

        if (savedInstanceState != null) {
            lastUrl = savedInstanceState.getString(STATE_LAST_URL, "");

            enteringPin = savedInstanceState.getString(STATE_ENTERING_PIN, "");

            enteringConsumerKey = savedInstanceState.getString(STATE_ENTERING_CONSUMER_KEY, "");
            enteringConsumerSecret = savedInstanceState.getString(STATE_ENTERING_CONSUMER_SECRET, "");

            requirePin = savedInstanceState.getBoolean(STATE_REQUIRE_PIN, false);
            useAnotherConsumerToken = savedInstanceState.getBoolean(STATE_REQUIRE_USE_ANOTHER_CONSUMER_TOKEN, false);

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
        if (useAnotherConsumerToken) {
            outState.putBoolean(STATE_REQUIRE_USE_ANOTHER_CONSUMER_TOKEN, true);
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
        if (!enteringConsumerKey.isEmpty()) {
            outState.putString(STATE_ENTERING_CONSUMER_KEY, enteringConsumerKey);
        }
        if (!enteringConsumerSecret.isEmpty()) {
            outState.putString(STATE_ENTERING_CONSUMER_SECRET, enteringConsumerSecret);
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
        if (model != null
                && !requirePin
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
            initType(CLIENT_TYPE_NOTHING);
        }
    }

    private void initType(int authClientType){
        switch (authClientType) {
            case CLIENT_TYPE_TWITTER:
                model = new OAuthModelImpl(
                        OAuthApiClientGeneratorKt.generateTwitterOAuthApiClient()
                );
                break;
            case CLIENT_TYPE_MASTODON:
                model = new OAuthModelImpl(
                        OAuthApiClientGeneratorKt.generateMastodonOAuthApiClient(
                                OkHttpHolderKt.getAppOkHttpClientInstance()
                        )
                );
                break;
            default:
                model = null;
                authClientType = CLIENT_TYPE_NOTHING;
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
        GlobalApplicationKt.getAccountsModel(this).add(accessToken);

        GlobalApplicationKt.preferenceRepository.putString(KEY_ACCOUNT_KEY, accessToken.getKeyString());

        ((GlobalApplication) getApplication()).initCurrentClient(accessToken);

        setResult(RESULT_OK);
        finish();
        initType(CLIENT_TYPE_NOTHING);
    }

    public void onStartTwitterAuthClick(View view) {
        initType(CLIENT_TYPE_TWITTER);

        startAuthAndOpenDialogIfNeeded(TokenSQLiteOpenHelper.TWITTER_URL);
    }

    public void onStartMastodonAuthClick(View view) {
        initType(CLIENT_TYPE_MASTODON);

        isUrlEnterDialogShown = true;

        compositeDisposable.add(
                EditTextsDialogShowerKt.createEditTextsDialog(
                        this,
                        getString(R.string.instance_domain),
                        true,
                        new DialogContent(
                                "e.g. mastodon.social",
                                lastUrl,
                                EditorInfo.TYPE_TEXT_VARIATION_URI,
                                url -> {
                                    lastUrl = url;
                                    return Unit.INSTANCE;
                                }
                        )
                ).subscribe(() -> {
                    isUrlEnterDialogShown = false;
                    startAuthAndOpenDialogIfNeeded(lastUrl);
                })
        );
    }

    private void startAuthAndOpenDialogIfNeeded(@NonNull String url) {
        String callbackUrl;
        if (requirePin) {
            showPinDialog();
            callbackUrl = null;
        } else {
            callbackUrl = getString(R.string.app_name) + "://OAuthActivity";
        }

        if (useAnotherConsumerToken && authClientType == CLIENT_TYPE_TWITTER) {
            compositeDisposable.add(
                    EditTextsDialogShowerKt.createEditTextsDialog(
                            this,
                            "Consumer key/secret",
                            true,
                            new DialogContent(
                                   "consumer key",
                                   enteringConsumerKey,
                                    EditorInfo.TYPE_CLASS_TEXT,
                                    ck -> {
                                       enteringConsumerKey = ck;
                                       return Unit.INSTANCE;
                                    }
                            ),
                            new DialogContent(
                                    "consumer secret",
                                    enteringConsumerSecret,
                                    EditorInfo.TYPE_CLASS_TEXT,
                                    cs -> {
                                        enteringConsumerSecret = cs;
                                        return Unit.INSTANCE;
                                    }
                            )
                    ).subscribe(() -> startAuth(url, callbackUrl, enteringConsumerKey, enteringConsumerSecret))
            );
        } else {
            startAuth(url, callbackUrl, null, null);
        }
    }

    private void startAuth(
            String url,
            String callbackUrl,
            String consumerKey,
            String consumerSecret
    ) {
        compositeDisposable.add(
                model
                        .getAuthUrl(consumerKey, consumerSecret, url, callbackUrl)
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
        pinDialog = EditTextsDialogShowerKt.createEditTextsDialog(
                this,
                null,
                false,
                new DialogContent(
                        "Code",
                        enteringPin,
                        EditorInfo.TYPE_NUMBER_FLAG_DECIMAL,
                        pin -> {
                            enteringPin = pin;
                            return Unit.INSTANCE;
                        }
                )
        ).subscribe(() -> initToken(enteringPin));
    }

    private void closePinDialog(){
        if (pinDialog != null){
            pinDialog.dispose();
        }
    }

    private void startBrowser(String url){
        Uri uri = Uri.parse(url);
        AppCustomTabsKt.launchChromeCustomTabs(
                this,
                uri,
                false,
                () -> new CustomTabsIntent.Builder()
                        .setShowTitle(false)
                        .setToolbarColor(
                                ContextCompat.getColor(this, R.color.color_primary)
                        )
                        .setSecondaryToolbarColor(
                                ContextCompat.getColor(this, R.color.color_primary_dark)
                        )
        );
    }

    private void onError(Throwable e){
        initType(CLIENT_TYPE_NOTHING);

        e.printStackTrace();
        Toast.makeText(
                this,
                getString(R.string.error_occurred) + "\n\n" + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_oauth_toolbar,menu);
        if (requirePin) {
            menu.findItem(R.id.action_use_auth_code).setChecked(true);
        }
        if (useAnotherConsumerToken) {
            menu.findItem(R.id.action_use_another_consumer_token).setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_use_another_consumer_token:
                useAnotherConsumerToken = !item.isChecked();
                item.setChecked(useAnotherConsumerToken);
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