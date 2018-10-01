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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.twitlatte.entity.Status;
import com.github.moko256.twitlatte.entity.User;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.base.PostTweetModel;
import com.github.moko256.twitlatte.model.impl.PostTweetModelCreator;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.text.DateFormat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import twitter4j.TwitterException;

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

    private StatusViewBinder statusViewBinder;

    private TextView tweetIsReply;
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
            ViewGroup statusViewFrame = findViewById(R.id.tweet_show_tweet);
            statusViewBinder = new StatusViewBinder(GlideApp.with(this), statusViewFrame);
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

            prepareStatus();
        }
    }

    @Override
    protected void onDestroy() {
        disposables.dispose();
        super.onDestroy();
        disposables=null;
        if (statusViewBinder != null){
            statusViewBinder.clear();
        }
        statusViewBinder=null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_tweet_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getShareUrl() {
        return ((com.github.moko256.twitlatte.entity.Status) GlobalApplication.statusCache.get(statusId)).getUrl();
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

    private void prepareStatus() {
        Status status = (Status) GlobalApplication.statusCache.get(statusId);

        if (status != null) {
            User user = GlobalApplication.userCache.get(status.getUserId());

            Status quotedStatus = status.getQuotedStatusId() != -1
                    ?(Status) GlobalApplication.statusCache.get(status.getQuotedStatusId())
                    :null;
            User quotedStatusUser = quotedStatus != null
                    ?GlobalApplication.userCache.get(quotedStatus.getUserId())
                    :null;

            updateView(new Result(user, status, quotedStatusUser, quotedStatus));
        } else {
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
        }
    }

    private Single<Result> updateStatus(){
        return Single.create(
                subscriber -> {
                    try {
                        twitter4j.Status result = GlobalApplication.twitter.showStatus(statusId);
                        GlobalApplication.statusCache.add(result, false);
                        Status status = (Status) GlobalApplication.statusCache.get(statusId);

                        User user = GlobalApplication.userCache.get(status.getUserId());

                        Status quotedStatus = status.getQuotedStatusId() != -1
                                ?(Status) GlobalApplication.statusCache.get(status.getQuotedStatusId())
                                :null;
                        User quotedStatusUser = quotedStatus != null
                                ?GlobalApplication.userCache.get(quotedStatus.getUserId())
                                :null;

                        subscriber.onSuccess(new Result(user, status, quotedStatusUser, quotedStatus));
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void updateView(Result item){
        long replyTweetId = item.status.getInReplyToStatusId();
        if (replyTweetId != -1){
            tweetIsReply.setVisibility(VISIBLE);
            tweetIsReply.setOnClickListener(v -> startActivity(getIntent(this, replyTweetId)));
        } else {
            tweetIsReply.setVisibility(GONE);
        }

        statusViewBinder.getUserImage().setOnClickListener(v -> {
            ActivityOptionsCompat animation = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            this,
                            v,
                            "icon_image"
                    );
            startActivity(
                    ShowUserActivity.getIntent(this, item.user.getId()),
                    animation.toBundle()
            );
        });

        statusViewBinder.getQuoteTweetLayout().setOnClickListener(v -> startActivity(
                ShowTweetActivity.getIntent(this, item.quotedStatus.getId())
        ));

        statusViewBinder.getLikeButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && (!item.status.isFavorited())) {
                Single
                        .create(subscriber -> {
                            try {
                                twitter4j.Status newStatus = GlobalApplication.twitter.createFavorite(item.status.getId());
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (statusViewBinder != null) {
                                        prepareStatus();
                                    }

                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.LIKE
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.convertErrorToText(throwable),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );
            } else if ((!b) && item.status.isFavorited()) {
                Single
                        .create(subscriber -> {
                            try {
                                twitter4j.Status newStatus = GlobalApplication.twitter.destroyFavorite(item.status.getId());
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (statusViewBinder != null) {
                                        prepareStatus();
                                    }

                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNLIKE
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.convertErrorToText(throwable),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );
            }
        });

        statusViewBinder.getRetweetButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && (!item.status.isRepeated())) {
                Single
                        .create(subscriber -> {
                            try {
                                twitter4j.Status newStatus = GlobalApplication.twitter.retweetStatus(item.status.getId());
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (statusViewBinder != null) {
                                        prepareStatus();
                                    }

                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.REPEAT
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.convertErrorToText(throwable),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );
            } else if ((!b) && item.status.isRepeated()) {
                Single
                        .create(subscriber -> {
                            try {
                                twitter4j.Status newStatus = GlobalApplication.twitter.unRetweetStatus(item.status.getId());
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (statusViewBinder != null) {
                                        prepareStatus();
                                    }

                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNREPEAT
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(
                                            this,
                                            TwitterStringUtils.convertErrorToText(throwable),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );
            }
        });

        statusViewBinder.getReplyButton().setOnClickListener(
                v -> startActivity(PostActivity.getIntent(
                        this,
                        item.status.getId(),
                        TwitterStringUtils.convertToReplyTopString(
                                GlobalApplication.userCache.get(GlobalApplication.userId).getScreenName(),
                                item.user.getScreenName(),
                                item.status.getMentions()
                        ).toString()
                ))
        );

        statusViewBinder.setStatus(item.user, item.status, item.quotedStatusUser, item.quotedStatus);

        timestampText.setText(
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                        .format(item.status.getCreatedAt())
        );

        viaText.setText(TwitterStringUtils.appendLinkAtViaText(this, item.status.getSourceName(), item.status.getSourceWebsite()));
        viaText.setMovementMethod(new LinkMovementMethod());

        resetReplyText(item.user, item.status);

        replyButton.setOnClickListener(v -> {
            replyButton.setEnabled(false);
            PostTweetModel model = PostTweetModelCreator.getInstance(GlobalApplication.twitter, getContentResolver());
            model.setTweetText(replyText.getText().toString());
            model.setInReplyToStatusId(item.status.getId());
            disposables.add(
                    model.postTweet()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        resetReplyText(item.user, item.status);
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

    private void resetReplyText(User postedUser, Status status){
        User user = GlobalApplication.userCache.get(GlobalApplication.userId);

        replyText.setText(TwitterStringUtils.convertToReplyTopString(
                user != null ? user.getScreenName() : GlobalApplication.accessToken.getScreenName(),
                postedUser.getScreenName(),
                status.getMentions()
        ));
    }

    private class Result {
        final User user;
        final Status status;
        final User quotedStatusUser;
        final Status quotedStatus;

        Result(User user, Status status, User quotedStatusUser, Status quotedStatus) {
            this.user = user;
            this.status = status;
            this.quotedStatusUser = quotedStatusUser;
            this.quotedStatus = quotedStatus;
        }
    }
}
