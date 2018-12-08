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

package com.github.moko256.twitlatte.text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.SearchResultActivity;
import com.github.moko256.twitlatte.ShowUserActivity;
import com.github.moko256.twitlatte.entity.StatusAction;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.link.entity.Link;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import twitter4j.TwitterException;

import static com.github.moko256.twitlatte.api.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;
import static com.github.moko256.twitlatte.api.twitter.TwitterApiClientImplKt.CLIENT_TYPE_TWITTER;

/**
 * Created by moko256 on 2016/08/06.
 *
 * @author moko256
 */

public class TwitterStringUtils {

    @NonNull
    public static StringBuilder plusAtMark(String... strings){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string: strings) {
            stringBuilder.append("@").append(string);
        }
        return stringBuilder;
    }

    public static CharSequence convertToSIUnitString(int num){
        if (num == 0) return "0";
        StringBuilder builder = new StringBuilder(5);
        if (num < 0) {
            num *= -1;
            builder.append("-");
        }

        float k = num / 1000;
        if (k < 1) return builder.append(String.valueOf(num));

        float m = k / 1000;
        if (m < 1) return builder.append(String.valueOf(Math.round(k))).append("K");

        float g = m / 1000;
        if (g < 1) return builder.append(String.valueOf(Math.round(m))).append("M");

        return builder.append(String.valueOf(Math.round(g))).append("G");
    }

    @NonNull
    public static CharSequence convertToReplyTopString(@NonNull String userScreenName,
                                                       @NonNull String replyToScreenName,
                                                       @Nullable String[] users){
        StringBuilder userIdsStr = new StringBuilder();

        if (!userScreenName.equals(replyToScreenName)) {
            userIdsStr.append("@").append(replyToScreenName).append(" ");
        }

        if (users != null) {
            for (String screenName : users) {
                if (!(screenName.equals(userScreenName) || screenName.equals(replyToScreenName))) {
                    userIdsStr.append("@").append(screenName).append(" ");
                }
            }
        }
        return userIdsStr;
    }

    public static String convertErrorToText(@NonNull Throwable e){
        if (e instanceof TwitterException && !TextUtils.isEmpty(((TwitterException) e).getErrorMessage())){
            return ((TwitterException) e).getErrorMessage();
        } else if (e instanceof Mastodon4jRequestException
                && ((Mastodon4jRequestException) e).isErrorResponse()) {
            String message;
            try {
                message = ((Mastodon4jRequestException) e).getResponse().body().string();
            } catch (Exception e1){
                message = "Unknown";
            }
            return message;
        } else {
            return e.getMessage();
        }
    }

    public static CharSequence getLinkedSequence(Context context, String text, Link[] links){
        if (links == null) {
            return text;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);

        for (Link link : links) {
            Object span;

            final Uri uri = Uri.parse(link.getUrl());
            if (uri.getScheme() != null && uri.getHost() != null && uri.getScheme().equals("twitlatte")) {
                switch (uri.getHost()) {
                    case "symbol":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(@NonNull View view) {
                                context.startActivity(
                                        SearchResultActivity.getIntent(context, "$" + uri.getLastPathSegment())
                                );

                            }
                        };
                        break;
                    case "hashtag":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(@NonNull View view) {
                                context.startActivity(
                                        SearchResultActivity.getIntent(context, "#" + uri.getLastPathSegment())
                                );
                            }
                        };
                        break;
                    case "user":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(@NonNull View view) {
                                context.startActivity(
                                        ShowUserActivity.getIntent(context, uri.getLastPathSegment())
                                );
                            }
                        };
                        break;
                    default:
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(@NonNull View view) {
                                context.startActivity(SearchResultActivity.getIntent(context, uri.getLastPathSegment()));
                            }
                        };
                        break;
                }
            } else {
                span = new ClickableNoLineSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        AppCustomTabsKt.launchChromeCustomTabs(context, uri);
                    }
                };
            }
            int nowLength = text.length();
            int start = link.getStart();
            int end = link.getEnd();
            if (end <= nowLength) {
                spannableStringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableStringBuilder;
    }

    public static CharSequence appendLinkAtViaText(Context context, String name, String url) {
        if (url == null) {
            return name;
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("via:" + name);
            spannableStringBuilder.setSpan(
                    new ClickableNoLineSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, url);
                        }
                    }, 4, name.length() + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableStringBuilder;
        }
    }

    public static String convertThumbImageUrl(int clientType, String baseUrl){
        return (clientType == CLIENT_TYPE_TWITTER)?
                baseUrl + ":thumb":
                baseUrl.replace("original", "small");
    }

    public static String convertSmallImageUrl(int clientType, String baseUrl){
        return (clientType == CLIENT_TYPE_TWITTER)?
                baseUrl + ":small":
                baseUrl.replace("original", "small");
    }

    public static String convertOriginalImageUrl(int clientType, String baseUrl){
        return (clientType == CLIENT_TYPE_TWITTER)?
                baseUrl + ":orig":
                baseUrl;
    }

    public static String convertLargeImageUrl(int clientType, String baseUrl){
        return (clientType == CLIENT_TYPE_TWITTER)?
                baseUrl + ":large":
                baseUrl;
    }

    public static CharSequence plusUserMarks(String name, TextView textView, boolean isLocked, boolean isAuthorized){
        if (!isLocked && !isAuthorized){
            return name;
        }

        SpannableStringBuilder result = new SpannableStringBuilder(name);

        Context context = textView.getContext();

        int textSize = Math.round(textView.getLineHeight());
        int left = Math.round(4 * context.getResources().getDisplayMetrics().density);

        if (isLocked){
            int length = result.length();
            result.append("\uFFFC");

            Drawable drawable = Objects.requireNonNull(AppCompatResources
                    .getDrawable(context, R.drawable.ic_lock_black_24dp))
                    .mutate();
            drawable.setBounds(left, 0, textSize + left, textSize
            );

            DrawableCompat.setTint(drawable, textView.getCurrentTextColor());

            result.setSpan(new ImageSpan(
                    drawable
            ), length, length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (isAuthorized){
            int length = result.length();
            result.append("\uFFFC");

            Drawable drawable = Objects.requireNonNull(AppCompatResources
                    .getDrawable(context, R.drawable.ic_check_circle_black_24dp))
                    .mutate();
            drawable.setBounds(left, 0, textSize + left, textSize);

            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.color_accent));

            result.setSpan(new ImageSpan(
                    drawable
            ), length, length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }

    @StringRes
    public static int getDidActionStringRes(int clientType, StatusAction action){
        switch (clientType){
            case CLIENT_TYPE_TWITTER:
                switch (action){
                    case FAVORITE:
                        return R.string.did_like;
                    case UNFAVORITE:
                        return R.string.did_unlike;
                    case REPEAT:
                        return R.string.did_retweet;
                    case UNREPEAT:
                        return R.string.did_unretweet;
                }

            case CLIENT_TYPE_MASTODON:
                switch (action){
                    case FAVORITE:
                        return R.string.did_favorite;
                    case UNFAVORITE:
                        return R.string.did_unfavorite;
                    case REPEAT:
                        return R.string.did_boost;
                    case UNREPEAT:
                        return R.string.did_unboost;
                }

            default:
                return 0;
        }
    }

    @StringRes
    public static int getRepeatedByStringRes(int clientType){
        switch (clientType){
            case CLIENT_TYPE_TWITTER:
                return R.string.retweeted_by;

            case CLIENT_TYPE_MASTODON:
                return R.string.boosted_by;

            default:
                return 0;
        }
    }

}