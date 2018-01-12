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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.text.util.LinkifyCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.text.DateFormat;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class UserInfoFragment extends Fragment implements ToolbarTitleInterface {

    CompositeSubscription subscription;

    GlideRequests glideRequests;

    SwipeRefreshLayout swipeRefreshLayout;

    ImageView header;
    ImageView icon;

    TextView userNameText;
    ImageView userLockIcon;
    TextView userIdText;
    TextView userBioText;
    TextView userLocation;
    TextView userUrl;
    TextView userCreatedAt;
    TextView userTweetsCount;
    TextView userFollowCount;
    TextView userFollowerCount;

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
        subscription = new CompositeSubscription();
        userId = getArguments().getLong("userId");

        User cachedUser = GlobalApplication.userCache.get(userId);
        if (cachedUser==null){
            subscription.add(
                    updateUser()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    this::setShowUserInfo,
                                    Throwable::printStackTrace
                            )
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show_user_info,container,false);

        glideRequests =GlideApp.with(this);

        swipeRefreshLayout = view.findViewById(R.id.show_user_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(() -> subscription.add(updateUser()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            setShowUserInfo(result);
                            swipeRefreshLayout.setRefreshing(false);
                        },
                        Throwable::printStackTrace
                )));

        header= view.findViewById(R.id.show_user_bgimage);
        icon= view.findViewById(R.id.show_user_image);

        userNameText = view.findViewById(R.id.show_user_name);
        userLockIcon = view.findViewById(R.id.show_user_lock);
        userIdText = view.findViewById(R.id.show_user_id);
        userBioText = view.findViewById(R.id.show_user_bio);
        userBioText.setMovementMethod(LinkMovementMethod.getInstance());
        userLocation = view.findViewById(R.id.show_user_location);
        userUrl = view.findViewById(R.id.show_user_url);
        userCreatedAt = view.findViewById(R.id.show_user_created_at);
        userTweetsCount = view.findViewById(R.id.show_user_tweets_count);
        userFollowCount = view.findViewById(R.id.show_user_follow_count);
        userFollowerCount = view.findViewById(R.id.show_user_follower_count);

        User cachedUser = GlobalApplication.userCache.get(userId);
        if (cachedUser!=null){
            setShowUserInfo(cachedUser);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        subscription = null;
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
        glideRequests.load(user.get400x400ProfileImageURLHttps()).circleCrop().into(icon);

        userNameText.setText(user.getName());
        userLockIcon.setVisibility(user.isProtected()? View.VISIBLE: View.GONE);
        userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName()));
        getActivity().setTitle(user.getName());
        userBioText.setText(TwitterStringUtils.getProfileLinkedSequence(getContext(), user));

        if (!TextUtils.isEmpty(user.getLocation())){
            userLocation.setText(getString(R.string.location_is, user.getLocation()));
        } else {
            userLocation.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(user.getURL())){
            SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.url_is, user.getURL()));
            LinkifyCompat.addLinks(builder, Linkify.WEB_URLS);
            userUrl.setText(TwitterStringUtils.convertUrlSpanToCustomTabs(builder, getContext()));
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            userUrl.setVisibility(View.GONE);
        }

        userCreatedAt.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(user.getCreatedAt()));
        userTweetsCount.setText(getString(R.string.post_counts_is, user.getStatusesCount()));
        userFollowCount.setText(getString(R.string.follow_counts_is, user.getFriendsCount()));
        userFollowerCount.setText(getString(R.string.follower_counts_is, user.getFollowersCount()));
    }

    private Single<User> updateUser(){
        return Single.create(
                subscriber -> {
                    try {
                        User user = GlobalApplication.twitter.showUser(userId);
                        GlobalApplication.userCache.add(user);
                        subscriber.onSuccess(user);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                });
    }
}
