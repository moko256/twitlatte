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

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.latte.client.base.MediaUrlConverter;
import com.github.moko256.latte.client.base.entity.Emoji;
import com.github.moko256.latte.client.base.entity.Media;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter;
import com.github.moko256.twitlatte.viewmodel.UserInfoViewModel;
import com.github.moko256.twitlatte.widget.UserHeaderImageView;

import java.text.DateFormat;

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

    private RequestManager glideRequests;

    private UserHeaderImageView header;
    private ImageView icon;

    private TextView userNameText;
    private TextView userIdText;
    private TextView userBioText;
    private TextView userLocation;
    private TextView userUrl;
    private TextView userCreatedAt;
    private TextView userCounts;
    private TextView userIsYouOrFollowedYou;
    private Button userFollowButton;
    private Button userUnfollowButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = GlobalApplicationKt.getClient(requireActivity());
        viewModel = ViewModelProviders.of(requireActivity()).get(UserInfoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_show_user, container, false);

        glideRequests = Glide.with(this);

        header = view.findViewById(R.id.show_user_bgimage);
        header.setWidthPerHeight((client.getAccessToken().getClientType() == CLIENT_TYPE_TWITTER) ? 3 : 2);
        icon = view.findViewById(R.id.show_user_image);
        ShapeDrawable iconBackground = new ShapeDrawable();
        iconBackground.setColorFilter(
                new PorterDuffColorFilter(
                        ((ColorDrawable) requireActivity().getWindow().getDecorView().getBackground()).getColor(),
                        PorterDuff.Mode.SRC_ATOP
                )
        );
        iconBackground.setShape(new OvalShape());
        icon.setBackground(iconBackground);

        userNameText = view.findViewById(R.id.show_user_name);
        userIdText = view.findViewById(R.id.show_user_id);
        userBioText = view.findViewById(R.id.show_user_bio);
        userBioText.setMovementMethod(LinkMovementMethod.getInstance());
        userLocation = view.findViewById(R.id.show_user_location);
        userUrl = view.findViewById(R.id.show_user_url);
        userCreatedAt = view.findViewById(R.id.show_user_created_at);
        userCounts = view.findViewById(R.id.show_user_counts);
        userIsYouOrFollowedYou = view.findViewById(R.id.followed_or_you);
        userFollowButton = view.findViewById(R.id.follow);
        userUnfollowButton = view.findViewById(R.id.unfollow);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getUser().observe(this, this::setShowUserInfo);
        viewModel.getFriendship().observe(
                this,
                friendship -> {
                    if (friendship.getFollowedBy()) {
                        userIsYouOrFollowedYou.setVisibility(View.VISIBLE);
                    } else {
                        userIsYouOrFollowedYou.setVisibility(View.GONE);
                    }
                    userIsYouOrFollowedYou.setText(R.string.follows_you);
                    if (friendship.getFollowing()) {
                        userFollowButton.setVisibility(View.GONE);
                        userUnfollowButton.setVisibility(View.VISIBLE);
                    } else {
                        userFollowButton.setVisibility(View.VISIBLE);
                        userUnfollowButton.setVisibility(View.GONE);
                    }
                }
        );
        userFollowButton.setOnClickListener(
                v -> confirmDialog(
                        R.string.confirm_follow,
                        (dialog, which) -> viewModel.requestCreateFollow(getString(R.string.did_follow))
                )
        );
        userUnfollowButton.setOnClickListener(
                v -> confirmDialog(
                        R.string.confirm_unfollow,
                        (dialog, which) -> viewModel.requestDestroyFollow(getString(R.string.did_unfollow))
                )
        );
    }

    private void confirmDialog(int message, DialogInterface.OnClickListener callback) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, callback)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
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
        if (user.getId() == client.getAccessToken().getUserId()) {
            userIsYouOrFollowedYou.setVisibility(View.VISIBLE);
            userIsYouOrFollowedYou.setText(R.string.you);
        }

        MediaUrlConverter mediaUrlConverter = client.getMediaUrlConverter();

        String headerUrl = mediaUrlConverter.convertProfileBannerLargeUrl(user);
        if (headerUrl != null) {
            glideRequests
                    .load(headerUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(header);

            header.setOnClickListener(v -> startActivity(
                    ShowMediasActivity.Companion.getIntent(
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
            if (!TextUtils.isEmpty(colorStr)) {
                header.setBackgroundColor(Color.parseColor("#" + colorStr));
            }
        }
        glideRequests
                .load(mediaUrlConverter.convertProfileIconLargeUrl(user))
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(icon);

        icon.setOnClickListener(v -> startActivity(
                ShowMediasActivity.Companion.getIntent(
                        getContext(),
                        new Media[]{
                                new Media(
                                        null,
                                        mediaUrlConverter.convertProfileIconOriginalUrl(user),
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
            new EmojiToTextViewSetter(glideRequests, userNameText, userName, userNameEmojis)
                    .bindToLifecycle(this);

            new EmojiToTextViewSetter(glideRequests, userBioText, userBio, userNameEmojis)
                    .bindToLifecycle(this);
        }

        userIdText.setText(TwitterStringUtils.plusAtMark(user.getScreenName()));

        if (!TextUtils.isEmpty(user.getLocation())) {
            userLocation.setText(getString(R.string.location_is, user.getLocation()));
        } else {
            userLocation.setVisibility(View.GONE);
        }

        final String url = user.getUrl();
        if (!TextUtils.isEmpty(url)) {
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

        userCreatedAt.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(user.getCreatedAt()));
        userCounts.setText(getString(R.string.user_counts_is, user.getStatusesCount(), user.getFriendsCount(), user.getFollowersCount()));
    }
}
