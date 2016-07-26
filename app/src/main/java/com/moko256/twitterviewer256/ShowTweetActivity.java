package com.moko256.twitterviewer256;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on GitHub on 2016/03/10.
 */
public class ShowTweetActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        Toolbar toolbar=(Toolbar)findViewById(R.id.tweet_show_tool_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        new AsyncTask<Void,Void,Status>(){
            @Override
            public twitter4j.Status doInBackground(Void... str){
                Long statusId=(Long) getIntent().getSerializableExtra("statusId");
                twitter4j.Status status=null;
                if(statusId!=null){
                    try {
                        status=Static.twitter.showStatus(statusId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }else {
                    status=(twitter4j.Status) getIntent().getSerializableExtra("status");
                }
                return status;
            }

            @Override
            public void onPostExecute(twitter4j.Status item){
                ImageView userImage=(ImageView) findViewById(R.id.tweet_show_image);

                Glide.with(ShowTweetActivity.this).load(item.getUser().getProfileImageURL()).into(userImage);

                ((TextView) findViewById(R.id.tweet_show_user_name)).setText(item.getUser().getName());
                ((TextView) findViewById(R.id.tweet_show_user_id)).setText(Static.plusAtMark(item.getUser().getScreenName()));
                ((TextView) findViewById(R.id.tweet_show_content)).setText(item.getText());

                userImage.setOnClickListener(v->{
                    Intent intent = new Intent(ShowTweetActivity.this,ShowUserActivity.class);
                    intent.putExtra("user",item.getUser());
                    startActivity(intent);
                });

                ((TweetImageTableView)findViewById(R.id.tweet_show_images)).setTwitterMediaEntities(item.getExtendedMediaEntities());
            }
        }.execute();

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}
