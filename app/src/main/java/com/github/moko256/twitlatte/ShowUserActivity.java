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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.moko256.latte.client.base.ApiClient;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity extends AppCompatActivity implements BaseListFragment.GetViewForSnackBar, BaseTweetListFragment.GetRecyclerViewPool, BaseUsersFragment.GetRecyclerViewPool {

    private CompositeDisposable disposable;
    private Client client;

    private String userScreenName;
    private long userId;

    private User user;

    private ActionBar actionBar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private RecyclerView.RecycledViewPool tweetListViewPool;
    private RecyclerView.RecycledViewPool userListViewPool;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        disposable = new CompositeDisposable();
        client = GlobalApplicationKt.getClient(this);

        setSupportActionBar(findViewById(R.id.toolbar_show_user));

        actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        viewPager= findViewById(R.id.show_user_view_pager);
        viewPager.setOffscreenPageLimit(1);

        tabLayout= findViewById(R.id.tab_show_user);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {}

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = Objects.requireNonNull((FragmentPagerAdapter) viewPager.getAdapter()).getFragment(tab.getPosition());
                if (fragment instanceof MovableTopInterface){
                    ((MovableTopInterface) fragment).moveToTop();
                }
            }
        });

        tweetListViewPool = new RecyclerView.RecycledViewPool();
        userListViewPool = new RecyclerView.RecycledViewPool();

        findViewById(R.id.activity_show_user_fab).setOnClickListener(v -> {
            if (user != null){
                startActivity(PostActivity.getIntent(this, TwitterStringUtils.plusAtMark(user.getScreenName())+" "));
            }
        });


        userScreenName = getIntent().getStringExtra("userScreenName");
        userId = getIntent().getLongExtra("userId", -1);

        if (userId != -1){
            user = client.getUserCache().get(userId);
        }

        if (user != null) {
            new ShowUserFragmentsPagerAdapter(client, getSupportFragmentManager(),this, user.getId()).initAdapter(viewPager);
        } else {
            disposable.add(
                    getUserSingle()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    it -> {
                                        user = it;
                                        new ShowUserFragmentsPagerAdapter(client, getSupportFragmentManager(),this,it.getId()).initAdapter(viewPager);
                                    },
                                    e -> Snackbar.make(
                                            getViewForSnackBar(),
                                            e.getMessage(),
                                            Snackbar.LENGTH_LONG
                                    ).show()
                            )
            );
        }
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
        disposable = null;
        client = null;

        tabLayout=null;
        viewPager=null;
        actionBar=null;

        user=null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_show_user_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getShareUrl(){
        String url;
        if (client.getAccessToken().getClientType() == CLIENT_TYPE_MASTODON){
            String baseUrl;
            String userName;
            if (user.getScreenName().matches(".*@.*")){
                String[] split = user.getScreenName().split("@");
                baseUrl = split[1];
                userName = split[0];
            } else {
                baseUrl = client.getAccessToken().getUrl();
                userName = user.getScreenName();
            }

            url = "https://"
                    + baseUrl
                    + "/@"
                    + userName;
        } else {
            url = "https://twitter.com/"
                    + user.getScreenName();
        }
        return url;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ThrowableFunc throwableFunc=null;
        @StringRes int didAction = -1;
        ApiClient apiClient = client.getApiClient();

        switch (item.getItemId()){
            case R.id.action_share:
                startActivity(Intent.createChooser(
                                new Intent()
                                        .setAction(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, getShareUrl()),
                                getString(R.string.share)
                ));
                break;
            case R.id.action_open_in_browser:
                AppCustomTabsKt.launchChromeCustomTabs(this, getShareUrl(), true);
                break;
            case R.id.action_create_follow:
                throwableFunc=()->apiClient.createFriendship(user.getId());
                didAction = R.string.did_follow;
                break;
            case R.id.action_destroy_follow:
                throwableFunc=()->apiClient.destroyFriendship(user.getId());
                didAction = R.string.did_unfollow;
                break;
            case R.id.action_create_mute:
                throwableFunc=()->apiClient.createMute(user.getId());
                didAction = R.string.did_mute;
                break;
            case R.id.action_destroy_mute:
                throwableFunc=()->apiClient.destroyMute(user.getId());
                didAction = R.string.did_unmute;
                break;
            case R.id.action_create_block:
                throwableFunc=()->apiClient.createBlock(user.getId());
                didAction = R.string.did_block;
                break;
            case R.id.action_destroy_block:
                throwableFunc=()->apiClient.destroyBlock(user.getId());
                didAction = R.string.did_unblock;
                break;
            case R.id.action_destroy_follow_follower:
                throwableFunc=()->{
                    apiClient.createBlock(user.getId());
                    apiClient.destroyBlock(user.getId());
                };
                didAction = R.string.did_destroy_ff;
                break;
            case R.id.action_spam_report:
                throwableFunc=()->apiClient.reportSpam(user.getId());
                break;
        }

        if (throwableFunc != null && didAction != -1) {
            ThrowableFunc finalThrowableFunc = throwableFunc;
            int finalDidAction = didAction;
            confirmDialog(item.getTitle(), getString(R.string.confirm_message),()->runAsWorkerThread(finalThrowableFunc, finalDidAction));
        }

        return super.onOptionsItemSelected(item);
    }

    private void runAsWorkerThread(ThrowableFunc func, @StringRes int didAction){
        disposable.add(
                Completable.create(
                        subscriber -> {
                            try{
                                func.call();
                                subscriber.onComplete();
                            } catch (Throwable throwable) {
                                subscriber.tryOnError(throwable);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Toast.makeText(this, didAction, Toast.LENGTH_SHORT).show(),
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                }
                        )
        );

    }

    private void confirmDialog(CharSequence title, CharSequence message,Func func){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> func.call()
                )
                .setNegativeButton(android.R.string.cancel,(dialog, which) -> dialog.cancel())
                .show();
    }

    private interface ThrowableFunc<E extends Throwable>{
        void call() throws E;

    }
    private interface Func{
        void call();
    }


    private Single<User> getUserSingle(){
        return Single
                .create(
                        subscriber-> {
                            try {
                                User user = null;
                                if (userId != -1) {
                                    user = client.getApiClient().showUser(userId);
                                } else if (userScreenName != null) {
                                    user = client.getApiClient().showUser(userScreenName);
                                    userId = user.getId();
                                }
                                if (user != null) {
                                    client.getUserCache().add(user);
                                }
                                subscriber.onSuccess(client.getUserCache().get(userId));
                            } catch (Throwable e) {
                                subscriber.tryOnError(e);
                            }
                        }
                );
    }

    @Override
    public View getViewForSnackBar() {
        return findViewById(R.id.activity_show_user_coordinator_layout);
    }

    @Override
    public RecyclerView.RecycledViewPool getUserListViewPool() {
        return userListViewPool;
    }

    @Override
    @NonNull
    public RecyclerView.RecycledViewPool getTweetListViewPool() {
        return tweetListViewPool;
    }

    public static Intent getIntent(Context context, long userId){
        return new Intent(context, ShowUserActivity.class).putExtra("userId", userId);
    }

    public static Intent getIntent(Context context, String userName){
        return new Intent(context, ShowUserActivity.class).putExtra("userScreenName", userName);
    }
}
