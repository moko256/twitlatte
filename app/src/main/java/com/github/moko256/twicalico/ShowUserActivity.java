package com.github.moko256.twicalico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Objects;

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
public class ShowUserActivity extends AppCompatActivity implements ShowUserFragment.HasUserActivity {

    User user;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.show_user_fragment_container, new ShowUserFragment())
                    .commit();
        } else {
            user=(User)savedInstanceState.getSerializable("user");
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_show_user));
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

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
        Twitter twitter = Static.twitter;

        switch (item.getItemId()){
            case R.id.action_share:
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"https://twitter.com/"+user.getScreenName());
                startActivity(intent);
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
                throwableFunc=()->Static.twitter.reportSpam(user.getId());
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
                            Toast.makeText(this,"Error occurred.",Toast.LENGTH_SHORT).show();
                        },
                        ()->Toast.makeText(this,"Succeeded.",Toast.LENGTH_SHORT).show()
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
                        subscriber->{
                            String userName=(String) getIntent().getSerializableExtra("userName");
                            if(!(user!=null&& Objects.equals(user.getScreenName(), userName))){
                                if(userName!=null){
                                    try {
                                        user=Static.twitter.showUser(userName);
                                    } catch (TwitterException e) {
                                        subscriber.onError(e);
                                    }
                                }else {
                                    user=(User) getIntent().getSerializableExtra("user");
                                }
                            }
                            subscriber.onNext(user);
                            subscriber.onCompleted();
                        }
                );
    }

    @Override
    public void updateHeader(User user) {
        ImageView header=(ImageView) findViewById(R.id.show_user_bgimage);
        ImageView icon=(ImageView) findViewById(R.id.show_user_image);

        Glide.with(this).load(user.getProfileBannerRetinaURL()).into(header);
        Glide.with(this).load(user.getBiggerProfileImageURL()).into(icon);

        ((TextView) findViewById(R.id.show_user_id)).setText(TwitterStringUtil.plusAtMark(user.getScreenName()));
        setTitle(user.getName());
        ((TextView) findViewById(R.id.show_user_bio)).setText(user.getDescription());
    }
}
