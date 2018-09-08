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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.github.moko256.mastodon.MTException;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.SearchResultActivity;
import com.github.moko256.twitlatte.ShowUserActivity;
import com.github.moko256.twitlatte.entity.Type;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.text.link.MTHtmlParser;
import com.github.moko256.twitlatte.text.link.entity.Link;
import com.github.moko256.twitlatte.text.style.ClickableNoLineSpan;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import kotlin.jvm.functions.Function1;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * Created by moko256 on 2016/08/06.
 *
 * @author moko256
 */

public class TwitterStringUtils {

    @NonNull
    public static String plusAtMark(String... strings){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string: strings) {
            stringBuilder.append("@").append(string);
        }
        return stringBuilder.toString();
    }

    public static String convertToSIUnitString(int num){
        if (num == 0) return "0";
        boolean isNegative = (num < 0);
        String sign;
        if (isNegative) {
            num *= -1;
            sign = "-";
        } else {
            sign = "";
        }

        float k = num / 1000;
        if (k < 1) return sign + String.valueOf(num);

        float m = k / 1000;
        if (m < 1) return sign + String.valueOf(Math.round(k)) + "K";

        float g = m / 1000;
        if (g < 1) return sign + String.valueOf(Math.round(m)) + "M";

        return sign + String.valueOf(Math.round(g)) + "G";
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
            return MTException.convertErrorString((Mastodon4jRequestException) e);
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
            if (uri.getScheme().equals("twitlatte")) {
                switch (uri.getHost()) {
                    case "symbol":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(
                                        SearchResultActivity.getIntent(context, "$" + uri.getLastPathSegment())
                                );

                            }
                        };
                        break;
                    case "hashtag":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(
                                        SearchResultActivity.getIntent(context, "#" + uri.getLastPathSegment())
                                );
                            }
                        };
                        break;
                    case "user":
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(
                                        ShowUserActivity.getIntent(context, uri.getLastPathSegment())
                                );
                            }
                        };
                        break;
                    default:
                        span = new ClickableNoLineSpan() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(SearchResultActivity.getIntent(context, uri.getLastPathSegment()));
                            }
                        };
                        break;
                }
            } else {
                span = new ClickableNoLineSpan() {
                    @Override
                    public void onClick(View view) {
                        AppCustomTabsKt.launchChromeCustomTabs(context, uri);
                    }
                };
            }
            spannableStringBuilder.setSpan(span, link.getStart(), link.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    public static CharSequence getProfileLinkedSequence(Context context, User user){

        String description = user.getDescription();

        if (GlobalApplication.clientType == Type.MASTODON){
            return MTHtmlParser.INSTANCE.convertToEntities(description, linkParserListener(context));
        } else {
            URLEntity[] urlEntities = user.getDescriptionURLEntities();

            if (urlEntities == null
                    || urlEntities.length <= 0
                    || TextUtils.isEmpty(urlEntities[0].getURL())
                    ) {
                return description;
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(description);
            int tweetLength = description.length();
            int sp = 0;

            for (URLEntity entity : urlEntities) {
                String url = entity.getURL();
                String expandedUrl = entity.getDisplayURL();

                int urlLength = url.length();
                int displayUrlLength = expandedUrl.length();

                int start = entity.getStart();
                int end = entity.getEnd();

                if (start <= tweetLength && end <= tweetLength) {
                    int dusp = displayUrlLength - urlLength;

                    start += sp;
                    end += sp;

                    spannableStringBuilder.replace(start, end, expandedUrl);
                    spannableStringBuilder.setSpan(new ClickableNoLineSpan() {
                        @Override
                        public void onClick(View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, entity.getExpandedURL());
                        }
                    }, start, end + dusp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    sp += dusp;
                }
            }

            return spannableStringBuilder;
        }
    }

    public static Function1<String, Object> linkParserListener(Context context) {
         return link -> {
            final Uri uri = Uri.parse(link);
            if (uri.getScheme().equals("twitlatte")) {
                if (uri.getHost().equals("user")) {
                    return new ClickableNoLineSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(
                                    ShowUserActivity.getIntent(context, uri.getLastPathSegment())
                            );
                        }
                    };
                } else {
                    return new ClickableNoLineSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(SearchResultActivity.getIntent(context, uri.getLastPathSegment()));
                        }
                    };
                }
            } else {
                return new ClickableNoLineSpan() {
                    @Override
                    public void onClick(View view) {
                        AppCustomTabsKt.launchChromeCustomTabs(context, link);
                    }
                };
            }
        };
    }

    public static CharSequence appendLinkAtViaText(Context context, String name, String url) {
        if (url == null) {
            return name;
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("via:" + name);
            spannableStringBuilder.setSpan(
                    new ClickableNoLineSpan() {
                        @Override
                        public void onClick(View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, url);
                        }
                    }, 4, name.length() + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableStringBuilder;
        }
    }

    public static String convertThumbImageUrl(String baseUrl){
        return (GlobalApplication.clientType == Type.TWITTER)?
                baseUrl + ":thumb":
                baseUrl.replace("original", "small");
    }

    public static String convertSmallImageUrl(String baseUrl){
        return (GlobalApplication.clientType == Type.TWITTER)?
                baseUrl + ":small":
                baseUrl.replace("original", "small");
    }

    public static String convertOriginalImageUrl(String baseUrl){
        return (GlobalApplication.clientType == Type.TWITTER)?
                baseUrl + ":orig":
                baseUrl;
    }

    public static String convertLargeImageUrl(String baseUrl){
        return (GlobalApplication.clientType == Type.TWITTER)?
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

            Drawable drawable = AppCompatResources
                    .getDrawable(context, R.drawable.ic_lock_black_24dp)
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

            Drawable drawable = AppCompatResources
                    .getDrawable(context, R.drawable.ic_check_circle_black_24dp)
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
    public static int getDidActionStringRes(@Type.ClientTypeInt int type, Action action){
        switch (type){
            case Type.TWITTER:
                switch (action){
                    case LIKE:
                        return R.string.did_like;
                    case UNLIKE:
                        return R.string.did_unlike;
                    case REPEAT:
                        return R.string.did_retweet;
                    case UNREPEAT:
                        return R.string.did_unretweet;
                     default:
                        return 0;
                }

            case Type.MASTODON:
                switch (action){
                    case LIKE:
                        return R.string.did_favorite;
                    case UNLIKE:
                        return R.string.did_unfavorite;
                    case REPEAT:
                        return R.string.did_boost;
                    case UNREPEAT:
                        return R.string.did_unboost;
                    default:
                        return 0;
                }

            default:
                return 0;
        }
    }

    @StringRes
    public static int getRepeatedByStringRes(@Type.ClientTypeInt int type){
        switch (type){
            case Type.TWITTER:
                return R.string.retweeted_by;

            case Type.MASTODON:
                return R.string.boosted_by;

            default:
                return 0;
        }
    }

    public enum Action{
        LIKE,
        UNLIKE,
        REPEAT,
        UNREPEAT
    }

}
