/*
 * Copyright 2015-2018 The twicalico authors
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

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.twicalico.entity.Type;
import com.github.moko256.twicalico.text.TwitterStringUtils;
import com.github.moko256.twicalico.widget.TweetImageTableView;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/02/17.
 * 
 * @author moko256
 */

public class StatusView extends FrameLayout {
    Status status;

    GlideRequests glideRequests;

    ImageView userImage;
    TextView retweetUserName;
    TextView retweetTimeStamp;
    TextView replyUserName;
    Space headerBottomMargin;
    TextView userName;
    TextView userId;
    TextView tweetContext;
    TextView timeStampText;
    RelativeLayout quoteTweetLayout;
    TextView quoteTweetUserName;
    TextView quoteTweetUserId;
    TextView quoteTweetContext;
    TweetImageTableView imageTableView;
    CheckBox likeButton;
    CheckBox retweetButton;
    ImageButton replyButton;
    TextView likeCount;
    TextView retweetCount;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet,this);

        glideRequests = GlideApp.with(context);
        
        userImage = findViewById(R.id.tweet_icon);
        retweetUserName = findViewById(R.id.tweet_retweet_user_name);
        retweetTimeStamp = findViewById(R.id.tweet_retweet_time_stamp_text);
        replyUserName = findViewById(R.id.tweet_reply_user_name);
        headerBottomMargin = findViewById(R.id.tweet_header_bottom_margin);
        userId = findViewById(R.id.tweet_user_id);
        userName = findViewById(R.id.tweet_user_name);
        tweetContext= findViewById(R.id.tweet_content);
        tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        timeStampText = findViewById(R.id.tweet_time_stamp_text);
        quoteTweetLayout = findViewById(R.id.tweet_quote_tweet);
        quoteTweetUserName = findViewById(R.id.tweet_quote_tweet_user_name);
        quoteTweetUserId = findViewById(R.id.tweet_quote_tweet_user_id);
        quoteTweetContext = findViewById(R.id.tweet_quote_tweet_content);
        imageTableView = findViewById(R.id.tweet_image_container);

        likeButton = findViewById(R.id.tweet_content_like_button);
        retweetButton = findViewById(R.id.tweet_content_retweet_button);
        replyButton = findViewById(R.id.tweet_content_reply_button);

        likeCount = findViewById(R.id.tweet_content_like_count);
        retweetCount = findViewById(R.id.tweet_content_retweet_count);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
            updateView();
        } else {
            if (!((Activity) getContext()).isDestroyed()) {
                glideRequests.clear(userImage);
                imageTableView.clearImages();
            }
        }
    }
    
    private void updateView(){
        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            if(retweetUserName.getVisibility() != View.VISIBLE){
                retweetUserName.setVisibility(View.VISIBLE);
            }
            retweetUserName.setText(getContext().getString(R.string.repeat_by,status.getUser().getName()));

            if(retweetTimeStamp.getVisibility() != View.VISIBLE){
                retweetTimeStamp.setVisibility(View.VISIBLE);
            }
            retweetTimeStamp.setText(DateUtils.getRelativeTimeSpanString(
                    status.getCreatedAt().getTime(),
                    System.currentTimeMillis(),
                    0
            ));
        } else {
            if(retweetUserName.getVisibility() != View.GONE){
                retweetUserName.setVisibility(View.GONE);
            }

            if(retweetTimeStamp.getVisibility() != View.GONE){
                retweetTimeStamp.setVisibility(View.GONE);
            }
        }

        boolean isReply = item.getInReplyToScreenName()!=null;
        if (isReply){
            if (replyUserName.getVisibility()!= View.VISIBLE){
                replyUserName.setVisibility(View.VISIBLE);
            }

            replyUserName.setText(getContext().getString(
                    (item.getInReplyToStatusId() != -1)?
                            R.string.reply_to:
                            R.string.mention_to,
                    item.getInReplyToScreenName()
            ));
        }
        else{
            if(replyUserName.getVisibility()!=View.GONE){
                replyUserName.setVisibility(View.GONE);
            }
        }

        if (status.isRetweet()||isReply){
            if (headerBottomMargin.getVisibility()!=VISIBLE){
                headerBottomMargin.setVisibility(VISIBLE);
            }
        } else {
            if (headerBottomMargin.getVisibility()!=GONE){
                headerBottomMargin.setVisibility(GONE);
            }
        }

        if (GlobalApplication.configuration.isTimelineImageLoad){
            glideRequests.load(item.getUser().get400x400ProfileImageURLHttps()).circleCrop().into(userImage);
        } else {
            userImage.setImageResource(R.drawable.border_frame_round);
        }

        TwitterStringUtils.plusAndSetMarks(
                item.getUser().getName(),
                userName,
                item.getUser().isProtected(),
                item.getUser().isVerified()
        );
        userId.setText(TwitterStringUtils.plusAtMark(item.getUser().getScreenName()));

        tweetContext.setOnClickListener(v -> callOnClick());

        TwitterStringUtils.setLinkedSequenceTo(item, tweetContext);
        if (!tweetContext.getText().toString().trim().isEmpty()) {
            if (tweetContext.getVisibility() != VISIBLE){
                tweetContext.setVisibility(VISIBLE);
            }
        } else {
            if (tweetContext.getVisibility() != GONE){
                tweetContext.setVisibility(GONE);
            }
        }

        timeStampText.setText(DateUtils.getRelativeTimeSpanString(
                item.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                0
        ));
        userImage.setOnClickListener(v -> {
            ActivityOptionsCompat animation = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            ((Activity) getContext()),
                            v,
                            "icon_image"
                    );
            getContext().startActivity(
                    ShowUserActivity.getIntent(getContext(), item.getUser().getId()),
                    animation.toBundle()
            );
        });
        setOnClickListener(v -> {
            ActivityOptionsCompat animation = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            ((Activity) getContext()),
                            userImage,
                            "icon_image"
                    );
            getContext().startActivity(
                    ShowTweetActivity.getIntent(getContext(), item.getId()),
                    animation.toBundle()
            );
        });

        Status quotedStatus=item.getQuotedStatus();
        if(quotedStatus!=null){
            if (quoteTweetLayout.getVisibility() != View.VISIBLE) {
                quoteTweetLayout.setVisibility(View.VISIBLE);
            }
            quoteTweetLayout.setOnClickListener(v -> getContext().startActivity(
                    ShowTweetActivity.getIntent(getContext(), quotedStatus.getId())
            ));
            TwitterStringUtils.plusAndSetMarks(
                    quotedStatus.getUser().getName(),
                    quoteTweetUserName,
                    quotedStatus.getUser().isProtected(),
                    quotedStatus.getUser().isVerified()
            );
            quoteTweetUserId.setText(TwitterStringUtils.plusAtMark(quotedStatus.getUser().getScreenName()));
            quoteTweetContext.setText(quotedStatus.getText());
        }else{
            if (quoteTweetLayout.getVisibility() != View.GONE) {
                quoteTweetLayout.setVisibility(View.GONE);
            }
        }

        MediaEntity mediaEntities[]=item.getMediaEntities();

        if (mediaEntities.length!=0){
            imageTableView.setVisibility(View.VISIBLE);
            imageTableView.setMediaEntities(mediaEntities, item.isPossiblySensitive());
        }
        else{
            imageTableView.setVisibility(View.GONE);
        }

        likeButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && (!item.isFavorited())) {
                Single
                        .create(subscriber -> {
                            try {
                                subscriber.onSuccess(GlobalApplication.twitter.createFavorite(item.getId()));
                            } catch (TwitterException e) {
                                subscriber.onError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Status status = (Status) result;
                                    GlobalApplication.statusCache.add(status);
                                    setStatus(GlobalApplication.statusCache.get(this.status.getId()));
                                    Toast.makeText(getContext(), R.string.did_like, Toast.LENGTH_SHORT).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                }
                        );
            } else if ((!b) && item.isFavorited()) {
                Single
                        .create(subscriber -> {
                            try {
                                subscriber.onSuccess(GlobalApplication.twitter.destroyFavorite(item.getId()));
                            } catch (TwitterException e) {
                                subscriber.onError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Status status = (Status) result;
                                    GlobalApplication.statusCache.add(status);
                                    setStatus(GlobalApplication.statusCache.get(this.status.getId()));
                                    Toast.makeText(getContext(), R.string.did_unlike, Toast.LENGTH_SHORT).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                }
                        );
            }
        });
        likeButton.setChecked(item.isFavorited());

        retweetButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && (!item.isRetweeted())) {
                Single
                        .create(subscriber -> {
                            try {
                                subscriber.onSuccess(GlobalApplication.twitter.retweetStatus(item.getId()));
                            } catch (TwitterException e) {
                                subscriber.onError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Status status = (Status) result;
                                    GlobalApplication.statusCache.add(status);
                                    setStatus(GlobalApplication.statusCache.get(this.status.getId()));
                                    Toast.makeText(getContext(), R.string.did_repeat, Toast.LENGTH_SHORT).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                }
                        );
            } else if ((!b) && item.isRetweeted()) {
                Single
                        .create(subscriber -> {
                            try {
                                subscriber.onSuccess(GlobalApplication.twitter.unRetweetStatus(item.getId()));
                            } catch (TwitterException e) {
                                subscriber.onError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Status status = (Status) result;
                                    GlobalApplication.statusCache.add(status);
                                    setStatus(GlobalApplication.statusCache.get(this.status.getId()));
                                    Toast.makeText(getContext(), R.string.did_unrepeat, Toast.LENGTH_SHORT).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                                }
                        );
            }
        });
        retweetButton.setChecked(item.isRetweeted());
        retweetButton.setEnabled(GlobalApplication.clientType == Type.MASTODON || !(item.getUser().isProtected()) || item.getUser().getId() == GlobalApplication.userId);

        replyButton.setOnClickListener(
                v -> getContext().startActivity(PostActivity.getIntent(
                        getContext(),
                        item.getId(),
                        TwitterStringUtils.convertToReplyTopString(
                                GlobalApplication.userCache.get(GlobalApplication.userId).getScreenName(),
                                item.getUser().getScreenName(),
                                item.getUserMentionEntities()
                        ).toString()
                ))
        );

        likeCount.setText((item.getFavoriteCount() != 0)? TwitterStringUtils.convertToSIUnitString(item.getFavoriteCount()): "");
        retweetCount.setText((item.getRetweetCount() != 0)? TwitterStringUtils.convertToSIUnitString(item.getRetweetCount()): "");
    }
}
