/*
 * Copyright 2015-2018 The twicalico authors
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

package com.github.moko256.twicalico;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

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
            return  new Intent(this, OAuthActivity.class);
        }

        if (getIntent() != null){

            Bundle extras = getIntent().getExtras();
            if (extras != null){
                if (extras.getCharSequence(Intent.EXTRA_TEXT) != null) {
                    CharSequence subject = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                    StringBuilder text = new StringBuilder();
                    if (subject != null) {
                        text.append(subject).append(" ");
                    }
                    text.append(extras.getCharSequence(Intent.EXTRA_TEXT));
                    return PostActivity.getIntent(
                            this,
                            -1,
                            text.toString()
                    );
                } else if (extras.get(Intent.EXTRA_STREAM) != null) {
                    ArrayList<Uri> list = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                    return PostActivity.getIntent(this, list);
                }
            }

            Uri data = getIntent().getData();
            if (data != null){
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
                                return ShowTweetActivity.getIntent(this, Long.parseLong(data.getQueryParameter("id")));
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

                        if ((size == 1 && lastPathSegment.equals("share"))
                                || (size == 2 && pathSegments.get(0).equals("intent") && lastPathSegment.equals("tweet"))){
                            StringBuilder tweet = new StringBuilder(data.getQueryParameter("text"));
                            if (data.getQueryParameter("url") != null){
                                tweet.append(" ")
                                        .append(data.getQueryParameter("url"));
                            }
                            if (data.getQueryParameter("hashtags") != null){
                                String[] hashtags = data.getQueryParameter("hashtags").split(",");
                                for (String hashtag: hashtags){
                                    tweet.append(" #")
                                            .append(hashtag);
                                }
                            }
                            if (data.getQueryParameter("via") != null){
                                tweet.append(" via @")
                                        .append(data.getQueryParameter("via"));
                            }
                            if (data.getQueryParameter("related") != null){
                                String[] relates = data.getQueryParameter("related").split(",");
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
                        } else if (size == 1 && lastPathSegment.equals("search")){
                            return SearchResultActivity.getIntent(this, data.getQueryParameter("q"));
                        } else if (size == 3 && (pathSegments.get(1).equals("status") || pathSegments.get(1).equals("statuses"))){
                            return ShowTweetActivity.getIntent(this, Long.parseLong(pathSegments.get(2)));
                        } else if (size == 1){
                            return ShowUserActivity.getIntent(this, lastPathSegment);
                        } else if (data.getQueryParameter("status") != null){
                            return PostActivity.getIntent(this, data.getQueryParameter("status"));
                        } else {
                            return Intent.createChooser(new Intent(Intent.ACTION_VIEW, data), "");
                        }
                    case "web+mastodon":
                        if (data.getHost().equals("share")) {
                            return PostActivity.getIntent(
                                    this,
                                    -1,
                                    data.getQueryParameter("text")
                            );
                        }
                        break;
                }
            }
        }

        return new Intent(this, MainActivity.class);
    }
}