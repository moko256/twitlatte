/*
 * Copyright 2016 The twicalico authors
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
        Intent intent = null;

        if (getIntent() != null && getIntent().getData() != null){
            Uri data = getIntent().getData();
            switch (data.getScheme()){
                case "twitter":
                    switch (data.getHost()) {
                        case "post":
                            String replyId = data.getQueryParameter("in_reply_to_status_id");
                            intent = SendTweetActivity.getIntent(
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
                    }
                    break;
                case "https":
                    if (data.getQueryParameter("status") != null){
                        intent = SendTweetActivity.getIntent(this, data.getQueryParameter("status"));
                    } else if (data.getPath().matches("/.+/status/.+")){
                        intent = ShowTweetActivity.getIntent(this, Long.parseLong(data.getPathSegments().get(2)));
                    } else if (data.getPathSegments().size() == 1){
                        intent = ShowUserActivity.getIntent(this, Long.valueOf(data.getLastPathSegment()));
                    }
                    break;
            }
        }

        if (intent == null) {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
    }
}
