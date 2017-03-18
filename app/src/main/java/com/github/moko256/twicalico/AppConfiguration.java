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

/**
 * Created by moko256 on 2017/02/04.
 *
 * @author moko256
 */

public class AppConfiguration {

    private boolean patternTweetMuteEnabled;
    private String tweetMutePattern;

    private boolean patternUserScreenNameMuteEnabled;
    private String userScreenNameMutePattern;

    private boolean patternUserNameMuteEnabled;
    private String userNameMutePattern;

    private boolean patternTweetSourceMuteEnabled;
    private String tweetSourceMutePattern;

    final public static int IMAGE_LOAD_MODE_NONE=0;
    final public static int IMAGE_LOAD_MODE_LOW=1;
    final public static int IMAGE_LOAD_MODE_NORMAL=2;
    final public static int IMAGE_LOAD_MODE_FULL=3;
    private int timelineImageLoadMode;

    public boolean isPatternTweetMuteEnabled() {
        return patternTweetMuteEnabled;
    }

    public void setPatternTweetMuteEnabled(boolean patternTweetMuteEnabled) {
        this.patternTweetMuteEnabled = patternTweetMuteEnabled;
    }

    public String getTweetMutePattern() {
        return tweetMutePattern;
    }

    public void setTweetMutePattern(String tweetMutePattern) {
        this.tweetMutePattern = tweetMutePattern;
    }

    public boolean isPatternUserScreenNameMuteEnabled() {
        return patternUserScreenNameMuteEnabled;
    }

    public void setPatternUserScreenNameMuteEnabled(boolean patternUserScreenNameMuteEnabled) {
        this.patternUserScreenNameMuteEnabled = patternUserScreenNameMuteEnabled;
    }

    public String getUserScreenNameMutePattern() {
        return userScreenNameMutePattern;
    }

    public void setUserScreenNameMutePattern(String userScreenNameMutePattern) {
        this.userScreenNameMutePattern = userScreenNameMutePattern;
    }

    public boolean isPatternUserNameMuteEnabled() {
        return patternUserNameMuteEnabled;
    }

    public void setPatternUserNameMuteEnabled(boolean patternUserNameMuteEnabled) {
        this.patternUserNameMuteEnabled = patternUserNameMuteEnabled;
    }

    public String getUserNameMutePattern() {
        return userNameMutePattern;
    }

    public void setUserNameMutePattern(String userNameMutePattern) {
        this.userNameMutePattern = userNameMutePattern;
    }

    public boolean isPatternTweetSourceMuteEnabled() {
        return patternTweetSourceMuteEnabled;
    }

    public void setPatternTweetSourceMuteEnabled(boolean patternTweetSourceMuteEnabled) {
        this.patternTweetSourceMuteEnabled = patternTweetSourceMuteEnabled;
    }

    public String getTweetSourceMutePattern() {
        return tweetSourceMutePattern;
    }

    public void setTweetSourceMutePattern(String tweetSourceMutePattern) {
        this.tweetSourceMutePattern = tweetSourceMutePattern;
    }

    public void setTimelineImageLoadMode(int timelineImageLoadMode) {
        this.timelineImageLoadMode = timelineImageLoadMode;
    }

    public int getTimelineImageLoadMode() {
        return timelineImageLoadMode;
    }
}
