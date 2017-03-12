package com.github.moko256.twicalico;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity extends AppCompatActivity implements ActivityHasUserObservable,BaseListFragment.GetSnackBarParentContainerId {

    User user;

    ActionBar actionBar;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        if (savedInstanceState != null) {
            user=(User)savedInstanceState.getSerializable("user");
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_show_user));

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        viewPager=(ViewPager) findViewById(R.id.show_user_view_pager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new ShowUserFragmentsPagerAdapter(getSupportFragmentManager(),this));

        tabLayout=(TabLayout) findViewById(R.id.tab_show_user);
        tabLayout.setupWithViewPager(viewPager);

        findViewById(R.id.activity_show_user_fab).setOnClickListener(v -> {
            if (user!=null){
                startActivity(SendTweetActivity.getIntent(this, TwitterStringUtil.plusAtMark(user.getScreenName())+" "));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tabLayout=null;
        viewPager=null;
        actionBar=null;

        user=null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putSerializable("user", user);
        super.onSaveInstanceState(outState);
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
                                .putExtra(Intent.EXTRA_TEXT,"https://twitter.com/"+user.getScreenName())
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
            confirmDialog("Summer vacation","Are you ok?",()->runAsWorkerThread(finalThrowableFunc));
        }

        return super.onOptionsItemSelected(item);
    }

    private void runAsWorkerThread(ThrowableFunc func){
        Observable
                .create(subscriber -> {
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
                        o -> {},
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        },
                        ()->Toast.makeText(this, R.string.succeeded, Toast.LENGTH_SHORT).show()
                );

    }

    private void confirmDialog(String title, String message,Func func){
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


    @Override
    public Observable<User> getUserObservable(){
        return Observable
                .create(
                        subscriber-> {
                            String userName = null;

                            String userNameByExtra = (String) getIntent().getSerializableExtra("userName");
                            if (userNameByExtra != null){
                                userName = userNameByExtra;
                            } else {
                                Uri data = getIntent().getData();

                                if (data != null){
                                    String scheme = data.getScheme();

                                    String userNameByLink = null;
                                    if (scheme.equals("https")){
                                        List<String> paths = data.getPathSegments();
                                        userNameByLink = paths.get(paths.size());
                                    } else if(scheme.equals("twitter")){
                                        String path = data.getPath();
                                        if (path.matches(".*\\?(id|screen_name)=.+")){
                                            userNameByLink = Pattern.compile("(?<=(.*(id|screen_name)=)).+").matcher(path).group();
                                        }
                                    }

                                    userName = userNameByLink;

                                }
                            }
                            if (user==null||!user.getScreenName().equals(userName)){
                                if (userName!=null){
                                    try {
                                        user=GlobalApplication.twitter.showUser(userName);
                                    } catch (TwitterException e) {
                                        subscriber.onError(e);
                                    }
                                } else {
                                    user=(User) getIntent().getSerializableExtra("user");
                                }
                            }
                            subscriber.onNext(user);
                            subscriber.onCompleted();
                        }
                );
    }

    @Override
    public int getSnackBarParentContainerId() {
        return R.id.activity_show_user_coordinator_layout;
    }
}
