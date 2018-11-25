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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by moko256 on 2017/04/16.
 *
 * @author moko256
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(switchIntent());
    }

    private Intent switchIntent(){

        if (GlobalApplication.twitter == null) {
            return new Intent(this, OAuthActivity.class);
        }

        Intent intent = getIntent();
        if (intent != null){

            Bundle extras = intent.getExtras();
            if (extras != null){
                try {
                    String text = null;
                    ArrayList<Uri> list = null;

                    if (extras.getCharSequence(Intent.EXTRA_TEXT) != null) {
                        CharSequence subject = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                        StringBuilder rawText = new StringBuilder();
                        if (subject != null) {
                            rawText.append(subject).append(" ");
                        }
                        rawText.append(extras.getCharSequence(Intent.EXTRA_TEXT));
                        text = rawText.toString();
                    }
                    if (extras.get(Intent.EXTRA_STREAM) != null) {
                        if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
                            list = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                        } else {
                            list = new ArrayList<>(1);
                            list.add(extras.getParcelable(Intent.EXTRA_STREAM));
                        }
                    }
                    return PostActivity.getIntent(this, -1, text, list);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            Uri data = intent.getData();
            if (data != null){
                try {
                    switch (data.getScheme()){
                        case "twitter":
                            switch (data.getHost()) {
                                case "post":
                                    String replyId = data.getQueryParameter("in_reply_to_status_id");
                                    return PostActivity.getIntent(
                                            this,
                                            replyId != null ? Long.valueOf(replyId) : -1,
                                            data.getQueryParameter("message")
                                    );
                                case "status":
                                    return ShowTweetActivity.getIntent(this, Long.valueOf(data.getQueryParameter("id")));
                                case "user":
                                    String userId = data.getQueryParameter("id");
                                    if (userId != null){
                                        return ShowUserActivity.getIntent(this, Long.valueOf(userId));
                                    } else {
                                        return ShowUserActivity.getIntent(this, data.getQueryParameter("screen_name"));
                                    }
                                default:
                                    return Intent.createChooser(new Intent(Intent.ACTION_VIEW, data), "");
                            }
                        case "https":
                            List<String> pathSegments = data.getPathSegments();
                            int size = pathSegments.size();

                            String lastPathSegment = data.getLastPathSegment();
                            switch (size){
                                case 1:
                                    switch (lastPathSegment){
                                        case "share":
                                            return generatePostIntent(data);
                                        case "search":
                                            return SearchResultActivity.getIntent(this, data.getQueryParameter("q"));
                                        default:
                                            return ShowUserActivity.getIntent(this, lastPathSegment);
                                    }
                                case 2:
                                    if (pathSegments.get(0).equals("intent") && lastPathSegment.equals("tweet")){
                                        return generatePostIntent(data);
                                    }
                                    break;

                                case 3:
                                    String s = pathSegments.get(1);
                                    if (s.equals("status") || s.equals("statuses")){
                                        return ShowTweetActivity.getIntent(this, Long.valueOf(lastPathSegment));
                                    }
                                    break;
                            }

                            if (data.getQueryParameter("status") != null){
                                return PostActivity.getIntent(this, data.getQueryParameter("status"));
                            }

                            return Intent.createChooser(new Intent(Intent.ACTION_VIEW, data), "");
                        case "web+mastodon":
                            if (data.getHost().equals("share")) {
                                return PostActivity.getIntent(
                                        this,
                                        data.getQueryParameter("text")
                                );
                            }
                            break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        return new Intent(this, MainActivity.class);
    }

    private Intent generatePostIntent(Uri data){
        StringBuilder tweet = new StringBuilder(data.getQueryParameter("text"));
        String url = data.getQueryParameter("url");
        if (url != null){
            tweet.append(" ")
                    .append(url);
        }
        String hashtagstr = data.getQueryParameter("hashtags");
        if (hashtagstr != null){
            String[] hashtags = hashtagstr.split(",");
            for (String hashtag: hashtags){
                tweet.append(" #")
                        .append(hashtag);
            }
        }
        String via = data.getQueryParameter("via");
        if (via != null){
            tweet.append(" via @")
                    .append(via);
        }
        String relatedstr = data.getQueryParameter("related");
        if (relatedstr != null){
            String[] relates = relatedstr.split(",");
            for (String related: relates){
                tweet.append(" @")
                        .append(related);
            }
        }

        return PostActivity.getIntent(
                this,
                data.getQueryParameter("in-reply-to") != null
                        ? Long.valueOf(data.getQueryParameter("in-reply-to"))
                        :-1L,
                tweet.toString()
        );
    }
}