package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rx.Observable;
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
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_show_user));
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    @Override
    public Observable<User> getUserObservable(){
        return Observable
                .create(
                        subscriber->{
                            String userName=(String) getIntent().getSerializableExtra("userName");
                            User user=null;
                            if(userName!=null){
                                try {
                                    user=Static.twitter.showUser(userName);
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }else {
                                user=(User) getIntent().getSerializableExtra("user");
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
