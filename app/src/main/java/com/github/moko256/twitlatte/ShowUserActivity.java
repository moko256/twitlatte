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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.moko256.latte.client.base.ApiClient;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.viewmodel.UserInfoViewModel;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import kotlin.Unit;

import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, BaseListFragment.GetViewForSnackBar, BaseTweetListFragment.GetRecyclerViewPool, BaseUsersFragment.GetRecyclerViewPool {

    private UserInfoViewModel viewModel;
    private Client client;

    private String userScreenName;
    private long userId;

    private ActionBar actionBar;
    private ViewPager viewPager;
    private ShowUserFragmentsPagerAdapter adapter;
    private TabLayout tabLayout;

    private RecyclerView.RecycledViewPool tweetListViewPool;
    private RecyclerView.RecycledViewPool userListViewPool;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        client = GlobalApplicationKt.getClient(this);

        viewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);

        setSupportActionBar(findViewById(R.id.toolbar_show_user));

        actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        viewPager= findViewById(R.id.show_user_view_pager);
        adapter = new ShowUserFragmentsPagerAdapter(
                client.getAccessToken(),
                getSupportFragmentManager(),
                this
        );
        adapter.initAdapter(viewPager);

        tabLayout= findViewById(R.id.tab_show_user);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(this);

        tweetListViewPool = new RecyclerView.RecycledViewPool();
        userListViewPool = new RecyclerView.RecycledViewPool();

        findViewById(R.id.activity_show_user_fab).setOnClickListener(v -> {
            User user = viewModel.getUser().getValue();
            if (user != null){
                startActivity(PostActivity.getIntent(this, TwitterStringUtils.plusAtMark(user.getScreenName())+" "));
            }
        });


        userScreenName = getIntent().getStringExtra("userScreenName");
        userId = getIntent().getLongExtra("userId", -1);

        viewModel.readCacheRepo = () -> {
            if (userId != -1) {
                return client.getUserCache().get(userId);
            } else {
                return null;
            }
        };

        viewModel.writeCacheRepo = user -> {
            client.getUserCache().add(user);
            return Unit.INSTANCE;
        };

        viewModel.remoteRepo = () -> {
            if (userId != -1) {
                return client.getApiClient().showUser(userId);
            } else if (userScreenName != null) {
                return client.getApiClient().showUser(userScreenName);
            } else {
                throw new IllegalStateException("Unreachable");
            }
        };

        viewModel.getUser().observe(this, user -> adapter.setUserId(user.getId()));
        viewModel.getAction().observe(
                this,
                message -> Snackbar.make(
                        getViewForSnackBar(),
                        message,
                        Snackbar.LENGTH_LONG
                ).show()
        );
        viewModel.getError().observe(
                this,
                throwable -> {
                    throwable.printStackTrace();
                    Snackbar.make(
                            getViewForSnackBar(),
                            throwable.getMessage(),
                            Snackbar.LENGTH_LONG
                    ).show();
                }
        );

        if (savedInstanceState == null) {
            viewModel.loadUser();
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client = null;

        tabLayout=null;
        viewPager=null;
        actionBar=null;

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
        User user = viewModel.getUser().getValue();

        if (user == null) {
            return "";
        }

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
        Func throwableFunc = null;
        @StringRes int didAction = -1;
        ApiClient apiClient = client.getApiClient();
        User user = viewModel.getUser().getValue();

        if (user == null) {
            return false;
        }
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

            case R.id.action_add_to_list:
                startActivityForResult(
                        GlobalApplicationKt.setAccountKeyForActivity(
                                new Intent(this, SelectListEntriesActivity.class)
                                        .putExtra(
                                                "userId",
                                                client.getAccessToken().getUserId()
                                        ),
                                this
                        ),
                        200
                );
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
                didAction = R.string.did_report_as_spam;
                break;
        }

        if (throwableFunc != null && didAction != -1) {
            Func finalThrowableFunc = throwableFunc;
            int finalDidAction = didAction;
            confirmDialog(item.getTitle(), getString(R.string.confirm_message),()->runAsWorkerThread(finalThrowableFunc, finalDidAction));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            confirmDialog(
                    getString(R.string.add_to_list),
                    getString(R.string.confirm_message),
                    () -> runAsWorkerThread(
                            () -> client
                                    .getApiClient()
                                    .addToLists(
                                            data.getLongExtra("listId", -1),
                                            userId
                                    ),
                            R.string.did_add_to_list
                    )
            );
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void runAsWorkerThread(Func func, @StringRes int didAction){
        viewModel.doAction(
                () -> {
                    func.call();
                    return Unit.INSTANCE;
                },
                getString(didAction)
        );
    }

    private void confirmDialog(CharSequence title, CharSequence message, Func func){
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

    private interface Func{
        void call();
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
