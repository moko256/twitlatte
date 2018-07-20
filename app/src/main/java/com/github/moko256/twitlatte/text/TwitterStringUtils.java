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
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.mastodon.MTException;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.SearchResultActivity;
import com.github.moko256.twitlatte.ShowUserActivity;
import com.github.moko256.twitlatte.entity.Type;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.jvm.functions.Function1;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

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
                                                       @NonNull UserMentionEntity[] users){
        StringBuilder userIdsStr = new StringBuilder();

        if (!userScreenName.equals(replyToScreenName)) {
            userIdsStr.append("@").append(replyToScreenName).append(" ");
        }

        for (UserMentionEntity user : users) {
            String screenName = user.getScreenName();
            if (!(screenName.equals(userScreenName) || screenName.equals(replyToScreenName))) {
                userIdsStr.append("@").append(screenName).append(" ");
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

    public static CharSequence getLinkedSequence(Context context, Status item){

        String tweet = item.getText();

        if (tweet == null) {
            return "";
        }

        if (GlobalApplication.clientType == Type.MASTODON){
            CharSequence html = MTHtmlParser.INSTANCE.convertToEntities(tweet, linkParserListener(context));
            int length = html.length();
            // If post has media only, context of post from Mastodon is "."
            if (length == 1
                    && item.getMediaEntities().length > 0
                    && html.charAt(0) == ".".charAt(0)
                    ){
                return "";
            } else {
                return html;
            }
        } else {

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(tweet);

            try {
                for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
                    spannableStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(SearchResultActivity.getIntent(context, "$" + symbolEntity.getText()));
                        }
                    }, tweet.offsetByCodePoints(0, symbolEntity.getStart()), tweet.offsetByCodePoints(0, symbolEntity.getEnd()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
                    spannableStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(SearchResultActivity.getIntent(context, "#" + hashtagEntity.getText())
                            );
                        }
                    }, tweet.offsetByCodePoints(0, hashtagEntity.getStart()), tweet.offsetByCodePoints(0, hashtagEntity.getEnd()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
                    spannableStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(
                                    ShowUserActivity.getIntent(context, userMentionEntity.getScreenName())
                            );
                        }
                    }, tweet.offsetByCodePoints(0, userMentionEntity.getStart()), tweet.offsetByCodePoints(0, userMentionEntity.getEnd()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                boolean hasMedia = item.getMediaEntities().length > 0;
                List<URLEntity> urlEntities = new ArrayList<>(item.getURLEntities().length + (hasMedia ? 1 : 0));
                urlEntities.addAll(Arrays.asList(item.getURLEntities()));
                if (hasMedia) {
                    urlEntities.add(item.getMediaEntities()[0]);
                }

                int tweetLength = tweet.codePointCount(0, tweet.length());
                int sp = 0;

                for (URLEntity entity : urlEntities) {
                    String url = entity.getURL();
                    String displayUrl = entity.getDisplayURL();

                    int urlLength = url.codePointCount(0, url.length());
                    int displayUrlLength = displayUrl.codePointCount(0, displayUrl.length());
                    if (entity.getStart() <= tweetLength && entity.getEnd() <= tweetLength) {
                        int dusp = displayUrlLength - urlLength;
                        spannableStringBuilder.replace(tweet.offsetByCodePoints(0, entity.getStart()) + sp, tweet.offsetByCodePoints(0, entity.getEnd()) + sp, displayUrl);
                        spannableStringBuilder.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View view) {
                                AppCustomTabsKt.launchChromeCustomTabs(context, entity.getExpandedURL());
                            }
                        }, tweet.offsetByCodePoints(0, entity.getStart()) + sp, tweet.offsetByCodePoints(0, entity.getEnd()) + sp + dusp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        sp += dusp;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }

            return spannableStringBuilder;
        }
    }

    public static CharSequence getProfileLinkedSequence(Context context, User user){

        String description = user.getDescription();

        if (GlobalApplication.clientType == Type.MASTODON){
            return MTHtmlParser.INSTANCE.convertToEntities(description, linkParserListener(context));
        } else {
            URLEntity[] urlEntities = user.getDescriptionURLEntities();

            if (urlEntities == null || urlEntities.length <= 0 || TextUtils.isEmpty(urlEntities[0].getURL()))
                return description;

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(description);
            int tweetLength = description.codePointCount(0, description.length());
            int sp = 0;

            for (URLEntity entity : urlEntities) {
                String url = entity.getURL();
                String expandedUrl = entity.getDisplayURL();

                int urlLength = url.codePointCount(0, url.length());
                int displayUrlLength = expandedUrl.codePointCount(0, expandedUrl.length());
                if (entity.getStart() <= tweetLength && entity.getEnd() <= tweetLength) {
                    int dusp = displayUrlLength - urlLength;
                    spannableStringBuilder.replace(description.offsetByCodePoints(0, entity.getStart()) + sp, description.offsetByCodePoints(0, entity.getEnd()) + sp, expandedUrl);
                    spannableStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, entity.getExpandedURL());
                        }
                    }, description.offsetByCodePoints(0, entity.getStart()) + sp, description.offsetByCodePoints(0, entity.getEnd()) + sp + dusp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                    return new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(
                                    ShowUserActivity.getIntent(context, uri.getLastPathSegment())
                            );
                        }
                    };
                } else {
                    return new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(SearchResultActivity.getIntent(context, uri.getLastPathSegment()));
                        }
                    };
                }
            } else {
                return new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        AppCustomTabsKt.launchChromeCustomTabs(context, link);
                    }
                };
            }
        };
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
