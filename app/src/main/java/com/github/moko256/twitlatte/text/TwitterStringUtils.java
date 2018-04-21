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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.github.moko256.mastodon.MTException;
import com.github.moko256.twitlatte.GlideApp;
import com.github.moko256.twitlatte.GlideRequests;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.SearchResultActivity;
import com.github.moko256.twitlatte.ShowUserActivity;
import com.github.moko256.twitlatte.cacheMap.StatusCacheMap;
import com.github.moko256.twitlatte.entity.Emoji;
import com.github.moko256.twitlatte.entity.Type;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import net.ellerton.japng.android.api.PngAndroid;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final static Pattern containsEmoji;

    static {
        containsEmoji = Pattern.compile(":([a-zA-Z0-9_]{2,}):");
    }

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

    @SuppressLint("StaticFieldLeak")
    public static void setLinkedSequenceTo(Status item, TextView textView){

        Context context = textView.getContext();

        String tweet = item.getText();

        if (GlobalApplication.clientType == Type.MASTODON){
            Spanned html = Html.fromHtml(tweet);
            int length = html.length();
            // Trim unless \n\n made by fromHtml() after post
            if (length == 3
                    && item.getMediaEntities().length > 0
                    && html.charAt(0) == ".".charAt(0)
                    ){
                // If post has media only, context of post from Mastodon is "."
                textView.setText("");
                return;
            } else if (length >= 2) {
                html = (Spanned) html.subSequence(0, length - 2);
            }
            SpannableStringBuilder builder = convertUrlSpanToCustomTabs(html, context);
            textView.setText(builder);

            List<Emoji> list = ((StatusCacheMap.CachedStatus) item).getEmojis();

            if (list != null){
                Matcher matcher = containsEmoji.matcher(builder);
                boolean matches = matcher.matches();

                int imageSize;

                if (matches) {
                    imageSize = (int) Math.floor((textView.getTextSize() * 1.15) * 2.0);
                } else {
                    imageSize = (int) Math.floor(textView.getTextSize() * 1.15);
                }

                new AsyncTask<Void, Void, Map<String, Drawable>>(){
                    @Override
                    protected Map<String, Drawable> doInBackground(Void... params) {
                        Map<String, Drawable> map = new ArrayMap<>();

                        GlideRequests glideRequests = GlideApp.with(context);
                        for (Emoji emoji : list){
                            try {
                                Drawable value;
                                try {
                                    FileInputStream inputStream = new FileInputStream(glideRequests.asFile().load(emoji.getUrl()).submit().get());
                                    value =  PngAndroid.readDrawable(textView.getContext(), inputStream);
                                    inputStream.close();
                                } catch (Throwable e){
                                    e.printStackTrace();
                                    value = glideRequests.load(emoji.getUrl()).submit().get();
                                }
                                value.setBounds(0, 0, imageSize, imageSize);
                                map.put(emoji.getShortCode(), value);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        return map;
                    }

                    @Override
                    protected void onPostExecute(Map<String, Drawable> map) {
                        if (TextUtils.equals(builder, textView.getText())) {
                            boolean found = matches || matcher.find();
                            while (found){
                                String shortCode = matcher.group(1);
                                Drawable drawable = map.get(shortCode);
                                if (drawable != null) {
                                    builder.setSpan(
                                            new ImageSpan(drawable),
                                            matcher.start(), matcher.end(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    );
                                    if (drawable instanceof Animatable){
                                        Handler handler = new Handler();
                                        drawable.setCallback(new Drawable.Callback() {
                                            @Override
                                            public void invalidateDrawable(@NonNull Drawable who) {
                                                if (TextUtils.equals(builder, textView.getText())){
                                                    textView.invalidate();
                                                } else {
                                                    ((Animatable) who).stop();
                                                }
                                            }

                                            @Override
                                            public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                                                who.invalidateSelf();
                                                handler.postAtTime(what, when);
                                            }

                                            @Override
                                            public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                                                handler.removeCallbacks(what);
                                            }
                                        });
                                        ((Animatable) drawable).start();
                                    }
                                }
                                found = matcher.find();
                            }
                            textView.setText(builder);
                        }
                    }
                }.execute();
            }

            return;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(tweet);

        for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    context.startActivity(SearchResultActivity.getIntent(context, symbolEntity.getText()));
                }
            }, tweet.offsetByCodePoints(0,symbolEntity.getStart()), tweet.offsetByCodePoints(0,symbolEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    context.startActivity(SearchResultActivity.getIntent(context, "#"+hashtagEntity.getText())
                    );
                }
            }, tweet.offsetByCodePoints(0,hashtagEntity.getStart()), tweet.offsetByCodePoints(0,hashtagEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    context.startActivity(
                            ShowUserActivity.getIntent(context, userMentionEntity.getScreenName())
                    );
                }
            }, tweet.offsetByCodePoints(0,userMentionEntity.getStart()), tweet.offsetByCodePoints(0,userMentionEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        boolean hasMedia = item.getMediaEntities().length > 0;
        List<URLEntity> urlEntities = new ArrayList<>(item.getURLEntities().length + (hasMedia? 1: 0));
        urlEntities.addAll(Arrays.asList(item.getURLEntities()));
        if (hasMedia){
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
                }, tweet.offsetByCodePoints(0, entity.getStart()) + sp, tweet.offsetByCodePoints(0, entity.getEnd()) + sp + dusp, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                sp += dusp;
            }
        }

        textView.setText(spannableStringBuilder);

    }

    public static CharSequence getProfileLinkedSequence(Context context, User user){

        String description = user.getDescription();

        if (GlobalApplication.clientType == Type.MASTODON){
            Spanned spanned = Html.fromHtml(description);
            // Trim unless \n\n made by fromHtml() after post
            int length = spanned.length();
            if (length >= 2) {
                spanned = (Spanned) spanned.subSequence(0, length - 2);
            }
            return convertUrlSpanToCustomTabs(spanned, context);
        }

        URLEntity[] urlEntities = user.getDescriptionURLEntities();

        if (urlEntities == null || urlEntities.length <= 0 || TextUtils.isEmpty(urlEntities[0].getURL())) return description;

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
                }, description.offsetByCodePoints(0, entity.getStart()) + sp, description.offsetByCodePoints(0, entity.getEnd()) + sp + dusp, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                sp += dusp;
            }
        }

        return spannableStringBuilder;
    }

    public static SpannableStringBuilder convertUrlSpanToCustomTabs(Spanned spanned, Context context){
        SpannableStringBuilder builder = SpannableStringBuilder.valueOf(spanned);
        URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int spanStart = builder.getSpanStart(span);
            int spanEnd = builder.getSpanEnd(span);

            ClickableSpan span1;
            String firstChar = String.valueOf(builder.subSequence(spanStart, spanStart + 1));
            switch (firstChar) {
                case "#":
                    span1 = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            context.startActivity(
                                    SearchResultActivity.getIntent(context, String.valueOf(builder.subSequence(spanStart + 1, spanEnd)))
                            );
                        }
                    };
                    break;
                case "@":
                    span1 = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            context.startActivity(
                                    ShowUserActivity.getIntent(context, String.valueOf(builder.subSequence(spanStart + 1, spanEnd)))
                            );
                        }
                    };
                    break;
                default:
                    span1 = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            AppCustomTabsKt.launchChromeCustomTabs(context, span.getURL());
                        }
                    };
                    break;
            }

            builder.removeSpan(span);
            builder.setSpan(span1, spanStart, spanEnd, builder.getSpanFlags(span));
        }
        return builder;
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

    public static void plusAndSetMarks(String name, TextView textView, boolean isLocked, boolean isAuthorized){
        if (!isLocked && !isAuthorized){
            textView.setText(name);
            return;
        }

        SpannableStringBuilder result = new SpannableStringBuilder(name);

        Context context = textView.getContext();

        int textSize = Math.round(textView.getLineHeight());
        int left = Math.round(4 * context.getResources().getDisplayMetrics().density);

        if (isLocked){
            int length = result.length();
            result.append("\uFFFC");

            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_lock_black_24dp);
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

            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_check_circle_black_24dp);
            drawable.setBounds(left, 0, textSize + left, textSize);

            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.color_accent));

            result.setSpan(new ImageSpan(
                    drawable
            ), length, length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(result);
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

    public static enum Action{
        LIKE,
        UNLIKE,
        REPEAT,
        UNREPEAT
    }

}
