package com.github.moko256.twitlatte;

import android.content.Intent;
import android.os.Build;
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

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.User;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Static.twitter == null) {
            finish();
            startActivity(new Intent(this, OAuthActivity.class));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0x00000000);
        }

        Observable
                .create(subscriber->{
                    if(Static.user==null){
                        try {
                            User u=Static.twitter.verifyCredentials();
                            Static.user=u;
                            subscriber.onNext(u);
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    }else{
                        subscriber.onNext(Static.user);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            User user=(User) result;

                            ((TextView)findViewById(R.id.user_name)).setText(user.getName());
                            ((TextView)findViewById(R.id.user_id)).setText(TwitterStringUtil.plusAtMark(user.getScreenName()));

                            ImageView userImage=(ImageView)findViewById(R.id.user_image);
                            ImageView userBackgroundImage=(ImageView)findViewById(R.id.user_bg_image);

                            userImage.setOnClickListener(v -> startMyUserActivity());

                            Glide.with(this).load(user.getBiggerProfileImageURL()).into(userImage);
                            Glide.with(this).load(user.getProfileBannerRetinaURL()).into(userBackgroundImage);
                        },
                        Throwable::printStackTrace,
                        ()->{}
                );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item-> {
            int id = item.getItemId();

            if(!item.isChecked()){
                switch(id){
                    case R.id.nav_timeline:
                        startFragment(new TimeLineFragment());
                        break;
                    case R.id.nav_mentions:
                        startFragment(new MentionsFragment());
                        break;
                    case R.id.nav_account:
                        startMyUserActivity();
                        break;
                    case R.id.nav_follow_and_follower:
                        startFragment(new MyFollowFollowerFragment());
                        break;
                    case R.id.nav_like:
                        startFragment(new LikeMeFragment());
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(this,SettingsActivity.class));
                        break;
                }
            }

            drawer.closeDrawer(GravityCompat.START);

            return (id != R.id.nav_settings)&&(id != R.id.nav_account);

        });

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(this , SendTweetActivity.class)));

        TabLayout tabLayout=(TabLayout) findViewById(R.id.toolbar_tab);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> onFragmentBackStackChanged(navigationView,tabLayout));

        if(savedInstanceState==null){
            startFragment(new TimeLineFragment());
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onFragmentBackStackChanged((NavigationView) findViewById(R.id.nav_view),(TabLayout)findViewById(R.id.toolbar_tab));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar,menu);
        //SearchView searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startFragment(new SearchFragment());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount()>1) {
            getSupportFragmentManager().popBackStack();
        } else {
            //super.onBackPressed();
            finish();
        }
    }

    private void startMyUserActivity() {
        if (Static.user!=null){
            startActivity(new Intent(this, ShowUserActivity.class).putExtra("user", Static.user));
        }
    }

    private void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainLayout, fragment)
                .commit();
    }

    private void onFragmentBackStackChanged(NavigationView navigationView, TabLayout tabLayout){
        Fragment fragment=getSupportFragmentManager().findFragmentById(R.id.mainLayout);
        if (fragment != null) {
            if(fragment instanceof ToolbarTitleInterface && fragment instanceof NavigationPositionInterface){
                setTitle(((ToolbarTitleInterface)fragment).getTitleResourceId());
                navigationView.setCheckedItem(((NavigationPositionInterface)fragment).getNavigationPosition());
            }

            if(fragment instanceof UseTabsInterface) {
                if(tabLayout.getVisibility()!=View.VISIBLE){
                    tabLayout.setVisibility(View.VISIBLE);
                }
                tabLayout.setupWithViewPager(((UseTabsInterface)fragment).getTabsViewPager());
            }
            else{
                if(tabLayout.getVisibility()!=View.GONE) {
                    tabLayout.setVisibility(View.GONE);
                }
            }
        }
    }

}