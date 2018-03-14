/*
 * Copyright 2018 The twicalico authors
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.twicalico.cacheMap.StatusCacheMap;
import com.github.moko256.twicalico.intent.AppCustomTabsKt;
import com.github.moko256.twicalico.model.base.PostTweetModel;
import com.github.moko256.twicalico.model.impl.PostTweetModelCreator;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.text.DateFormat;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by moko256 on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {

    CompositeSubscription subscriptions;
    long statusId;

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

        statusId = getIntent().getLongExtra("statusId", -1);
        if (statusId == -1) {
            ShowTweetActivity.this.finish();
            return;
        }
        Status status = GlobalApplication.statusCache.get(statusId);
        if (status == null){
            subscriptions.add(
                    updateStatus()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result->{
                                        if (result == null) {
                                            finish();
                                            return;
                                        }
                                        updateView(result);
                                    },
                                    e->{
                                        e.printStackTrace();
                                        finish();
                                    })
            );
        } else {
            updateView(status);
        }

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.tweet_show_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(() -> subscriptions.add(
                updateStatus()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result == null) {
                                        finish();
                                        return;
                                    }
                                    updateView(result);
                                    swipeRefreshLayout.setRefreshing(false);
                                },
                                e->{
                                    e.printStackTrace();
                                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                })
        ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
        subscriptions=null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_tweet_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getShareUrl() {
        return ((StatusCacheMap.CachedStatus) GlobalApplication.statusCache.get(statusId)).getRemoteUrl();
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                startActivity(Intent.createChooser(
                        new Intent()
                                .setAction(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, getShareUrl()),
                        getString(R.string.share)));
                break;
            case R.id.action_open_in_browser:
                AppCustomTabsKt.launchChromeCustomTabs(this, getShareUrl());
                break;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    public static Intent getIntent(Context context, long statusId){
        return new Intent(context, ShowTweetActivity.class).putExtra("statusId", statusId);
    }

    private Single<Status> updateStatus(){
        return Single.create(
                subscriber -> {
                    try {
                        Status status = GlobalApplication.twitter.showStatus(statusId);
                        GlobalApplication.statusCache.add(status);
                        subscriber.onSuccess(status);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                });
    }

    private void updateView(Status item){
        TextView tweetIsReply = findViewById(R.id.tweet_show_is_reply_text);
        long replyTweetId = item.getInReplyToStatusId();
        if (replyTweetId != -1){
            tweetIsReply.setVisibility(VISIBLE);
            tweetIsReply.setOnClickListener(v -> startActivity(getIntent(this, replyTweetId)));
        } else {
            tweetIsReply.setVisibility(GONE);
        }

        StatusView statusView = new StatusView(this);
        statusView.setStatus(item);
        ViewGroup cview = (ViewGroup) statusView.getChildAt(0);
        ViewGroup sview = (ViewGroup) cview.getChildAt(0);
        cview.removeView(sview);
        FrameLayout statusViewFrame = findViewById(R.id.tweet_show_tweet);
        statusViewFrame.removeAllViews();
        statusViewFrame.addView(sview);

        ((TextView)findViewById(R.id.tweet_show_timestamp)).setText(
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                        .format(item.getCreatedAt())
        );
        TextView viaText= findViewById(R.id.tweet_show_via);
        viaText.setText(TwitterStringUtils.convertUrlSpanToCustomTabs(Html.fromHtml("via:"+item.getSource()), this));
        viaText.setMovementMethod(new LinkMovementMethod());

        AppCompatEditText replyText= findViewById(R.id.tweet_show_tweet_reply_text);
        AppCompatButton replyButton= findViewById(R.id.tweet_show_tweet_reply_button);
        UserMentionEntity[] users = item.getUserMentionEntities();
        replyText.setText(TwitterStringUtils.convertToReplyTopString(
                GlobalApplication.userCache.get(GlobalApplication.userId).getScreenName(),
                item.getUser().getScreenName(),
                users
        ));
        replyButton.setOnClickListener(v -> {
            replyButton.setEnabled(false);
            PostTweetModel model = PostTweetModelCreator.getInstance(GlobalApplication.twitter, getContentResolver());
            model.setTweetText(replyText.getText().toString());
            model.setInReplyToStatusId(item.getId());
            subscriptions.add(
                    model.postTweet()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    it -> {
                                        replyText.setText(TwitterStringUtils.convertToReplyTopString(
                                                GlobalApplication.userCache.get(GlobalApplication.userId).getScreenName(),
                                                item.getUser().getScreenName(), users
                                        ));
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
    }

}
