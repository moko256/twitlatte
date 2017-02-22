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
