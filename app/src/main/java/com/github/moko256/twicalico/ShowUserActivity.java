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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.moko256.twicalico.text.TwitterStringUtils;

import rx.Completable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity extends AppCompatActivity implements BaseListFragment.GetSnackBarParentContainerId {

    CompositeSubscription subscription;

    User user;

    ActionBar actionBar;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        subscription = new CompositeSubscription();

        setSupportActionBar(findViewById(R.id.toolbar_show_user));

        actionBar=getSupportActionBar();
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
                Fragment fragment = ((FragmentPagerAdapter) viewPager.getAdapter()).getItem(tab.getPosition());
                if (fragment instanceof MoveableTopInterface){
                    ((MoveableTopInterface) fragment).moveToTop();
                }
            }
        });

        findViewById(R.id.activity_show_user_fab).setOnClickListener(v -> {
            if (user!=null){
                startActivity(PostTweetActivity.getIntent(this, TwitterStringUtils.plusAtMark(user.getScreenName())+" "));
            }
        });
        subscription.add(
                getUserSingle()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                it -> viewPager.setAdapter(new ShowUserFragmentsPagerAdapter(getSupportFragmentManager(),this,it.getId())),
                                e -> Snackbar.make(findViewById(getSnackBarParentContainerId()), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                        .show()
                        )
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscription.unsubscribe();
        subscription = null;

        tabLayout=null;
        viewPager=null;
        actionBar=null;

        user=null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_show_user_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ThrowableFunc throwableFunc=null;
        Twitter twitter = GlobalApplication.twitter;

        switch (item.getItemId()){
            case R.id.action_share:
                startActivity(
                        new Intent()
                                .setAction(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" + user.getScreenName())
                );
                break;
            case R.id.action_create_follow:
                throwableFunc=()->twitter.createFriendship(user.getId());
                break;
            case R.id.action_destroy_follow:
                throwableFunc=()->twitter.destroyFriendship(user.getId());
                break;
            case R.id.action_create_mute:
                throwableFunc=()->twitter.createMute(user.getId());
                break;
            case R.id.action_destroy_mute:
                throwableFunc=()->twitter.destroyMute(user.getId());
                break;
            case R.id.action_create_block:
                throwableFunc=()->twitter.createBlock(user.getId());
                break;
            case R.id.action_destroy_block:
                throwableFunc=()->twitter.destroyBlock(user.getId());
                break;
            case R.id.action_destroy_follow_follower:
                throwableFunc=()->{
                    twitter.createBlock(user.getId());
                    twitter.destroyBlock(user.getId());
                };
                break;
            case R.id.action_spam_report:
                throwableFunc=()->GlobalApplication.twitter.reportSpam(user.getId());
                break;
        }

        if (throwableFunc != null) {
            ThrowableFunc finalThrowableFunc = throwableFunc;
            confirmDialog(item.getTitle(), getString(R.string.confirm_message),()->runAsWorkerThread(finalThrowableFunc));
        }

        return super.onOptionsItemSelected(item);
    }

    private void runAsWorkerThread(ThrowableFunc func){
        Completable.create(
                subscriber -> {
                    try{
                        func.call();
                        subscriber.onCompleted();
                    } catch (Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(this, R.string.succeeded, Toast.LENGTH_SHORT).show(),
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        }
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


    public Single<User> getUserSingle(){
        return Single
                .create(
                        subscriber-> {
                            if (user==null){
                                String userScreenName = getIntent().getStringExtra("userScreenName");
                                long userId = getIntent().getLongExtra("userId", -1);

                                if (userId != -1){
                                    user = GlobalApplication.userCache.get(userId);
                                }

                                if (user==null) {
                                    try {
                                        if (userId != -1) {
                                            user = GlobalApplication.twitter.showUser(userId);
                                        } else if (userScreenName != null) {
                                            user = GlobalApplication.twitter.showUser(userScreenName);
                                            GlobalApplication.userCache.add(user);
                                        }
                                    } catch (TwitterException e) {
                                        subscriber.onError(e);
                                    }
                                }
                            }

                            if (user != null){
                                subscriber.onSuccess(user);
                            }
                        }
                );
    }

    @Override
    public int getSnackBarParentContainerId() {
        return R.id.activity_show_user_coordinator_layout;
    }

    public static Intent getIntent(Context context, long userId){
        return new Intent(context, ShowUserActivity.class).putExtra("userId", userId);
    }

    public static Intent getIntent(Context context, String userName){
        return new Intent(context, ShowUserActivity.class).putExtra("userScreenName", userName);
    }
}
