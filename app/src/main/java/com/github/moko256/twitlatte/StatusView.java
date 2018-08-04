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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.Layout;
import android.text.TextUtils;
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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.mastodon.MTUser;
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap;
import com.github.moko256.twitlatte.database.CachedUsersSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.Emoji;
import com.github.moko256.twitlatte.entity.Type;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;
import com.github.moko256.twitlatte.widget.TweetImageTableView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/02/17.
 * 
 * @author moko256
 */

public class StatusView extends FrameLayout {
    private Status status;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final GlideRequests glideRequests;
    private EmojiToTextViewSetter contextEmojiSetter;
    private EmojiToTextViewSetter spoilerTextEmojiSetter;
    private EmojiToTextViewSetter userNameEmojiSetter;

    private final ImageView userImage;
    private final TextView retweetUserName;
    private final TextView retweetTimeStamp;
    private final TextView replyUserName;
    private final Space headerBottomMargin;
    private final TextView userName;
    private final TextView userId;
    private final TextView tweetSpoilerText;
    private final CheckBox contentOpenerToggle;
    private final TextView tweetContext;
    private final TextView timeStampText;
    private final RelativeLayout quoteTweetLayout;
    private final TweetImageTableView quoteTweetImages;
    private final TextView quoteTweetUserName;
    private final TextView quoteTweetUserId;
    private final TextView quoteTweetContext;
    private final TweetImageTableView imageTableView;
    private final CheckBox likeButton;
    private final CheckBox retweetButton;
    private final ImageButton replyButton;
    private final TextView likeCount;
    private final TextView retweetCount;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet,this);

        glideRequests = GlideApp.with(this);
        
        userImage = findViewById(R.id.tweet_icon);
        retweetUserName = findViewById(R.id.tweet_retweet_user_name);
        retweetTimeStamp = findViewById(R.id.tweet_retweet_time_stamp_text);
        replyUserName = findViewById(R.id.tweet_reply_user_name);
        headerBottomMargin = findViewById(R.id.tweet_header_bottom_margin);
        userId = findViewById(R.id.tweet_user_id);
        userName = findViewById(R.id.tweet_user_name);
        tweetSpoilerText = findViewById(R.id.tweet_spoiler);
        contentOpenerToggle = findViewById(R.id.tweet_spoiler_opener);
        tweetContext = findViewById(R.id.tweet_content);
        contentOpenerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tweetContext.setVisibility(VISIBLE);
            } else {
                tweetContext.setVisibility(GONE);
            }
        });
        tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        timeStampText = findViewById(R.id.tweet_time_stamp_text);
        quoteTweetLayout = findViewById(R.id.tweet_quote_tweet);
        quoteTweetImages = findViewById(R.id.tweet_quote_images);
        quoteTweetUserName = findViewById(R.id.tweet_quote_tweet_user_name);
        quoteTweetUserId = findViewById(R.id.tweet_quote_tweet_user_id);
        quoteTweetContext = findViewById(R.id.tweet_quote_tweet_content);
        imageTableView = findViewById(R.id.tweet_image_container);

        likeButton = findViewById(R.id.tweet_content_like_button);
        retweetButton = findViewById(R.id.tweet_content_retweet_button);
        replyButton = findViewById(R.id.tweet_content_reply_button);

        likeCount = findViewById(R.id.tweet_content_like_count);
        retweetCount = findViewById(R.id.tweet_content_retweet_count);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tweetContext.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            quoteTweetContext.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null) {
            disposable.clear();
            this.status = status;
            updateView();
        } else {
            if (!((Activity) getContext()).isDestroyed()) {
                glideRequests.clear(userImage);
                imageTableView.clearImages();
                disposable.clear();
            } else {
                disposable.dispose();
            }
        }
    }
    
    private void updateView(){
        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            if(retweetUserName.getVisibility() != View.VISIBLE){
                retweetUserName.setVisibility(View.VISIBLE);
            }
            retweetUserName.setText(getContext().getString(
                    TwitterStringUtils.getRepeatedByStringRes(GlobalApplication.clientType),
                    status.getUser().getName(),
                    TwitterStringUtils.plusAtMark(status.getUser().getScreenName())
            ));

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
                    TwitterStringUtils.plusAtMark(item.getInReplyToScreenName())
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

        String timelineImageLoadMode = GlobalApplication.preferenceRepository.getString(GlobalApplication.KEY_TIMELINE_IMAGE_LOAD_MODE, "normal");
        if (!timelineImageLoadMode.equals("none")){
            glideRequests
                    .load(
                            timelineImageLoadMode.equals("normal")?
                                    item.getUser().get400x400ProfileImageURLHttps():
                                    item.getUser().getMiniProfileImageURLHttps()
                    )
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userImage);
        } else {
            userImage.setImageResource(R.drawable.border_frame_round);
        }

        CharSequence userNameText = TwitterStringUtils.plusUserMarks(
                item.getUser().getName(),
                userName,
                item.getUser().isProtected(),
                item.getUser().isVerified()
        );
        userName.setText(userNameText);
        List<Emoji> userNameEmojis = null;
        if (item.getUser() instanceof CachedUsersSQLiteOpenHelper.CachedUser) {
            userNameEmojis = ((CachedUsersSQLiteOpenHelper.CachedUser) item.getUser()).getEmojis();
        } else if (item.getUser() instanceof MTUser) {
            List<com.sys1yagi.mastodon4j.api.entity.Emoji> emojis = ((MTUser) item.getUser()).account.getEmojis();
            userNameEmojis = new ArrayList<>(emojis.size());
            for (com.sys1yagi.mastodon4j.api.entity.Emoji emoji : emojis) {
                userNameEmojis.add(new Emoji(
                        emoji.getShortcode(),
                        emoji.getUrl()
                ));
            }
        }
        if (userNameEmojis != null) {
            if (userNameEmojiSetter == null) {
                userNameEmojiSetter = new EmojiToTextViewSetter(glideRequests, userName);
            }
            Disposable[] set = userNameEmojiSetter.set(userNameText, userNameEmojis);
            if (set != null) {
                disposable.addAll(set);
            }
        }
        userId.setText(TwitterStringUtils.plusAtMark(item.getUser().getScreenName()));

        tweetContext.setOnClickListener(v -> callOnClick());

        CharSequence linkedSequence = TwitterStringUtils.getLinkedSequence(getContext(), item);

        tweetContext.setText(linkedSequence);

        if (!TextUtils.isEmpty(linkedSequence)) {
            if (tweetContext.getVisibility() != VISIBLE){
                tweetContext.setVisibility(VISIBLE);
            }

            List<Emoji> statusEmojis = ((StatusCacheMap.CachedStatus) item).getEmojis();
            if (statusEmojis != null) {
                if (contextEmojiSetter == null) {
                    contextEmojiSetter = new EmojiToTextViewSetter(glideRequests, tweetContext);
                }
                Disposable[] set = contextEmojiSetter.set(linkedSequence, statusEmojis);
                if (set != null) {
                    disposable.addAll(set);
                }
            }
        } else {
            if (tweetContext.getVisibility() != GONE){
                tweetContext.setVisibility(GONE);
            }
        }

        String spoilerText = ((StatusCacheMap.CachedStatus) item).getSpoilerText();
        tweetSpoilerText.setText(spoilerText);
        if (spoilerText == null) {
            if (tweetSpoilerText.getVisibility() != GONE){
                tweetSpoilerText.setVisibility(GONE);
            }
            if (contentOpenerToggle.getVisibility() != GONE){
                contentOpenerToggle.setVisibility(GONE);
            }
        } else {
            contentOpenerToggle.setChecked(false);
            if (tweetContext.getVisibility() != GONE){
                tweetContext.setVisibility(GONE);
            }
            if (tweetSpoilerText.getVisibility() != VISIBLE){
                tweetSpoilerText.setVisibility(VISIBLE);
            }
            if (contentOpenerToggle.getVisibility() != VISIBLE){
                contentOpenerToggle.setVisibility(VISIBLE);
            }

            List<Emoji> statusEmojis = ((StatusCacheMap.CachedStatus) item).getEmojis();
            if (statusEmojis != null) {
                if (spoilerTextEmojiSetter == null) {
                    spoilerTextEmojiSetter = new EmojiToTextViewSetter(glideRequests, tweetSpoilerText);
                }
                Disposable[] set = spoilerTextEmojiSetter.set(spoilerText, statusEmojis);
                if (set != null) {
                    disposable.addAll(set);
                }
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
            quoteTweetUserName.setText(
                    TwitterStringUtils.plusUserMarks(
                            quotedStatus.getUser().getName(),
                            quoteTweetUserName,
                            quotedStatus.getUser().isProtected(),
                            quotedStatus.getUser().isVerified()
                    )
            );
            if (quotedStatus.getMediaEntities().length > 0) {
                if (quoteTweetImages.getVisibility() != View.VISIBLE) {
                    quoteTweetImages.setVisibility(View.VISIBLE);
                }
                quoteTweetImages.setMediaEntities(quotedStatus.getMediaEntities(), quotedStatus.isPossiblySensitive());
            } else {
                if (quoteTweetImages.getVisibility() != View.GONE) {
                    quoteTweetImages.setVisibility(View.GONE);
                }
            }
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
                                Status newStatus = GlobalApplication.twitter.createFavorite(item.getId());
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
                                    Status newStatus = GlobalApplication.statusCache.get(status.getId());

                                    if (newStatus != null && status.getId() == newStatus.getId()) {
                                        setStatus(newStatus);
                                    }

                                    Toast.makeText(
                                            getContext(),
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.LIKE
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
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
                                Status newStatus = GlobalApplication.twitter.destroyFavorite(item.getId());
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
                                    Status newStatus = GlobalApplication.statusCache.get(status.getId());

                                    if (newStatus != null && status.getId() == newStatus.getId()) {
                                        setStatus(newStatus);
                                    }

                                    Toast.makeText(
                                            getContext(),
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNLIKE
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
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
                                Status newStatus = GlobalApplication.twitter.retweetStatus(item.getId());
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
                                    Status newStatus = GlobalApplication.statusCache.get(status.getId());

                                    if (newStatus != null && status.getId() == newStatus.getId()) {
                                        setStatus(newStatus);
                                    }

                                    Toast.makeText(
                                            getContext(),
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.REPEAT
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
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
                                Status newStatus = GlobalApplication.twitter.unRetweetStatus(item.getId());
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
                                    Status newStatus = GlobalApplication.statusCache.get(status.getId());

                                    if (newStatus != null && status.getId() == newStatus.getId()) {
                                        setStatus(newStatus);
                                    }

                                    Toast.makeText(
                                            getContext(),
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNREPEAT
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
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
        retweetButton.setOnLongClickListener(v -> {
            getContext().startActivity(
                    PostActivity.getIntent(getContext(), ((StatusCacheMap.CachedStatus) item).getRemoteUrl() + " ")
            );
            return true;
        });

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
