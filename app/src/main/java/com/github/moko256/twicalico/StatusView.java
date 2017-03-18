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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.util.TimeSpanConverter;

/**
 * Created by moko256 on 2017/02/17.
 * 
 * @author moko256
 */

public class StatusView extends RelativeLayout {
    Status status;

    RequestManager imageRequestManager;

    ImageView tweetUserImage;
    TextView tweetRetweetUserName;
    TextView tweetReplyUserName;
    Space tweetHeaderBottomMargin;
    TextView tweetUserName;
    TextView tweetUserId;
    TextView tweetContext;
    TextView tweetViaText;
    TextView tweetTimeStampText;
    RelativeLayout tweetQuoteTweetLayout;
    TextView tweetQuoteTweetUserName;
    TextView tweetQuoteTweetUserId;
    TextView tweetQuoteTweetContext;
    TweetImageTableView tweetImageTableView;
    AppCompatCheckBox tweetLikeButton;
    AppCompatCheckBox tweetRetweetButton;
    AppCompatImageButton tweetReplyButton;

    TimeSpanConverter timeSpanConverter;
    
    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet,this);

        imageRequestManager= Glide.with(context);
        
        tweetUserImage=(ImageView) findViewById(R.id.tweet_icon);
        tweetRetweetUserName=(TextView) findViewById(R.id.tweet_retweet_user_name);
        tweetReplyUserName=(TextView) findViewById(R.id.tweet_reply_user_name);
        tweetHeaderBottomMargin=(Space) findViewById(R.id.tweet_header_bottom_margin);
        tweetUserId=(TextView) findViewById(R.id.tweet_user_id);
        tweetUserName=(TextView) findViewById(R.id.tweet_user_name);
        tweetContext=(TextView) findViewById(R.id.tweet_content);
        tweetViaText=(TextView) findViewById(R.id.tweet_via_text);
        tweetTimeStampText=(TextView) findViewById(R.id.tweet_time_stamp_text);
        tweetQuoteTweetLayout=(RelativeLayout)  findViewById(R.id.tweet_quote_tweet);
        tweetQuoteTweetUserName=(TextView) findViewById(R.id.tweet_quote_tweet_user_name);
        tweetQuoteTweetUserId=(TextView) findViewById(R.id.tweet_quote_tweet_user_id);
        tweetQuoteTweetContext=(TextView) findViewById(R.id.tweet_quote_tweet_content);
        tweetImageTableView=(TweetImageTableView) findViewById(R.id.tweet_image_container);

        tweetLikeButton=(AppCompatCheckBox) findViewById(R.id.tweet_content_like_button);
        tweetLikeButton.setButtonDrawable(R.drawable.ic_heart_black_24dp);

        tweetRetweetButton=(AppCompatCheckBox) findViewById(R.id.tweet_content_retweet_button);
        tweetRetweetButton.setButtonDrawable(R.drawable.ic_retweet_black_24dp);

        tweetReplyButton=(AppCompatImageButton) findViewById(R.id.tweet_content_reply_button);
        Drawable replyIcon= DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_reply_white_24dp));
        DrawableCompat.setTintList(replyIcon,context.getResources().getColorStateList(R.color.reply_button_color_stateful));
        tweetReplyButton.setImageDrawable(replyIcon);

        timeSpanConverter=new TimeSpanConverter();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
            updateView();
        } else {
            for (int i=0;i<4;i++){
                Glide.clear(tweetImageTableView.getImageView(i));
            }
        }
    }
    
    private void updateView(){
        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            if(tweetRetweetUserName.getVisibility()!= View.VISIBLE){
                tweetRetweetUserName.setVisibility(View.VISIBLE);
            }
            tweetRetweetUserName.setText(getContext().getString(R.string.retweet_by,status.getUser().getName()));
        }
        else{
            if(tweetRetweetUserName.getVisibility()!=View.GONE){
                tweetRetweetUserName.setVisibility(View.GONE);
            }
        }

        boolean isReply = status.getInReplyToScreenName()!=null;
        if (isReply){
            if(tweetReplyUserName.getVisibility()!= View.VISIBLE){
                tweetReplyUserName.setVisibility(View.VISIBLE);
            }
            tweetReplyUserName.setText(getContext().getString(R.string.reply_to,status.getUser().getName()));
        }
        else{
            if(tweetReplyUserName.getVisibility()!=View.GONE){
                tweetReplyUserName.setVisibility(View.GONE);
            }
        }

        if (status.isRetweet()||isReply){
            if (tweetHeaderBottomMargin.getVisibility()!=VISIBLE){
                tweetHeaderBottomMargin.setVisibility(VISIBLE);
            }
        } else {
            if (tweetHeaderBottomMargin.getVisibility()!=GONE){
                tweetHeaderBottomMargin.setVisibility(GONE);
            }
        }

        String profileImageUrl;
        if (GlobalApplication.configuration.getTimelineImageLoadMode()<=AppConfiguration.IMAGE_LOAD_MODE_LOW){
            profileImageUrl=item.getUser().getProfileImageURLHttps();
        } else {
            profileImageUrl=item.getUser().getBiggerProfileImageURLHttps();
        }
        imageRequestManager.load(profileImageUrl).into(tweetUserImage);

        tweetUserName.setText(item.getUser().getName());
        tweetUserId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        tweetContext.setText(TwitterStringUtil.getStatusTextSequence(item));

        tweetViaText.setText("via:"+TwitterStringUtil.removeHtmlTags(item.getSource()));

        tweetTimeStampText.setText(timeSpanConverter.toTimeSpanString(item.getCreatedAt().getTime()));
        tweetUserImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(tweetUserImage,"tweet_user_image");
            getContext().startActivity(
                    new Intent(getContext(), ShowUserActivity.class).putExtra("user",item.getUser()),
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), tweetUserImage, "tweet_user_image").toBundle()
            );
        });
        setOnClickListener(v -> {
            ViewCompat.setTransitionName(tweetUserImage,"tweet_user_image");
            getContext().startActivity(
                    new Intent(getContext(),ShowTweetActivity.class).putExtra("status",item),
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), tweetUserImage,"tweet_user_image").toBundle()
            );
        });

        Status quotedStatus=item.getQuotedStatus();
        if(quotedStatus!=null){
            if (tweetQuoteTweetLayout.getVisibility() != View.VISIBLE) {
                tweetQuoteTweetLayout.setVisibility(View.VISIBLE);
            }
            tweetQuoteTweetLayout.setOnClickListener(v -> getContext().startActivity(
                    new Intent(getContext(),ShowTweetActivity.class).putExtra("statusId",(Long)quotedStatus.getId()))
            );
            tweetQuoteTweetUserName.setText(quotedStatus.getUser().getName());
            tweetQuoteTweetUserId.setText(TwitterStringUtil.plusAtMark(quotedStatus.getUser().getScreenName()));
            tweetQuoteTweetContext.setText(quotedStatus.getText());
        }else{
            if (tweetQuoteTweetLayout.getVisibility() != View.GONE) {
                tweetQuoteTweetLayout.setVisibility(View.GONE);
            }
        }

        MediaEntity mediaEntities[]=item.getMediaEntities();

        if (mediaEntities.length!=0){
            tweetImageTableView.setVisibility(View.VISIBLE);
            tweetImageTableView.setImageNumber(mediaEntities.length);
            for (int ii = 0; ii < mediaEntities.length; ii++) {
                ImageView imageView=tweetImageTableView.getImageView(ii);
                int finalIi = ii;
                imageView.setOnClickListener(v-> getContext().startActivity(ShowImageActivity.getIntent(getContext(),mediaEntities, finalIi)));

                int loadMode=GlobalApplication.configuration.getTimelineImageLoadMode();

                if (loadMode!=AppConfiguration.IMAGE_LOAD_MODE_NONE){
                    String mediaUrlPrefix;
                    switch (loadMode){
                        case AppConfiguration.IMAGE_LOAD_MODE_LOW:
                            mediaUrlPrefix="small";
                            break;
                        case AppConfiguration.IMAGE_LOAD_MODE_FULL:
                            mediaUrlPrefix="large";
                            break;
                        default:
                            mediaUrlPrefix="medium";
                    }
                    imageRequestManager
                            .load(mediaEntities[ii].getMediaURLHttps()+":"+mediaUrlPrefix)
                            .centerCrop()
                            .into(imageView);
                } else {
                    imageRequestManager
                            .load(R.drawable.border_frame)
                            .into(imageView);
                }
            }
        }
        else{
            tweetImageTableView.setVisibility(View.GONE);
        }

        tweetLikeButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isFavorited())){
                            subscriber.onNext(GlobalApplication.twitter.createFavorite(item.getId()));
                        }
                        else if((!b)&&item.isFavorited()){
                            subscriber.onNext(GlobalApplication.twitter.destroyFavorite(item.getId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            GlobalApplication.statusCache.add((Status)result);
                            Toast.makeText(getContext(), R.string.succeeded, Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        tweetLikeButton.setChecked(item.isFavorited());

        tweetRetweetButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isRetweeted())){
                            subscriber.onNext(GlobalApplication.twitter.retweetStatus(item.getId()));
                        }
                        else if((!b)&&item.isRetweeted()){
                            subscriber.onNext(GlobalApplication.twitter.destroyStatus(item.getCurrentUserRetweetId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            GlobalApplication.statusCache.add((Status)result);
                            Toast.makeText(getContext(), R.string.succeeded, Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(getContext(),R.string.error_occurred,Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        tweetRetweetButton.setChecked(item.isRetweeted());
        tweetRetweetButton.setEnabled(!item.getUser().isProtected());
    }
}
