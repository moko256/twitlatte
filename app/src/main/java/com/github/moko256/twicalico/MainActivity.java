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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.moko256.twicalico.database.CachedUsersSQLiteOpenHelper;
import com.github.moko256.twicalico.database.TokenSQLiteOpenHelper;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.util.ArrayList;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class MainActivity extends AppCompatActivity implements BaseListFragment.GetSnackBarParentContainerId {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.getChildAt(0).setOnClickListener(v -> {
            Fragment fragment = getMainFragment();
            if (fragment instanceof MoveableTopInterface){
                ((MoveableTopInterface) fragment).moveToTop();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item-> {
            int id = item.getItemId();

            if(!item.isChecked()){
                switch(id){
                    case R.id.nav_timeline:
                        replaceFragment(new HomeTimeLineFragment());
                        break;
                    case R.id.nav_mentions:
                        replaceFragment(new MentionsFragment());
                        break;
                    case R.id.nav_account:
                        startMyUserActivity();
                        break;
                    case R.id.nav_follow_and_follower:
                        replaceFragment(new MyFollowFollowerFragment());
                        break;
                    case R.id.nav_like:
                        replaceFragment(UserLikeFragment.newInstance(GlobalApplication.userId));
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(this,SettingsActivity.class));
                        break;
                }
            }

            drawer.closeDrawer(GravityCompat.START);

            return (id != R.id.nav_settings)&&(id != R.id.nav_account);

        });

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        TextView userNameText = headerView.findViewById(R.id.user_name);
        TextView userIdText = headerView.findViewById(R.id.user_id);
        ImageView userImage = headerView.findViewById(R.id.user_image);
        ImageView userBackgroundImage = headerView.findViewById(R.id.user_bg_image);
        ImageView toggleAccount = headerView.findViewById(R.id.toggle_account);

        toggleAccount.setOnClickListener(v -> {
            TokenSQLiteOpenHelper helper = new TokenSQLiteOpenHelper(this);

            final AlertDialog[] dialog = new AlertDialog[1];

            SelectAccountsAdapter adapter = new SelectAccountsAdapter(this);
            adapter.setOnImageButtonClickListener(i -> {

                AccessToken token = helper.getAccessToken(i);
                if (token.getUserId() != GlobalApplication.userId){
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .edit()
                            .putString("AccountPoint",String.valueOf(i))
                            .apply();
                    ((GlobalApplication) getApplication()).initTwitter(token);
                    startActivity(
                            new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    );
                } else {
                    dialog[0].cancel();
                }
                helper.close();
            });
            adapter.setOnAddButtonClickListener(v1 -> {
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("AccountPoint", "-1")
                        .apply();
                GlobalApplication.twitter = null;
                startActivity(new Intent(this, OAuthActivity.class));
            });

            Single.create(
                    singleSubscriber -> {
                        SQLiteDatabase database = helper.getReadableDatabase();
                        Cursor c=database.query("AccountTokenList",new String[]{"userId", "userName"},null,null,null,null,null);
                        ArrayList<Pair<Uri, String>> r = new ArrayList<>();
                        while (c.moveToNext()){
                            long id = Long.valueOf(c.getString(0));
                            User user = new CachedUsersSQLiteOpenHelper(this, id).getCachedUser(id);
                            if (user ==  null){
                                try {
                                    user = ((GlobalApplication) getApplication()).getTwitterInstance(helper.getAccessToken(c.getPosition())).verifyCredentials();
                                    new CachedUsersSQLiteOpenHelper(this, user.getId()).addCachedUser(user);
                                } catch (TwitterException e) {
                                    singleSubscriber.onError(e);
                                }
                            }
                            r.add(new Pair<>(
                                    Uri.parse(user.getProfileImageURLHttps()),
                                    TwitterStringUtils.plusAtMark(c.getString(1))
                            ));
                        }
                        c.close();
                        database.close();
                        singleSubscriber.onSuccess(r);
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            o -> {
                                adapter.getImagesList().addAll((ArrayList<Pair<Uri, String>>) o);
                                adapter.notifyDataSetChanged();
                            },
                            Throwable::printStackTrace
                    );

            float dp = Math.round(getResources().getDisplayMetrics().density);

            int topPadding = Math.round(20 * dp);
            int bottomPadding = Math.round(8 * dp);

            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            recyclerView.setPadding(0, topPadding, 0, bottomPadding);
            recyclerView.setAdapter(adapter);

            dialog[0] = new AlertDialog.Builder(this)
                    .setTitle(R.string.account)
                    .setView(recyclerView)
                    .show();
        });

        Single.create(
                subscriber->{
                    try {
                        User me = GlobalApplication.userCache.get(GlobalApplication.userId);
                        if (me == null){
                            me = GlobalApplication.twitter.verifyCredentials();
                            GlobalApplication.userCache.add(me);
                        }
                        subscriber.onSuccess(me);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> setDrawerHeader((User) result, userNameText, userIdText, userImage, userBackgroundImage),
                        Throwable::printStackTrace
                );

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(this, PostTweetActivity.class)));

        TabLayout tabLayout= findViewById(R.id.toolbar_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {}

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = ((FragmentPagerAdapter) ((UseTabsInterface) getMainFragment()).getTabsViewPager().getAdapter()).getItem(tab.getPosition());
                if (fragment instanceof MoveableTopInterface){
                    ((MoveableTopInterface) fragment).moveToTop();
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> attachFragment(getMainFragment(), navigationView, tabLayout));

        if(savedInstanceState==null){
            Fragment top=new HomeTimeLineFragment();
            addFragment(top);
            attachFragment(top, navigationView, tabLayout);
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        attachFragment(getMainFragment(), findViewById(R.id.nav_view), findViewById(R.id.toolbar_tab));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_search){
            startActivity(new Intent(this, TrendsActivity.class));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startMyUserActivity() {
        startActivity(ShowUserActivity.getIntent(this, GlobalApplication.userId));
    }

    private void addFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainLayout, fragment)
                .commit();
    }

    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
                .replace(R.id.mainLayout, fragment)
                .commit();
    }

    private Fragment getMainFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.mainLayout);
    }

    @Override
    public int getSnackBarParentContainerId() {
        return R.id.activity_main_coordinator_layout;
    }

    private void attachFragment(Fragment fragment, NavigationView navigationView, TabLayout tabLayout){
        if (fragment != null) {
            if(fragment instanceof ToolbarTitleInterface){
                setTitle(((ToolbarTitleInterface)fragment).getTitleResourceId());
            }

            if(fragment instanceof NavigationPositionInterface){
                navigationView.setCheckedItem(((NavigationPositionInterface)fragment).getNavigationPosition());
            }

            if(fragment instanceof UseTabsInterface) {
                if(tabLayout.getVisibility()!=View.VISIBLE){
                    tabLayout.setVisibility(View.VISIBLE);
                }
                tabLayout.setupWithViewPager(((UseTabsInterface)fragment).getTabsViewPager());
            } else{
                if(tabLayout.getVisibility()!=View.GONE) {
                    tabLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setDrawerHeader(User user, TextView userNameText, TextView userIdText, ImageView userImage, ImageView userBackgroundImage){
        userNameText.setText(user.getName());
        userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName()));

        userImage.setOnClickListener(v -> startMyUserActivity());

        GlideRequests requests=GlideApp.with(this);

        requests.load(user.getBiggerProfileImageURL()).circleCrop().into(userImage);
        requests.load(user.getProfileBannerRetinaURL()).centerCrop().into(userBackgroundImage);
    }

}