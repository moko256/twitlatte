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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.twitlatte.entity.Emoji;
import com.github.moko256.twitlatte.entity.User;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;

import java.text.DateFormat;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class UserInfoFragment extends Fragment implements ToolbarTitleInterface {

    private CompositeDisposable disposable;

    private GlideRequests glideRequests;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ImageView header;
    private ImageView icon;

    private TextView userNameText;
    private EmojiToTextViewSetter userNameEmojiSetter;
    private TextView userIdText;
    private TextView userBioText;
    private EmojiToTextViewSetter userBioEmojiSetter;
    private TextView userLocation;
    private TextView userUrl;
    private TextView userCreatedAt;
    private TextView userTweetsCount;
    private TextView userFollowCount;
    private TextView userFollowerCount;

    private long userId = -1;

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
        disposable = new CompositeDisposable();
        userId = Objects.requireNonNull(getArguments()).getLong("userId");

        User cachedUser = GlobalApplication.userCache.get(userId);
        if (cachedUser==null){
            disposable.add(
                    updateUser()
                            .subscribeOn(Schedulers.io())
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

        glideRequests = GlideApp.with(this);

        swipeRefreshLayout = view.findViewById(R.id.show_user_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(() -> disposable.add(updateUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> swipeRefreshLayout.setRefreshing(false))
                .subscribe(
                        this::setShowUserInfo,
                        throwable -> {
                            throwable.printStackTrace();
                            Snackbar.make(
                                    ((BaseListFragment.GetViewForSnackBar) requireActivity()).getViewForSnackBar(),
                                    TwitterStringUtils.convertErrorToText(throwable),
                                    Snackbar.LENGTH_LONG
                            );
                        }
                )));

        header= view.findViewById(R.id.show_user_bgimage);
        icon= view.findViewById(R.id.show_user_image);

        userNameText = view.findViewById(R.id.show_user_name);
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
        disposable.dispose();
        super.onDestroy();
        disposable = null;
    }

    @Override
    public int getTitleResourceId() {
        return R.string.account;
    }

    private void setShowUserInfo(User user) {
        String headerUrl = user.getProfileBanner1500x500URL();
        if (headerUrl != null) {
            glideRequests
                    .load(headerUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(header);
        } else {
            String colorStr = user.getProfileBackgroundColor();
            if (!TextUtils.isEmpty(colorStr)){
                header.setBackgroundColor(Color.parseColor("#" + colorStr));
            }
        }
        glideRequests
                .load(user.get400x400ProfileImageURLHttps())
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(icon);

        CharSequence userName = TwitterStringUtils.plusUserMarks(
                user.getName(),
                userNameText,
                user.isProtected(),
                user.isVerified()
        );
        CharSequence userBio = TwitterStringUtils.getLinkedSequence(
                getContext(),
                user.getDescription(),
                user.getDescriptionLinks()
        );
        userNameText.setText(userName);
        userBioText.setText(userBio);
        Emoji[] userNameEmojis = user.getEmojis();
        if (userNameEmojis != null) {
            if (userNameEmojiSetter == null) {
                userNameEmojiSetter = new EmojiToTextViewSetter(glideRequests, userNameText);
            }
            Disposable[] setOfName = userNameEmojiSetter.set(userName, userNameEmojis);
            if (setOfName != null) {
                disposable.addAll(setOfName);
            } else {
                if (userBioEmojiSetter == null) {
                    userBioEmojiSetter = new EmojiToTextViewSetter(glideRequests, userBioText);
                }
                Disposable[] setOfBio = userBioEmojiSetter.set(userBio, userNameEmojis);
                if (setOfBio != null) {
                    disposable.addAll(setOfBio);
                }
            }
        }

        userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName()));
        requireActivity().setTitle(user.getName());

        if (!TextUtils.isEmpty(user.getLocation())){
            userLocation.setText(getString(R.string.location_is, user.getLocation()));
        } else {
            userLocation.setVisibility(View.GONE);
        }

        final String url = user.getUrl();
        if (!TextUtils.isEmpty(url)){
            String text = getString(R.string.url_is, url);
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            int start = text.indexOf(url);
            builder.setSpan(new ClickableNoLineSpan() {
                @Override
                public void onClick(View widget) {
                    AppCustomTabsKt.launchChromeCustomTabs(requireContext(), url);
                }
            }, start, start + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            userUrl.setText(builder);
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            userUrl.setVisibility(View.GONE);
        }

        userCreatedAt.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(user.getCreatedAt()));
        userTweetsCount.setText(getString(R.string.posts_counts_is, user.getStatusesCount()));
        userFollowCount.setText(getString(R.string.following_counts_is, user.getFriendsCount()));
        userFollowerCount.setText(getString(R.string.followers_counts_is, user.getFollowersCount()));
    }

    private Single<User> updateUser(){
        return Single.create(
                subscriber -> {
                    try {
                        twitter4j.User user = GlobalApplication.twitter.showUser(userId);
                        GlobalApplication.userCache.add(user);
                        subscriber.onSuccess(GlobalApplication.userCache.get(userId));
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                });
    }
}
