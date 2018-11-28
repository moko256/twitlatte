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
import android.content.ClipData;
import android.content.ClipboardManager;
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

import com.github.moko256.twitlatte.converter.StatusConverterKt;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.entity.Post;
import com.github.moko256.twitlatte.entity.Status;
import com.github.moko256.twitlatte.entity.User;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.base.PostTweetModel;
import com.github.moko256.twitlatte.model.impl.PostTweetModelCreatorKt;
import com.github.moko256.twitlatte.model.impl.StatusActionModelImpl;
import com.github.moko256.twitlatte.repository.server.impl.TwitterStatusActionRepositoryImpl;
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
import kotlin.Unit;
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

    private CompositeDisposable disposables = new CompositeDisposable();
    private StatusActionModelImpl statusActionModel;
    private long statusId;
    private String shareUrl = "";

    private StatusViewBinder statusViewBinder;

    private TextView tweetIsReply;
    private TextView timestampText;
    private TextView viaText;
    private EditText replyText;
    private Button replyButton;

    private Client client;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        statusId = getIntent().getLongExtra("statusId", -1);

        client = GlobalApplication.getClient(this);
        statusActionModel = new StatusActionModelImpl(
                new TwitterStatusActionRepositoryImpl(client.getTwitter()),
                GlobalApplication.statusActionQueue,
                client.getStatusCache()
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        tweetIsReply = findViewById(R.id.tweet_show_is_reply_text);
        ViewGroup statusViewFrame = findViewById(R.id.tweet_show_tweet);
        statusViewBinder = new StatusViewBinder(client.getAccessToken(), GlideApp.with(this), statusViewFrame);
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

        disposables.addAll(

                statusActionModel.getUpdateObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            prepareStatus();

                            Toast.makeText(
                                    this,
                                    TwitterStringUtils.getDidActionStringRes(
                                            client.getAccessToken().getType(),
                                            it.getSecond()
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }),

                statusActionModel.getErrorObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            prepareStatus();

                            it.getSecond().printStackTrace();
                            Toast.makeText(
                                    this,
                                    TwitterStringUtils.convertErrorToText(it.getSecond()),
                                    Toast.LENGTH_LONG
                            ).show();
                        })

        );

        prepareStatus();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_quote:
                startActivity(PostActivity.getIntent(
                        this,
                        shareUrl + " "
                ));
                break;
            case R.id.action_share:
                startActivity(Intent.createChooser(
                        new Intent()
                                .setAction(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, shareUrl),
                        getString(R.string.share)));
                break;
            case R.id.action_open_in_browser:
                AppCustomTabsKt.launchChromeCustomTabs(this, shareUrl);
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
        Post status = client.getPostCache().getPost(statusId);

        if (status != null) {
            updateView(status);
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

    private Single<Post> updateStatus(){
        return Single.create(
                subscriber -> {
                    try {
                        twitter4j.Status result = client.getTwitter().showStatus(statusId);
                        client.getStatusCache().add(StatusConverterKt.convertToPost(result), false);
                        subscriber.onSuccess(client.getPostCache().getPost(statusId));
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                });
    }

    private void updateView(Post item){
        shareUrl = item.getStatus().getUrl();
        long replyTweetId = item.getStatus().getInReplyToStatusId();
        if (replyTweetId != -1){
            tweetIsReply.setVisibility(VISIBLE);
            tweetIsReply.setOnClickListener(v -> startActivity(getIntent(this, replyTweetId)));
        } else {
            tweetIsReply.setVisibility(GONE);
        }

        statusViewBinder.getTweetSpoilerText().setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.did_copy, Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("spoiler_text", item.getStatus().getSpoilerText()));
            return true;
        });

        statusViewBinder.getTweetContext().setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.did_copy, Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("post_context", item.getStatus().getText()));
            return true;
        });

        statusViewBinder.getUserImage().setOnClickListener(v -> {
            ActivityOptionsCompat animation = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            this,
                            v,
                            "icon_image"
                    );
            startActivity(
                    ShowUserActivity.getIntent(this, item.getUser().getId()),
                    animation.toBundle()
            );
        });

        statusViewBinder.getQuoteTweetLayout().setOnClickListener(v -> startActivity(
                ShowTweetActivity.getIntent(this, item.getQuotedRepeatingStatus().getId())
        ));

        statusViewBinder.getLikeButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                statusActionModel.createFavorite(item.getId());
            } else {
                statusActionModel.removeFavorite(item.getId());
            }
            return Unit.INSTANCE;
        });

        statusViewBinder.getRepeatButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                statusActionModel.createRepeat(item.getId());
            } else {
                statusActionModel.removeRepeat(item.getId());
            }
            return Unit.INSTANCE;
        });

        statusViewBinder.getReplyButton().setOnClickListener(
                v -> startActivity(PostActivity.getIntent(
                        this,
                        item.getStatus().getId(),
                        TwitterStringUtils.convertToReplyTopString(
                                client.getUserCache().get(client.getAccessToken().getUserId()).getScreenName(),
                                item.getUser().getScreenName(),
                                item.getStatus().getMentions()
                        ).toString()
                ))
        );

        statusViewBinder.setStatus(
                item.getRepeatedUser(),
                item.getRepeat(),
                item.getUser(),
                item.getStatus(),
                item.getQuotedRepeatingUser(),
                item.getQuotedRepeatingStatus()
        );

        timestampText.setText(
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                        .format(item.getStatus().getCreatedAt())
        );

        if (item.getStatus().getSourceName() != null) {
            viaText.setText(TwitterStringUtils.appendLinkAtViaText(this, item.getStatus().getSourceName(), item.getStatus().getSourceWebsite()));
            viaText.setMovementMethod(new LinkMovementMethod());
        } else {
            viaText.setVisibility(GONE);
        }

        resetReplyText(item.getUser(), item.getStatus());

        replyButton.setOnClickListener(v -> {
            replyButton.setEnabled(false);
            PostTweetModel model = PostTweetModelCreatorKt.getInstance(client, getContentResolver());
            model.setTweetText(replyText.getText().toString());
            model.setInReplyToStatusId(item.getStatus().getId());
            disposables.add(
                    model.postTweet()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        resetReplyText(item.getUser(), item.getStatus());
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
        User user = client.getUserCache().get(client.getAccessToken().getUserId());

        replyText.setText(TwitterStringUtils.convertToReplyTopString(
                user != null ? user.getScreenName() : client.getAccessToken().getScreenName(),
                postedUser.getScreenName(),
                status.getMentions()
        ));
    }
}