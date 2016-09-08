package com.moko256.twitterviewer256;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on GitHub on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        new AsyncTask<Void,Void,Status>(){
            @Override
            public twitter4j.Status doInBackground(Void... str){
                Long statusId;
                twitter4j.Status status=(twitter4j.Status) getIntent().getSerializableExtra("status");
                if(status==null) {
                    statusId = (Long) getIntent().getSerializableExtra("statusId");
                    if (statusId == null) {
                        Uri data = getIntent().getData();
                        if (data.getScheme().equals("https")||data.getScheme().equals("http")) {
                            statusId = Long.parseLong(data.getPathSegments().get(2), 10);
                        } else if (data.getScheme().equals("twitter")) {
                            statusId = Long.parseLong(data.getQueryParameter("id"), 10);
                        } else {
                            ShowTweetActivity.this.finish();
                        }
                    }
                    try {
                        status = Static.twitter.showStatus(statusId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
                return status;
            }

            @Override
            public void onPostExecute(twitter4j.Status item){
                ImageView userImage=(ImageView) findViewById(R.id.tweet_show_image);

                Glide.with(ShowTweetActivity.this).load(item.getUser().getProfileImageURL()).into(userImage);

                ((TextView) findViewById(R.id.tweet_show_user_name)).setText(item.getUser().getName());
                ((TextView) findViewById(R.id.tweet_show_user_id)).setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
                TextView contentText=(TextView) findViewById(R.id.tweet_show_content);
                contentText.setText(TwitterStringUtil.getLinkedSequence(item,ShowTweetActivity.this));
                contentText.setMovementMethod(LinkMovementMethod.getInstance());

                userImage.setOnClickListener(v->{
                    Intent intent = new Intent(ShowTweetActivity.this,ShowUserActivity.class);
                    intent.putExtra("user",item.getUser());
                    startActivity(intent);
                });

                ((TweetImageTableView)findViewById(R.id.tweet_show_images)).setTwitterMediaEntities(item.getExtendedMediaEntities());
                ((TextView)findViewById(R.id.tweet_show_timestamp)).setText(DateUtils.formatDateTime(
                        ShowTweetActivity.this,item.getCreatedAt().getTime(),DateUtils.FORMAT_ABBREV_RELATIVE)
                );
                TextView viaText=(TextView)findViewById(R.id.tweet_show_via);
                viaText.setText(Html.fromHtml(item.getSource()));
                viaText.setMovementMethod(new LinkMovementMethod());
            }
        }.execute();

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}
