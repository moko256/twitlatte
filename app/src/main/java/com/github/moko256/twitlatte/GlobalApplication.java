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

import android.app.Application;
import android.os.Build;
import android.preference.PreferenceManager;

import com.github.moko256.mastodon.MastodonTwitterImpl;
import com.github.moko256.twitlatte.cacheMap.PostCache;
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap;
import com.github.moko256.twitlatte.cacheMap.UserCacheMap;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.ClientType;
import com.github.moko256.twitlatte.model.AccountsModel;
import com.github.moko256.twitlatte.net.SSLSocketFactoryCompat;
import com.github.moko256.twitlatte.queue.StatusActionQueue;
import com.github.moko256.twitlatte.repository.PreferenceRepository;

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.collection.LruCache;
import okhttp3.OkHttpClient;
import twitter4j.AlternativeHttpClientImpl;
import twitter4j.AlternativeTwitterFactoryKt;
import twitter4j.HttpClientFactory;
import twitter4j.Twitter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_NIGHT_MODE;

/**
 * Created by moko256 on 2016/04/30.
 *
 * @author moko256
 */
public class GlobalApplication extends Application {

    public static int statusLimit;
    public final static int statusCacheListLimit = 1000;

    private final static LruCache<Configuration, Twitter> twitterCache = new LruCache<>(4);
    public static Twitter twitter;
    public static AccessToken accessToken;

    @ClientType.ClientTypeInt
    public static int clientType = -1;

    static long userId;

    public static PreferenceRepository preferenceRepository;
    public static StatusActionQueue statusActionQueue = new StatusActionQueue();

    public final static UserCacheMap userCache = new UserCacheMap();
    public final static StatusCacheMap statusCache = new StatusCacheMap();
    public final static PostCache postCache = new PostCache(statusCache, userCache);

    public static AccountsModel accountsModel;

    @Override
    public void onCreate() {
        preferenceRepository = new PreferenceRepository(
                PreferenceManager.getDefaultSharedPreferences(this)
        );

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        @AppCompatDelegate.NightMode
        int mode=AppCompatDelegate.MODE_NIGHT_NO;

        switch(preferenceRepository.getString(KEY_NIGHT_MODE,"mode_night_no_value")){

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

        accountsModel = new AccountsModel(this);

        String accountKey = preferenceRepository.getString(KEY_ACCOUNT_KEY,"-1");

        if (!accountKey.equals("-1")) {

            AccessToken accessToken = accountsModel.get(accountKey);

            if (accessToken != null) {
                initTwitter(accessToken);
            }
        }

        super.onCreate();
    }

    public void initTwitter(@NonNull AccessToken accessToken){
        userId = accessToken.getUserId();
        clientType = accessToken.getType();
        twitter = createTwitterInstance(accessToken);
        GlobalApplication.accessToken = accessToken;
        userCache.prepare(this, accessToken);
        statusCache.prepare(this, accessToken);
        statusLimit = clientType == ClientType.TWITTER? 200: 40;
    }

    @NonNull
    public Twitter createTwitterInstance(@NonNull AccessToken accessToken){
        Twitter t;

        Configuration conf;

        if (accessToken.getType() == ClientType.TWITTER){
            conf = new ConfigurationBuilder()
                    .setTweetModeExtended(true)
                    .setOAuthConsumerKey(BuildConfig.CONSUMER_KEY)
                    .setOAuthConsumerSecret(BuildConfig.CONSUMER_SECRET)
                    .setOAuthAccessToken(accessToken.getToken())
                    .setOAuthAccessTokenSecret(accessToken.getTokenSecret())
                    .build();

            replaceCompatibleOkHttpClient(conf);

            t = twitterCache.get(conf);

            if (t == null) {
                t = AlternativeTwitterFactoryKt.createTwitterInstance(conf);
                twitterCache.put(conf, t);
            }
        } else {
            conf = new ConfigurationBuilder()
                    .setOAuthAccessToken(accessToken.getToken())
                    .setRestBaseURL(accessToken.getUrl())
                    .build();

            replaceCompatibleOkHttpClient(conf);

            t = twitterCache.get(conf);

            if (t == null) {
                t = new MastodonTwitterImpl(
                        conf,
                        accessToken.getUserId(),
                        getOkHttpClient(conf).newBuilder()
                );
                twitterCache.put(conf, t);
            }
        }

        return t;
    }

    private static void replaceCompatibleOkHttpClient(Configuration conf){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AlternativeHttpClientImpl httpClient = getT4jHttpClient(conf);
            OkHttpClient oldClient = httpClient.getOkHttpClient();
            if (!(oldClient.sslSocketFactory() instanceof SSLSocketFactoryCompat)){
                try {
                    X509TrustManager trustManager = systemDefaultTrustManager();

                    Field field = httpClient.getClass().getDeclaredField("okHttpClient");
                    field.setAccessible(true);
                    field.set(
                            httpClient,
                            oldClient.newBuilder()
                                    .sslSocketFactory(new SSLSocketFactoryCompat(trustManager), trustManager)
                                    .build()
                    );
                } catch (NoSuchFieldException | IllegalAccessException | NoSuchAlgorithmException | KeyStoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static X509TrustManager systemDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException, IllegalStateException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    @NonNull
    public static OkHttpClient getOkHttpClient(){
        return getOkHttpClient(twitter.getConfiguration());
    }

    @NonNull
    public static OkHttpClient getOkHttpClient(Configuration configuration){
        replaceCompatibleOkHttpClient(configuration);
        return getT4jHttpClient(configuration).getOkHttpClient();
    }

    @NonNull
    private static AlternativeHttpClientImpl getT4jHttpClient(Configuration configuration){
        return (AlternativeHttpClientImpl) HttpClientFactory.getInstance(
                configuration.getHttpClientConfiguration()
        );
    }
}