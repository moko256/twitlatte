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

package com.github.moko256.twicalico.text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.github.moko256.twicalico.GlideApp;
import com.github.moko256.twicalico.GlideRequests;
import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.SearchResultActivity;
import com.github.moko256.twicalico.ShowUserActivity;
import com.github.moko256.twicalico.cacheMap.StatusCacheMap;
import com.github.moko256.twicalico.entity.Emoji;
import com.github.moko256.twicalico.entity.Type;
import com.github.moko256.twicalico.intent.AppCustomTabsKt;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
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
        containsEmoji = Pattern.compile(":([[A-z]_]+):");
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
        if (isNegative) num *= -1;

        float k = num / 1000;
        if (k < 1) return (isNegative? "-": "") + String.valueOf(num);

        float m = k / 1000;
        if (m < 1) return (isNegative? "-": "") + String.valueOf(Math.round(k)) + "K";

        float g = m / 1000;
        if (g < 1) return (isNegative? "-": "") + String.valueOf(Math.round(m)) + "M";

        return (isNegative? "-": "") + String.valueOf(Math.round(g)) + "G";
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
            try {
                return ((Mastodon4jRequestException) e).getResponse().body().string();
            } catch (IOException e1) {
                e1.printStackTrace();
                return e.getMessage();
            }
        } else {
            return e.getMessage();
        }
    }

    public static CharSequence getStatusTextSequence(Status item){

        String postText = item.getText();
        StringBuilder builder = new StringBuilder(postText);

        URLEntity[] urlEntities = item.getURLEntities();

        int tweetLength = postText.codePointCount(0, postText.length());
        int sp = 0;

        for (URLEntity entity : urlEntities) {
            String url = entity.getURL();
            String displayUrl = entity.getDisplayURL();

            int urlLength = url.codePointCount(0, url.length());
            int displayUrlLength = displayUrl.codePointCount(0, displayUrl.length());
            if (entity.getStart() <= tweetLength && entity.getEnd() <= tweetLength) {
                int dusp = displayUrlLength - urlLength;
                builder.replace(postText.offsetByCodePoints(0,entity.getStart()) + sp, postText.offsetByCodePoints(0,entity.getEnd()) + sp, displayUrl);

                sp+=dusp;
            }
        }

        MediaEntity[] mediaEntities = item.getMediaEntities();
        if (mediaEntities.length > 0){
            MediaEntity mediaEntity = mediaEntities[0];
            int result = builder.indexOf(mediaEntity.getURL(), builder.offsetByCodePoints(0, mediaEntity.getStart()));
            if (result != -1){
                builder.replace(result, result + mediaEntity.getURL().length(), "");
            }
        }

        return builder;
    }

    public static void setLinkedSequenceTo(Status item, TextView textView){

        Context context = textView.getContext();

        String tweet = item.getText();

        if (GlobalApplication.clientType == Type.MASTODON){
            Spanned html = Html.fromHtml(tweet);
            if (html.length() >= 2) {
                html = (Spanned) html.subSequence(0, html.length() - 2);
            }
            SpannableStringBuilder builder = convertUrlSpanToCustomTabs(html, context);
            textView.setText(builder);

            Matcher matcher = containsEmoji.matcher(builder);

            int imageSize;
            if (matcher.matches()) {
                imageSize = (int) Math.floor((textView.getTextSize() * 1.15) * 2.0);
            } else {
                imageSize = (int) Math.floor(textView.getTextSize() * 1.15);
            }
            matcher.reset();

            List<Emoji> list = ((StatusCacheMap.CachedStatus) item).getEmojis();

            if (list != null){
                new AsyncTask<Void, Void, Map<String, Drawable>>(){
                    @Override
                    protected Map<String, Drawable> doInBackground(Void... params) {
                        Map<String, Drawable> map = new ArrayMap<>();

                        GlideRequests glideRequests = GlideApp.with(context);
                        for (Emoji emoji : list){
                            try {
                                Drawable value = glideRequests.load(emoji.getUrl()).submit().get();
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
                        if (builder == textView.getText()) {
                            while (matcher.find()){
                                String shortCode = matcher.group(1);
                                Drawable drawable = map.get(shortCode);
                                if (drawable != null) {
                                    builder.setSpan(
                                            new ImageSpan(
                                                    drawable,
                                                    DynamicDrawableSpan.ALIGN_BASELINE
                                            ),
                                            matcher.start(), matcher.end(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    );
                                }
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

        List<URLEntity> urlEntities = new ArrayList<>(item.getURLEntities().length + item.getMediaEntities().length);
        urlEntities.addAll(Arrays.asList(item.getURLEntities()));
        urlEntities.addAll(Arrays.asList(item.getMediaEntities()));

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
            return convertUrlSpanToCustomTabs(Html.fromHtml(description), context);
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

    public static String convertLargeImageUrl(String baseUrl){
        return (GlobalApplication.clientType == Type.TWITTER)?
                baseUrl + ":large":
                baseUrl;
    }

    public static String convertSmallImageUrl(String baseUrl){
        return GlobalApplication.clientType == Type.MASTODON?
                baseUrl.replace("original", "small"):
                baseUrl + ":small";
    }

}
