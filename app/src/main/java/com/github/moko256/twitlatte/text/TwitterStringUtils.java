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

package com.github.moko256.twitlatte.text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.latte.client.base.entity.StatusAction;
import com.github.moko256.latte.html.entity.Link;
import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.SearchResultActivity;
import com.github.moko256.twitlatte.ShowUserActivity;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.style.ClickableBoldSpan;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.github.moko256.twitlatte.view.DpToPxKt;

import java.util.Arrays;
import java.util.Objects;

import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;
import static com.github.moko256.latte.client.twitter.TwitterApiClientImplKt.CLIENT_TYPE_TWITTER;

/**
 * Created by moko256 on 2016/08/06.
 *
 * @author moko256
 */

public class TwitterStringUtils {

    @NonNull
    public static StringBuilder plusAtMark(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append('@').append(string);
        }
        return stringBuilder;
    }

    private static final int[] maxTable = new int[]{
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, Integer.MAX_VALUE
    };

    private static int uLog10(int x) {
        for (int i = 0; ; i++) {
            if (x < maxTable[i]) {
                return i - 1;
            }
        }
    }

    private static int uPow10(int x) {
        return maxTable[x];
    }

    private static int uRound(@FloatRange(from = 0f) float value) {
        return (int) (value + 0.5);
    }

    public static CharSequence formatIntInCompactForm(int num, int digitPer1Unit, int back, char[] units) {
        if (num == 0) return "0";
        StringBuilder builder = new StringBuilder(digitPer1Unit + 2);
        if (num < 0) {
            num *= -1;
            builder.append('-');
        }
        int exponent = uLog10(num);
        if (exponent < digitPer1Unit) {
            builder.append(num);
        } else {
            int e = (exponent + back) / digitPer1Unit;
            int m = uPow10(e * digitPer1Unit);
            if (num < m) {
                char[] zeros = new char[back + 1];
                Arrays.fill(zeros, '0');
                zeros[back] = '.';
                builder.append(zeros)
                        .append(uRound((float) (uPow10(back) * num) / m));
            } else {
                builder.append(uRound((float) num / m));
            }
            builder.append(units[e - 1]);
        }
        return builder;
    }

    @NonNull
    public static CharSequence convertToReplyTopString(@NonNull String userScreenName,
                                                       @NonNull String replyToScreenName,
                                                       @Nullable String[] users) {
        StringBuilder userIdsStr = new StringBuilder();

        if (!userScreenName.equals(replyToScreenName)) {
            userIdsStr.append('@').append(replyToScreenName).append(' ');
        }

        if (users != null) {
            for (String screenName : users) {
                if (!(screenName.equals(userScreenName) || screenName.equals(replyToScreenName))) {
                    userIdsStr.append('@').append(screenName).append(' ');
                }
            }
        }
        return userIdsStr;
    }

    public static CharSequence getLinkedSequence(Context context, AccessToken accessToken, String text, Link[] links) {
        if (links == null) {
            return text;
        }

        SpannableString spannableString = new SpannableString(text);

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
                        String name = uri.getLastPathSegment();
                        if (name != null) {
                            if (name.split("@")[0].equals(accessToken.getScreenName())) {
                                span = new ClickableBoldSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        context.startActivity(
                                                ShowUserActivity.getIntent(context, name)
                                        );
                                    }
                                };
                            } else {
                                span = new ClickableNoLineSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        context.startActivity(
                                                ShowUserActivity.getIntent(context, name)
                                        );
                                    }
                                };
                            }
                        } else {
                            span = null;
                        }
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
                        AppCustomTabsKt.launchChromeCustomTabs(context, uri, false);
                    }
                };
            }
            int nowLength = text.length();
            int start = link.getStart();
            int end = link.getEnd();
            if (start < end && end <= nowLength) {
                spannableString.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }

    public static CharSequence appendLinkAtViaText(Context context, String name, String url) {
        if (url == null) {
            return name;
        } else {
            SpannableString spannableString = new SpannableString("via:" + name);
            spannableString.setSpan(
                    new ClickableNoLineSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, url, false);
                        }
                    }, 4, name.length() + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }
    }

    public static String convertThumbImageUrl(int clientType, String baseUrl) {
        switch (clientType) {
            case CLIENT_TYPE_TWITTER:
                // Do not use convertTwitterImageUrl() because :thumb is not supported in video
                return baseUrl + ":thumb";
            case CLIENT_TYPE_MASTODON:
                return baseUrl.replace("original", "small");
            default:
                return baseUrl;
        }
    }

    public static String convertSmallImageUrl(int clientType, String baseUrl) {
        switch (clientType) {
            case CLIENT_TYPE_TWITTER:
                return convertTwitterImageUrl(baseUrl, "small");
            case CLIENT_TYPE_MASTODON:
                return baseUrl.replace("original", "small");
            default:
                return baseUrl;
        }
    }

    public static String convertOriginalImageUrl(int clientType, String baseUrl) {
        return (clientType == CLIENT_TYPE_TWITTER) ?
                baseUrl + ":orig" : // Do not use convertTwitterImageUrl() because :orig is not supported
                baseUrl;
    }

    public static String convertLargeImageUrl(int clientType, String baseUrl) {
        return (clientType == CLIENT_TYPE_TWITTER) ?
                convertTwitterImageUrl(baseUrl, "large") :
                baseUrl;
    }

    private static String convertTwitterImageUrl(String baseUrl, String mode) {
        int baseLength = baseUrl.length();
        return new StringBuilder(baseLength + mode.length() + 14)
                .append(baseUrl)
                .replace(baseLength - 4, baseLength, "?format=webp&name=")
                .append(mode)
                .toString();
    }

    public static CharSequence plusUserMarks(String name, TextView textView, boolean isLocked, boolean isAuthorized) {
        if (!isLocked && !isAuthorized) {
            return name;
        }

        SpannableStringBuilder result = new SpannableStringBuilder(name);

        Context context = textView.getContext();

        int textSize = uRound(textView.getLineHeight());
        int left = DpToPxKt.dpToPx(context, 4);

        if (isLocked) {
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

        if (isAuthorized) {
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

    private static final int[] TWITTER_ACTION_ID = new int[]{
            R.string.did_like, R.string.did_unlike, R.string.did_retweet, R.string.did_unretweet
    };

    private static final int[] MASTODON_ACTION_ID = new int[]{
            R.string.did_favorite, R.string.did_unfavorite, R.string.did_boost, R.string.did_unboost, R.string.did_vote
    };

    @StringRes
    public static int getDidActionStringRes(int clientType, StatusAction action) {
        switch (clientType) {
            case CLIENT_TYPE_TWITTER:
                return TWITTER_ACTION_ID[action.ordinal()];

            case CLIENT_TYPE_MASTODON:
                return MASTODON_ACTION_ID[action.ordinal()];

            default:
                return 0;
        }
    }

    @StringRes
    public static int getRepeatedByStringRes(int clientType) {
        switch (clientType) {
            case CLIENT_TYPE_TWITTER:
                return R.string.retweeted_by;

            case CLIENT_TYPE_MASTODON:
                return R.string.boosted_by;

            default:
                return 0;
        }
    }

}