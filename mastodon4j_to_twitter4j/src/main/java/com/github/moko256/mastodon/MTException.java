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

package com.github.moko256.mastodon;

import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/10/04.
 *
 * @author moko256
 */

public class MTException extends TwitterException {
    private Mastodon4jRequestException exception;

    public MTException(Mastodon4jRequestException e){
        super(e);
        exception = e;
    }

    @Override
    public String getErrorMessage() {
        return convertErrorString(exception);
    }

    public static String convertErrorString(Mastodon4jRequestException exception){
        String message;
        if (exception.isErrorResponse()) {
            try {
                message = exception.getResponse().body().string();
            } catch (Exception e1){
                message = "Unknown";
            }
        } else {
            message = exception.getMessage();
        }
        return message;
    }
}
