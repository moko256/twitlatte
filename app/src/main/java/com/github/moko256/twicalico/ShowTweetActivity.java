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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.moko256.twicalico.glideImageTarget.CircleImageTarget;
import com.github.moko256.twicalico.model.SendTweetModel;
import com.github.moko256.twicalico.text.TwitterStringUtils;
import com.github.moko256.twicalico.widget.TweetImageTableView;

import java.text.DateFormat;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

/**
 * Created by moko256 on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {

    CompositeSubscription subscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        subscriptions=new CompositeSubscription();

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        subscriptions.add(
                Single.create(
                        subscriber -> {
                            long statusId;
                            Status status;
                            statusId = getIntent().getLongExtra("statusId", -1);
                            if (statusId == -1) {
                                ShowTweetActivity.this.finish();
                                return;
                            }
                            status = GlobalApplication.statusCache.get(statusId);
                            if (status == null){
                                try {
                                    status = GlobalApplication.twitter.showStatus(statusId);
                                    GlobalApplication.statusCache.add(status);
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }
                            subscriber.onSuccess(status);
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result->{
                                    if (result == null) {
                                        finish();
                                        return;
                                    }
                                    Status item=(Status) result;

                                    RequestManager imageRequestManager= Glide.with(ShowTweetActivity.this);

                                    ImageView userImage= findViewById(R.id.tweet_show_image);

                                    imageRequestManager.load(item.getUser().getProfileImageURL()).asBitmap().into(new CircleImageTarget(userImage));

                                    TextView tweetIsReply = findViewById(R.id.tweet_show_is_reply_text);
                                    long replyTweetId = item.getInReplyToStatusId();
                                    if (replyTweetId != -1){
                                        tweetIsReply.setVisibility(View.VISIBLE);
                                        tweetIsReply.setOnClickListener(v -> startActivity(getIntent(this, replyTweetId)));
                                    } else {
                                        tweetIsReply.setVisibility(View.GONE);
                                    }

                                    ((TextView) findViewById(R.id.tweet_show_user_name)).setText(item.getUser().getName());
                                    ((TextView) findViewById(R.id.tweet_show_user_id)).setText(TwitterStringUtils.plusAtMark(item.getUser().getScreenName()));
                                    TextView contentText= findViewById(R.id.tweet_show_content);
                                    contentText.setText(TwitterStringUtils.getLinkedSequence(item,ShowTweetActivity.this));
                                    contentText.setMovementMethod(LinkMovementMethod.getInstance());

                                    userImage.setOnClickListener(v-> startActivity(ShowUserActivity.getIntent(this, item.getUser().getId())));

                                    RelativeLayout tweetQuoteTweetLayout= findViewById(R.id.tweet_show_quote_tweet);

                                    twitter4j.Status quotedStatus=item.getQuotedStatus();
                                    if(quotedStatus!=null){
                                        if (tweetQuoteTweetLayout.getVisibility() != View.VISIBLE) {
                                            tweetQuoteTweetLayout.setVisibility(View.VISIBLE);
                                        }
                                        tweetQuoteTweetLayout.setOnClickListener(v -> startActivity(getIntent(this, quotedStatus.getId())));
                                        ((TextView) findViewById(R.id.tweet_show_quote_tweet_user_name)).setText(quotedStatus.getUser().getName());
                                        ((TextView) findViewById(R.id.tweet_show_quote_tweet_user_id)).setText(TwitterStringUtils.plusAtMark(quotedStatus.getUser().getScreenName()));
                                        ((TextView) findViewById(R.id.tweet_show_quote_tweet_content)).setText(quotedStatus.getText());
                                    }else{
                                        if (tweetQuoteTweetLayout.getVisibility() != View.GONE) {
                                            tweetQuoteTweetLayout.setVisibility(View.GONE);
                                        }
                                    }

                                    MediaEntity mediaEntities[]=item.getMediaEntities();

                                    TweetImageTableView tableView= findViewById(R.id.tweet_show_images);
                                    if(mediaEntities.length!=0){
                                        tableView.setVisibility(View.VISIBLE);
                                        tableView.setMediaEntities(mediaEntities);
                                    }else{
                                        tableView.setVisibility(View.GONE);
                                    }

                                    ((TextView)findViewById(R.id.tweet_show_timestamp)).setText(
                                            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                                                    .format(item.getCreatedAt())
                                    );
                                    TextView viaText= findViewById(R.id.tweet_show_via);
                                    viaText.setText(Html.fromHtml("via:"+item.getSource()));
                                    viaText.setMovementMethod(new LinkMovementMethod());

                                    AppCompatEditText replyText= findViewById(R.id.tweet_show_tweet_reply_text);
                                    AppCompatButton replyButton= findViewById(R.id.tweet_show_tweet_reply_button);
                                    UserMentionEntity[] users = item.getUserMentionEntities();
                                    replyText.setText(TwitterStringUtils.convertToReplyTopString(item.getUser().getScreenName(), users));
                                    replyButton.setOnClickListener(v -> {
                                        replyButton.setEnabled(false);
                                        SendTweetModel model = new SendTweetModel(GlobalApplication.twitter, getContentResolver());
                                        model.setTweetText(replyText.getText().toString());
                                        model.setInReplyToStatusId(item.getId());
                                        subscriptions.add(
                                                model.postTweet()
                                                        .subscribe(
                                                                it -> {
                                                                    replyText.setText(TwitterStringUtils.convertToReplyTopString(item.getUser().getScreenName(), users));
                                                                    replyButton.setEnabled(true);
                                                                    Toast.makeText(ShowTweetActivity.this,R.string.succeeded,Toast.LENGTH_SHORT).show();
                                                                },
                                                                e->{
                                                                    e.printStackTrace();
                                                                    Toast.makeText(ShowTweetActivity.this,R.string.error_occurred,Toast.LENGTH_SHORT).show();
                                                                    replyButton.setEnabled(true);
                                                                }
                                                        )
                                        );
                                    });
                                },
                                e->{
                                    e.printStackTrace();
                                    finish();
                                })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
        subscriptions=null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    public static Intent getIntent(Context context, long statusId){
        return new Intent(context, ShowTweetActivity.class).putExtra("statusId", statusId);
    }

}
