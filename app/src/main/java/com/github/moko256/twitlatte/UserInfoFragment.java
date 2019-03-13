/*
 * Copyright 2015-2019 The twitlatte authors
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.latte.client.base.entity.Emoji;
import com.github.moko256.latte.client.base.entity.Media;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetterKt;
import com.github.moko256.twitlatte.viewmodel.UserInfoViewModel;
import com.github.moko256.twitlatte.widget.UserHeaderImageView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;

import static com.github.moko256.latte.client.base.ApiClientKt.CLIENT_TYPE_NOTHING;
import static com.github.moko256.latte.client.twitter.TwitterApiClientImplKt.CLIENT_TYPE_TWITTER;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class UserInfoFragment extends Fragment implements ToolbarTitleInterface {

    private UserInfoViewModel viewModel;
    private Client client;

    private GlideRequests glideRequests;

    private SwipeRefreshLayout swipeRefreshLayout;

    private UserHeaderImageView header;
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

        userId = Objects.requireNonNull(getArguments()).getLong("userId");

        client = GlobalApplicationKt.getClient(requireActivity());

        viewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);

        if (savedInstanceState == null) {
            viewModel.readCacheRepo = () -> client.getUserCache().get(userId);

            viewModel.writeCacheRepo = user -> {
                client.getUserCache().add(user);
                return Unit.INSTANCE;
            };

            viewModel.remoteRepo = () -> client.getApiClient().showUser(userId);

            viewModel.loadUser();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = requireActivity();

        viewModel.getError().observe(activity, throwable -> {
            throwable.printStackTrace();
            Snackbar.make(
                    ((BaseListFragment.GetViewForSnackBar) activity).getViewForSnackBar(),
                    throwable.getMessage(),
                    Snackbar.LENGTH_LONG
            ).show();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show_user_info,container,false);

        glideRequests = GlideApp.with(this);

        swipeRefreshLayout = view.findViewById(R.id.show_user_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.updateUser());

        header= view.findViewById(R.id.show_user_bgimage);
        header.setWidthPerHeight((client.getAccessToken().getClientType() == CLIENT_TYPE_TWITTER)? 3: 2);
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

        User cachedUser = client.getUserCache().get(userId);
        if (cachedUser!=null){
            setShowUserInfo(cachedUser);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getUser().observe(this, user -> {
            setShowUserInfo(user);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onDestroy() {
        glideRequests.clear(icon);
        glideRequests.clear(header);
        super.onDestroy();
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

            header.setOnClickListener(v -> startActivity(
                    ShowMediasActivity.getIntent(
                            getContext(),
                            new Media[]{
                                    new Media(
                                            null,
                                            headerUrl,
                                            null,
                                            Media.MediaType.PICTURE.getValue()
                                    )
                            },
                            CLIENT_TYPE_NOTHING,
                            0
                    )
            ));
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

        icon.setOnClickListener(v -> startActivity(
                ShowMediasActivity.getIntent(
                        getContext(),
                        new Media[]{
                                new Media(
                                        null,
                                        user.getOriginalProfileImageURLHttps(),
                                        null,
                                        Media.MediaType.PICTURE.getValue()
                                )
                        },
                        CLIENT_TYPE_NOTHING,
                        0
                )
        ));

        CharSequence userName = TwitterStringUtils.plusUserMarks(
                user.getName(),
                userNameText,
                user.isProtected(),
                user.isVerified()
        );
        CharSequence userBio = TwitterStringUtils.getLinkedSequence(
                getContext(),
                client.getAccessToken(),
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
                EmojiToTextViewSetterKt.bindToLifecycle(setOfName, this);
            } else {
                if (userBioEmojiSetter == null) {
                    userBioEmojiSetter = new EmojiToTextViewSetter(glideRequests, userBioText);
                }
                Disposable[] setOfBio = userBioEmojiSetter.set(userBio, userNameEmojis);
                if (setOfBio != null) {
                    EmojiToTextViewSetterKt.bindToLifecycle(setOfBio, this);
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
            SpannableString spannableString = new SpannableString(text);
            int start = text.indexOf(url);
            spannableString.setSpan(new ClickableNoLineSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    AppCustomTabsKt.launchChromeCustomTabs(requireContext(), url, false);
                }
            }, start, start + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            userUrl.setText(spannableString);
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            userUrl.setVisibility(View.GONE);
        }

        userCreatedAt.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(user.getCreatedAt()));
        userTweetsCount.setText(getString(R.string.posts_counts_is, user.getStatusesCount()));
        userFollowCount.setText(getString(R.string.following_counts_is, user.getFriendsCount()));
        userFollowerCount.setText(getString(R.string.followers_counts_is, user.getFollowersCount()));
    }
}
