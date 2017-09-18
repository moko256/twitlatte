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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.text.DateFormat;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class UserInfoFragment extends Fragment implements ToolbarTitleInterface {

    GlideRequests glideRequests;

    ImageView header;
    ImageView icon;

    TextView userNameText;
    TextView userIdText;
    TextView userBioText;
    TextView userLocation;
    TextView userUrl;
    TextView userCreatedAt;
    TextView userTweetsCount;
    TextView userFollowCount;
    TextView userFollowerCount;

    CardView userLatestTweetCard;
    TextView userLatestTweetText;
    Button userLatestTweetLoadMoreButton;

    long userId = -1;

    public static UserInfoFragment newInstance(long userId){
        UserInfoFragment result = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putLong("userId", userId);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getArguments().getLong("userId");

        User cachedUser = GlobalApplication.userCache.get(userId);
        if (cachedUser==null){
            Single.create(
                    subscriber -> {
                        try {
                            subscriber.onSuccess(GlobalApplication.twitter.showUser(userId));
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> setShowUserInfo((User) result),
                            throwable->{
                                throwable.printStackTrace();
                                getActivity().finish();
                            }
                    );
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show_user_info,container,false);

        glideRequests =GlideApp.with(this);

        header= view.findViewById(R.id.show_user_bgimage);
        icon= view.findViewById(R.id.show_user_image);

        userNameText= view.findViewById(R.id.show_user_name);
        userIdText= view.findViewById(R.id.show_user_id);
        userBioText = view.findViewById(R.id.show_user_bio);
        userLocation = view.findViewById(R.id.show_user_location);
        userUrl = view.findViewById(R.id.show_user_url);
        userCreatedAt= view.findViewById(R.id.show_user_created_at);
        userTweetsCount= view.findViewById(R.id.show_user_tweets_count);
        userFollowCount= view.findViewById(R.id.show_user_follow_count);
        userFollowerCount= view.findViewById(R.id.show_user_follower_count);

        userLatestTweetCard= view.findViewById(R.id.show_user_latest_tweet);
        userLatestTweetText= view.findViewById(R.id.show_user_latest_tweet_text);
        userLatestTweetLoadMoreButton= view.findViewById(R.id.show_user_latest_tweet_see_more_button);

        User cachedUser = GlobalApplication.userCache.get(userId);
        if (cachedUser!=null){
            setShowUserInfo(cachedUser);
        }

        return view;
    }

    @Override
    public int getTitleResourceId() {
        return R.string.account;
    }

    private void setShowUserInfo(User user) {
        String headerUrl = user.getProfileBanner1500x500URL();
        if (headerUrl != null) {
            glideRequests.load(headerUrl).into(header);
        } else {
            String colorStr = user.getProfileBackgroundColor();
            if (!TextUtils.isEmpty(colorStr)){
                header.setBackgroundColor(Color.parseColor("#" + colorStr));
            }
        }
        glideRequests.asBitmap().load(user.getBiggerProfileImageURL()).circleCrop().into(icon);

        userNameText.setText(user.getName());
        userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName()));
        getActivity().setTitle(user.getName());
        userBioText.setText(TwitterStringUtils.getProfileLinkedSequence(getContext(), user));
        userBioText.setMovementMethod(LinkMovementMethod.getInstance());

        if (!TextUtils.isEmpty(user.getLocation())){
            userLocation.setText(getString(R.string.location_is, user.getLocation()));
        } else {
            userLocation.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(user.getURL())){
            userUrl.setText(getString(R.string.url_is, user.getURL()));
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            userUrl.setVisibility(View.GONE);
        }

        userCreatedAt.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(user.getCreatedAt()));
        userTweetsCount.setText(getContext().getString(R.string.tweet_counts_is,String.valueOf(user.getStatusesCount())));
        userFollowCount.setText(getContext().getString(R.string.follow_counts_is,String.valueOf(user.getFriendsCount())));
        userFollowerCount.setText(getContext().getString(R.string.follower_counts_is,String.valueOf(user.getFollowersCount())));

        Status status=user.getStatus();
        if (status == null) {
            userLatestTweetCard.setVisibility(View.GONE);
        } else {
            userLatestTweetText.setText(status.getText());
        }
    }
}
