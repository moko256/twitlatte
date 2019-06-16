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
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.latte.client.base.MediaUrlConverter;
import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.latte.client.base.entity.Emoji;
import com.github.moko256.latte.client.base.entity.ListEntry;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.database.CachedUsersSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.model.AccountsModel;
import com.github.moko256.twitlatte.rx.VerifyCredentialOnSubscribe;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ALWAYS_CLOSE_APP;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class MainActivity extends AppCompatActivity implements
        DrawerLayout.DrawerListener,
        TabLayout.OnTabSelectedListener,
        BaseListFragment.GetViewForSnackBar,
        BaseTweetListFragment.GetRecyclerViewPool,
        BaseUsersFragment.GetRecyclerViewPool,
        SelectListsEntriesFragment.ListEntrySelectionListener {

    private static final int REQUEST_OAUTH = 2;

    private CompositeDisposable disposable;
    private Client client;
    private AccountsModel accountsModel;

    @Nullable
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private TextView userNameText;
    private TextView userIdText;
    private ImageView userImage;
    private ImageView userBackgroundImage;
    private ImageView userToggleImage;
    private RecyclerView accountListView;
    private SelectAccountsAdapter adapter;

    private TabLayout tabLayout;

    private boolean isDrawerAccountsSelection = false;

    private RecyclerView.RecycledViewPool recycledViewPool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainActivityTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        disposable = new CompositeDisposable();
        client = GlobalApplicationKt.getClient(this);
        accountsModel = GlobalApplicationKt.getAccountsModel(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.getChildAt(0).setOnClickListener(v -> {
            Fragment fragment = getMainFragment();
            if (fragment instanceof MovableTopInterface){
                ((MovableTopInterface) fragment).moveToTop();
            }
        });

        drawer = findViewById(R.id.drawer_layout);

        if (drawer != null){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            toggle.setDrawerSlideAnimationEnabled(false);
            toggle.syncState();

            drawer.addDrawerListener(this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_primary_dark));
        }

        navigationView = findViewById(R.id.nav_view);
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
                        replaceFragment(UserLikeFragment.Companion.newInstance(client.getAccessToken().getUserId()));
                        break;
                    case R.id.nav_lists:
                        replaceFragment(SelectListsEntriesFragment.Companion.newInstance(client.getAccessToken().getUserId()));
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(this,SettingsActivity.class));
                        break;
                }
            }

            if (drawer != null) {
                drawer.closeDrawer(GravityCompat.START);
            }
            return (id != R.id.nav_settings)&&(id != R.id.nav_account);

        });

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        userNameText = headerView.findViewById(R.id.user_name);
        userIdText = headerView.findViewById(R.id.user_id);
        userImage = headerView.findViewById(R.id.user_image);
        userToggleImage = headerView.findViewById(R.id.toggle_account);
        userBackgroundImage = headerView.findViewById(R.id.user_bg_image);
        userBackgroundImage.setOnClickListener(v -> changeIsDrawerAccountsSelection());

        accountListView = new RecyclerView(this);
        accountListView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        accountListView.setLayoutManager(new LinearLayoutManager(this));
        accountListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        accountListView.setVisibility(View.GONE);
        navigationView.addHeaderView(accountListView);

        adapter = new SelectAccountsAdapter(this);
        adapter.onImageButtonClickListener = accessToken -> {
            if (accessToken.getUserId() != client.getAccessToken().getUserId()) {
                changeIsDrawerAccountsSelection();
                if (drawer != null) {
                    drawer.closeDrawer(GravityCompat.START);
                }

                GlobalApplicationKt.preferenceRepository.putString(
                        KEY_ACCOUNT_KEY, accessToken.getKeyString()
                );
                ((GlobalApplication) getApplication()).initCurrentClient(accessToken);
                client = GlobalApplicationKt.getClient(this);
                adapter.updateSelectedPosition(accessToken);
                updateDrawerImage();
                clearAndPrepareFragment();
            }
        };
        adapter.onAddButtonClickListener = v -> startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
        adapter.onRemoveButtonClickListener = v -> new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_logout)
                .setCancelable(true)
                .setPositiveButton(R.string.do_logout,
                        (dialog, i) -> {
                            AccessToken token = accountsModel.get(
                                    GlobalApplicationKt.preferenceRepository.getString(KEY_ACCOUNT_KEY, "-1")
                            );
                            accountsModel.delete(token);
                            adapter.removeAccessTokensAndUpdate(token);

                            int point = accountsModel.size() - 1;
                            if (point != -1) {
                                AccessToken accessToken = accountsModel.getAccessTokens().get(point);
                                GlobalApplicationKt.preferenceRepository.putString(
                                        KEY_ACCOUNT_KEY, accessToken.getKeyString()
                                );
                                ((GlobalApplication) getApplication()).initCurrentClient(accessToken);
                                client = GlobalApplicationKt.getClient(this);
                                adapter.updateSelectedPosition(accessToken);
                                updateDrawerImage();
                                updateAccountsList();
                                clearAndPrepareFragment();
                            } else {
                                GlobalApplicationKt.preferenceRepository.putString(
                                        KEY_ACCOUNT_KEY, "-1"
                                );
                                ((GlobalApplication) getApplication()).clearCurrentClient();
                                startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        accountListView.setAdapter(adapter);

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(this, PostActivity.class)));

        tabLayout= findViewById(R.id.toolbar_tab);
        tabLayout.addOnTabSelectedListener(this);

        recycledViewPool = new RecyclerView.RecycledViewPool();
        recycledViewPool.setMaxRecycledViews(R.layout.layout_post_card, 16);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> attachFragment(getMainFragment()));

        if (client == null) {
            startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
        } else {
            updateDrawerImage();
            updateAccountsList();

            if(savedInstanceState==null){
                prepareFragment();
            }

            if (client.getAccessToken().getToken().isEmpty()) {
                Toast.makeText(this, R.string.please_re_login, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onListItemSelected(@NotNull ListEntry listEntry) {
        replaceFragment(ListsTimelineFragment.Companion.newInstance(listEntry));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {}

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Fragment fragment = Objects.requireNonNull(
                (FragmentPagerAdapter)
                        ((UseTabsInterface) getMainFragment())
                                .getTabsViewPager()
                                .getAdapter()
        ).getFragment(tab.getPosition());
        if (fragment instanceof MovableTopInterface){
            ((MovableTopInterface) fragment).moveToTop();
        }
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
        disposable = null;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        attachFragment(getMainFragment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OAUTH) {
            client = GlobalApplicationKt.getClient(this);

            if (resultCode == RESULT_OK) {
                updateDrawerImage();
                updateAccountsList();
                clearAndPrepareFragment();
            } else if (client == null) {
                super.finish();
            }
        }
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
        if (drawer!= null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        if (GlobalApplicationKt.preferenceRepository.getBoolean(KEY_ALWAYS_CLOSE_APP, true)) {
            super.finish();
        } else {
            boolean result = moveTaskToBack(false);
            if (!result) {
                super.finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_N){
            startActivity(new Intent(this, PostActivity.class));
        } else if (event.isShiftPressed() && keyCode == KeyEvent.KEYCODE_SLASH){
            new AlertDialog.Builder(this)
                    .setTitle("KeyBoard Shortcuts")
                    .setMessage("? : This Dialog\nn : New Post\nCtrl + Tab : Open Drawer\nCtrl + Enter : Post")
                    .show();
        } else if (drawer != null && event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_TAB){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {}

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        if (isDrawerAccountsSelection){
            changeIsDrawerAccountsSelection();
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {}

    private void changeIsDrawerAccountsSelection() {
        isDrawerAccountsSelection = !isDrawerAccountsSelection;

        accountListView.setVisibility(isDrawerAccountsSelection? View.VISIBLE: View.GONE);

        userToggleImage.setRotation(isDrawerAccountsSelection? 180: 0);

        navigationView.getMenu().setGroupVisible(R.id.drawer_menu_main, !isDrawerAccountsSelection);
        navigationView.getMenu().setGroupVisible(R.id.drawer_menu_settings, !isDrawerAccountsSelection);
    }

    private void startMyUserActivity() {
        ActivityOptionsCompat animation = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                userImage,
                "icon_image"
        );
        startActivity(ShowUserActivity.getIntent(this, client.getAccessToken().getUserId()), animation.toBundle());
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
    public View getViewForSnackBar() {
        return findViewById(R.id.activity_main_coordinator_layout);
    }

    private void attachFragment(Fragment fragment){
        if (fragment != null) {
            if(fragment instanceof ToolbarTitleInterface){
                setTitle(((ToolbarTitleInterface)fragment).getTitleResourceId());
            } else if (fragment instanceof ToolbarStringTitleInterface) {
                setTitle(((ToolbarStringTitleInterface) fragment).getTitleString());
            }

            if(fragment instanceof NavigationPositionInterface){
                navigationView.setCheckedItem(((NavigationPositionInterface)fragment).getNavigationPosition());
            }

            if(fragment instanceof UseTabsInterface) {
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.setupWithViewPager(((UseTabsInterface)fragment).getTabsViewPager());
            } else{
                tabLayout.setVisibility(View.GONE);
            }
        }
    }

    private void updateDrawerImage(){
        disposable.add(
                Single.create(new VerifyCredentialOnSubscribe(client))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    RequestManager requests= Glide.with(this);

                                    CharSequence userName = TwitterStringUtils.plusUserMarks(
                                            user.getName(),
                                            userNameText,
                                            user.isProtected(),
                                            user.isVerified()
                                    );

                                    userNameText.setText(userName);
                                    Emoji[] userNameEmojis = user.getEmojis();
                                    if (userNameEmojis != null) {
                                        disposable.add(new EmojiToTextViewSetter(requests, userNameText, userName, userNameEmojis));
                                    }
                                    userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName(), client.getAccessToken().getUrl()));

                                    userImage.setOnClickListener(v -> startMyUserActivity());

                                    MediaUrlConverter mediaUrlConverter = client.getMediaUrlConverter();
                                    requests.load(mediaUrlConverter.convertProfileIconLargeUrl(user))
                                            .circleCrop()
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(userImage);
                                    requests.load(mediaUrlConverter.convertProfileBannerSmallUrl(user))
                                            .centerCrop()
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(userBackgroundImage);
                                },
                                Throwable::printStackTrace
                        )
        );

    }

    private void updateAccountsList() {
        disposable.add(
                Single.create(
                        singleSubscriber -> {
                            try {
                                List<AccessToken> accessTokens = accountsModel.getAccessTokens();

                                ArrayList<User> users = new ArrayList<>(accessTokens.size());
                                for (AccessToken accessToken : accessTokens){
                                    long id = accessToken.getUserId();
                                    CachedUsersSQLiteOpenHelper userHelper = new CachedUsersSQLiteOpenHelper(getApplicationContext(), accessToken);
                                    User user = userHelper.getCachedUser(id);
                                    if (user == null){
                                        try {
                                            user = ((GlobalApplication) getApplication()).createApiClientInstance(accessToken).verifyCredentials();
                                            userHelper.addCachedUser(user);
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        } finally {
                                            userHelper.close();
                                        }
                                    }
                                    users.add(user);
                                }
                                singleSubscriber.onSuccess(new Pair<>(users, accessTokens));
                            } catch (Throwable e) {
                                singleSubscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                o -> {
                                    @SuppressWarnings("unchecked")
                                    Pair<ArrayList<User>, ArrayList<AccessToken>> pairs = (Pair<ArrayList<User>, ArrayList<AccessToken>>) o;

                                    adapter.clearImages();
                                    adapter.addAndUpdate(pairs.first, pairs.second);
                                    adapter.setSelectedPosition(client.getAccessToken());
                                    adapter.notifyDataSetChanged();
                                },
                                Throwable::printStackTrace
                        )
        );
    }

    private void prepareFragment(){
        Fragment top=new HomeTimeLineFragment();
        addFragment(top);
        attachFragment(top);
    }

    private void clearAndPrepareFragment(){
        Fragment top=new HomeTimeLineFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager
                .beginTransaction()
                .replace(R.id.mainLayout, top)
                .commit();

        attachFragment(top);
    }

    @Override
    @NonNull
    public RecyclerView.RecycledViewPool getTweetListViewPool() {
        return recycledViewPool;
    }

    @Override
    public RecyclerView.RecycledViewPool getUserListViewPool() {
        return recycledViewPool;
    }
}