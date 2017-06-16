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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.text.DateFormat;

import rx.Observable;
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

    RequestManager requestManager;

    ImageView header;
    ImageView icon;

    TextView userNameText;
    TextView userIdText;
    TextView userBioText;
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
            Observable
                    .create(
                            subscriber -> {
                                try {
                                    subscriber.onNext(GlobalApplication.twitter.showUser(userId));
                                    subscriber.onCompleted();
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }
                    )
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> setShowUserInfo((User) result),
                            throwable->{
                                throwable.printStackTrace();
                                getActivity().finish();
                            },
                            ()->{}
                    );
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show_user_info,container,false);

        requestManager=Glide.with(this);

        header= view.findViewById(R.id.show_user_bgimage);
        icon= view.findViewById(R.id.show_user_image);

        userNameText= view.findViewById(R.id.show_user_name);
        userIdText= view.findViewById(R.id.show_user_id);
        userBioText = view.findViewById(R.id.show_user_bio);
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
        requestManager.load(user.getProfileBannerRetinaURL()).into(header);
        requestManager.load(user.getBiggerProfileImageURL()).asBitmap().into(new CircleImageTarget(icon));

        userNameText.setText(user.getName());
        userIdText.setText(TwitterStringUtil.plusAtMark(user.getScreenName()));
        getActivity().setTitle(user.getName());
        userBioText.setText(user.getDescription());

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
