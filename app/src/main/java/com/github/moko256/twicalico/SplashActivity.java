/*
 * Copyright 2017 The twicalico authors
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

/**
 * Created by moko256 on 2017/04/16.
 *
 * @author moko256
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = null;

        if (getIntent() != null){
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.getCharSequence(Intent.EXTRA_TEXT) != null){
                CharSequence subject = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                StringBuilder text = new StringBuilder();
                if (subject != null){
                    text.append(subject).append(" ");
                }
                text.append(extras.getCharSequence(Intent.EXTRA_TEXT));
                intent = PostActivity.getIntent(
                        this,
                        -1,
                        text.toString()
                );
            } else if (getIntent().getData() != null){
                Uri data = getIntent().getData();
                switch (data.getScheme()){
                    case "twitter":
                        switch (data.getHost()) {
                            case "post":
                                String replyId = data.getQueryParameter("in_reply_to_status_id");
                                intent = PostActivity.getIntent(
                                        this,
                                        replyId != null ? Long.valueOf(replyId) : -1,
                                        data.getQueryParameter("message")
                                );
                                break;
                            case "status":
                                intent = ShowTweetActivity.getIntent(this, Long.parseLong(data.getQueryParameter("id")));
                                break;
                            case "user":
                                String userId = data.getQueryParameter("id");
                                if (userId != null){
                                    intent = ShowUserActivity.getIntent(this, Long.valueOf(userId));
                                } else {
                                    intent = ShowUserActivity.getIntent(this, data.getQueryParameter("screen_name"));
                                }
                                break;
                            default:
                                intent = Intent.createChooser(new Intent(Intent.ACTION_VIEW, data), "");
                        }
                        break;
                    case "https":
                        if (data.getPath().equals("/share") || data.getPath().equals("/intent/tweet")){
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

                            intent = PostActivity.getIntent(
                                    this,
                                    data.getQueryParameter("in-reply-to") != null
                                            ? Long.valueOf(data.getQueryParameter("in-reply-to"))
                                            :-1L,
                                    tweet.toString()
                                    );
                        } else if (data.getPath().equals("/search")){
                            intent = SearchResultActivity.getIntent(this, data.getQueryParameter("q"));
                        } else if (data.getPath().matches("/.+/status(es)*/.+")){
                            intent = ShowTweetActivity.getIntent(this, Long.parseLong(data.getPathSegments().get(2)));
                        } else if (data.getPathSegments().size() == 1){
                            intent = ShowUserActivity.getIntent(this, data.getLastPathSegment());
                        } else if (data.getQueryParameter("status") != null){
                            intent = PostActivity.getIntent(this, data.getQueryParameter("status"));
                        } else {
                            intent = Intent.createChooser(new Intent(Intent.ACTION_VIEW, data), "");
                        }
                        break;
                    case "web+mastodon":
                        if (data.getHost().equals("share")) {
                            intent = PostActivity.getIntent(
                                    this,
                                    -1,
                                    data.getQueryParameter("text")
                            );
                        }
                        break;
                }
            }
        }

        if (GlobalApplication.twitter == null) {
            intent = new Intent(this, OAuthActivity.class);
        }

        if (intent == null) {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
    }
}
