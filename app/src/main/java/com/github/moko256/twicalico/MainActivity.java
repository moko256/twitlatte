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
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.User;

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

        Observable
                .create(subscriber->{
                    try {
                        User me = GlobalApplication.userCache.get(GlobalApplication.userId);
                        if (me == null){
                            me = GlobalApplication.twitter.verifyCredentials();
                            GlobalApplication.userCache.add(me);
                        }
                        subscriber.onNext(me);
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> setDrawerHeader((User) result, userNameText, userIdText, userImage, userBackgroundImage),
                        Throwable::printStackTrace,
                        ()->{}
                );

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(this, SendTweetActivity.class)));

        TabLayout tabLayout= findViewById(R.id.toolbar_tab);

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
        userIdText.setText(TwitterStringUtil.plusAtMark(user.getScreenName()));

        userImage.setOnClickListener(v -> startMyUserActivity());

        RequestManager manager=Glide.with(this);

        manager.load(user.getBiggerProfileImageURL()).asBitmap().into(new CircleImageTarget(userImage));
        manager.load(user.getProfileBannerRetinaURL()).centerCrop().into(userBackgroundImage);
    }

}