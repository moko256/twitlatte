/*
 * Copyright 2015-2018 The twitlatte authors
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.twitlatte.cacheMap.StatusCacheMap;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.base.PostTweetModel;
import com.github.moko256.twitlatte.model.impl.PostTweetModelCreator;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.text.link.MTHtmlParser;

import java.text.DateFormat;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by moko256 on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {

    private CompositeDisposable disposables;
    private long statusId;

    private StatusView statusView;

    private TextView tweetIsReply;
    private FrameLayout statusViewFrame;
    private TextView timestampText;
    private TextView viaText;
    private EditText replyText;
    private Button replyButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        statusId = getIntent().getLongExtra("statusId", -1);
        if (statusId == -1) {
            finish();
        } else {
            disposables=new CompositeDisposable();

            ActionBar actionBar=getSupportActionBar();
            if (actionBar!=null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
            }

            tweetIsReply = findViewById(R.id.tweet_show_is_reply_text);
            statusViewFrame = findViewById(R.id.tweet_show_tweet);
            timestampText = findViewById(R.id.tweet_show_timestamp);
            viaText = findViewById(R.id.tweet_show_via);
            replyText= findViewById(R.id.tweet_show_tweet_reply_text);
            replyButton= findViewById(R.id.tweet_show_tweet_reply_button);

            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.tweet_show_swipe_refresh);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
            swipeRefreshLayout.setOnRefreshListener(() -> disposables.add(
                    updateStatus()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        if (result == null) {
                                            finish();
                                        } else {
                                            updateView(result);
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    },
                                    e->{
                                        e.printStackTrace();
                                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                        swipeRefreshLayout.setRefreshing(false);
                                    })
            ));

            Status status = GlobalApplication.statusCache.get(statusId);
            if (status == null){
                disposables.add(
                        updateStatus()
                                .subscribeOn(Schedulers.io())
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
        }
    }

    @Override
    protected void onDestroy() {
        disposables.dispose();
        super.onDestroy();
        disposables=null;
        if (statusView != null){
            statusView.setStatus(null);
        }
        statusView=null;
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
            case R.id.action_quote:
                startActivity(PostActivity.getIntent(
                        this,
                        getShareUrl() + " "
                ));
                break;
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
        return true;
    }

    public static Intent getIntent(Context context, long statusId){
        return new Intent(context, ShowTweetActivity.class).putExtra("statusId", statusId);
    }

    private Single<Status> updateStatus(){
        return Single.create(
                subscriber -> {
                    try {
                        Status status = GlobalApplication.twitter.showStatus(statusId);
                        GlobalApplication.statusCache.add(status, false);
                        subscriber.onSuccess(GlobalApplication.statusCache.get(statusId));
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                });
    }

    private void updateView(Status arg){
        Status item = arg.isRetweet()? arg.getRetweetedStatus(): arg;

        long replyTweetId = item.getInReplyToStatusId();
        if (replyTweetId != -1){
            tweetIsReply.setVisibility(VISIBLE);
            tweetIsReply.setOnClickListener(v -> startActivity(getIntent(this, replyTweetId)));
        } else {
            tweetIsReply.setVisibility(GONE);
        }

        statusView = new StatusView(this);
        statusView.setStatus(item);
        statusView.setOnClickListener(null);
        ViewGroup cview = (ViewGroup) statusView.getChildAt(0);
        ViewGroup sview = (ViewGroup) cview.getChildAt(0);
        cview.removeView(sview);
        statusViewFrame.removeAllViews();
        statusViewFrame.addView(sview);

        timestampText.setText(
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                        .format(item.getCreatedAt())
        );

        viaText.setText(MTHtmlParser.INSTANCE.convertToEntities("via:"+item.getSource(), TwitterStringUtils.linkParserListener(this)));
        viaText.setMovementMethod(new LinkMovementMethod());

        resetReplyText(item);

        replyButton.setOnClickListener(v -> {
            replyButton.setEnabled(false);
            PostTweetModel model = PostTweetModelCreator.getInstance(GlobalApplication.twitter, getContentResolver());
            model.setTweetText(replyText.getText().toString());
            model.setInReplyToStatusId(item.getId());
            disposables.add(
                    model.postTweet()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        resetReplyText(item);
                                        replyButton.setEnabled(true);
                                        Toast.makeText(ShowTweetActivity.this,R.string.did_post,Toast.LENGTH_SHORT).show();
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

    private void resetReplyText(Status status){
        User user = GlobalApplication.userCache.get(GlobalApplication.userId);

        replyText.setText(TwitterStringUtils.convertToReplyTopString(
                user != null ? user.getScreenName() : GlobalApplication.accessToken.getScreenName(),
                status.getUser().getScreenName(),
                status.getUserMentionEntities()
        ));
    }

}
