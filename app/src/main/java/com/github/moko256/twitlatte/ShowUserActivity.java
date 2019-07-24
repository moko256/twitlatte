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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.viewmodel.UserInfoViewModel;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity

        extends
        AppCompatActivity

        implements
        TabLayout.OnTabSelectedListener,
        BaseListFragment.GetViewForSnackBar,
        BaseTweetListFragment.GetRecyclerViewPool,
        BaseUsersFragment.GetRecyclerViewPool,
        HasRefreshLayoutInterface,
        HasNotifiableAppBar {

    private UserInfoViewModel viewModel;
    private Client client;

    private ActionBar actionBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ViewPager viewPager;
    private ShowUserFragmentsPagerAdapter adapter;
    private TabLayout tabLayout;

    private RecyclerView.RecycledViewPool recycledViewPool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        client = GlobalApplicationKt.getClient(this);

        viewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);

        setSupportActionBar(findViewById(R.id.toolbar_show_user));

        actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        swipeRefreshLayout = findViewById(R.id.activity_show_user_swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);

        appBarLayout = findViewById(R.id.appbar_show_user);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_show_user);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        viewPager = findViewById(R.id.show_user_view_pager);

        new ScrollListener(appBarLayout, viewPager, swipeRefreshLayout, collapsingToolbarLayout);

        adapter = new ShowUserFragmentsPagerAdapter(
                client.getAccessToken(),
                getSupportFragmentManager(),
                this
        );
        adapter.initAdapter(viewPager);

        tabLayout = findViewById(R.id.tab_show_user);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(this);

        recycledViewPool = new RecyclerView.RecycledViewPool();

        findViewById(R.id.activity_show_user_fab).setOnClickListener(v -> {
            User user = viewModel.getUser().getValue();
            if (user != null) {
                startActivity(PostActivity.getIntent(this, TwitterStringUtils.plusAtMark(user.getScreenName()) + " "));
            }
        });


        viewModel.setUserName(getIntent().getStringExtra("userScreenName"));
        viewModel.setUserId(getIntent().getLongExtra("userId", -1));
        viewModel.client = client;

        viewModel.getUser().observe(this, user -> {
            adapter.setUserId(user.getId());
            collapsingToolbarLayout.setTitle(user.getName());
        });
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
            viewModel.loadData(true);
        }
    }

    @Override
    public void requestAppBarCollapsed() {
        appBarLayout.setExpanded(false, true);
    }

    @NotNull
    @Override
    public SwipeRefreshLayout get() {
        return swipeRefreshLayout;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Fragment fragment = Objects.requireNonNull((FragmentPagerAdapter) viewPager.getAdapter()).getFragment(tab.getPosition());
        if (fragment instanceof MovableTopInterface) {
            ((MovableTopInterface) fragment).moveToTop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client = null;

        tabLayout = null;
        viewPager = null;
        appBarLayout = null;
        swipeRefreshLayout = null;
        actionBar = null;

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_show_user_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getShareUrl() {
        String url;
        User user = viewModel.getUser().getValue();

        if (user == null) {
            return "";
        }

        if (client.getAccessToken().getClientType() == CLIENT_TYPE_MASTODON) {
            String baseUrl;
            String userName;
            if (user.getScreenName().matches(".*@.*")) {
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
        if (viewModel.getUser().getValue() == null) {
            return false;
        }
        switch (item.getItemId()) {
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
                confirmDialog(
                        getString(R.string.confirm_follow),
                        () -> viewModel.requestCreateFollow(getString(R.string.did_follow))
                );
                break;

            case R.id.action_destroy_follow:
                confirmDialog(
                        getString(R.string.confirm_unfollow),
                        () -> viewModel.requestDestroyFollow(getString(R.string.did_unfollow))
                );
                break;

            case R.id.action_create_mute:
                confirmDialog(
                        getString(R.string.confirm_mute),
                        () -> viewModel.requestCreateMute(getString(R.string.did_mute))
                );
                break;

            case R.id.action_destroy_mute:
                confirmDialog(
                        getString(R.string.confirm_unmute),
                        () -> viewModel.requestDestroyMute(getString(R.string.did_unmute))
                );
                break;

            case R.id.action_create_block:
                confirmDialog(
                        getString(R.string.confirm_block),
                        () -> viewModel.requestCreateBlock(getString(R.string.did_block))
                );
                break;

            case R.id.action_destroy_block:
                confirmDialog(
                        getString(R.string.confirm_unblock),
                        () -> viewModel.requestDestroyBlock(getString(R.string.did_unblock))
                );
                break;

            case R.id.action_destroy_follow_follower:
                confirmDialog(
                        getString(R.string.confirm_destroy_ff),
                        () -> viewModel.requestDestroyF2F(getString(R.string.did_destroy_ff))
                );
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            viewModel.requestAddToList(
                    getString(R.string.did_add_to_list),
                    data.getLongExtra("listId", -1)
            );
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void confirmDialog(CharSequence message, Func func) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> func.call()
                )
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    private interface Func {
        void call();
    }

    @Override
    public View getViewForSnackBar() {
        return findViewById(R.id.activity_show_user_coordinator_layout);
    }

    @Override
    public RecyclerView.RecycledViewPool getUserListViewPool() {
        return recycledViewPool;
    }

    @Override
    @NonNull
    public RecyclerView.RecycledViewPool getTweetListViewPool() {
        return recycledViewPool;
    }

    public static Intent getIntent(Context context, long userId) {
        return new Intent(context, ShowUserActivity.class).putExtra("userId", userId);
    }

    public static Intent getIntent(Context context, String userName) {
        return new Intent(context, ShowUserActivity.class).putExtra("userScreenName", userName);
    }

    private static class ScrollListener implements ViewPager.OnPageChangeListener, AppBarLayout.OnOffsetChangedListener {
        private SwipeRefreshLayout swipeRefreshLayout;
        private CollapsingToolbarLayout collapsingToolbarLayout;

        ScrollListener(AppBarLayout appBarLayout, ViewPager viewPager, SwipeRefreshLayout swipeRefreshLayout, CollapsingToolbarLayout collapsingToolbarLayout) {
            this.swipeRefreshLayout = swipeRefreshLayout;
            this.collapsingToolbarLayout = collapsingToolbarLayout;

            appBarLayout.addOnOffsetChangedListener(this);
            viewPager.addOnPageChangeListener(this);
        }

        boolean appBarStopping = true;
        boolean viewPagerStopping = true;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            viewPagerStopping = state == SCROLL_STATE_IDLE;
            updatePullToRefresh();
        }

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            appBarStopping = i == 0;
            updatePullToRefresh();
            collapsingToolbarLayout.setTitleEnabled(Math.abs(i) >= appBarLayout.getTotalScrollRange());
        }

        private void updatePullToRefresh() {
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setEnabled(viewPagerStopping && appBarStopping);
            }
        }
    }
}
