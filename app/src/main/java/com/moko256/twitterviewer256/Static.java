package com.moko256.twitterviewer256;

import twitter4j.Twitter;
import twitter4j.User;

/**
 * Created by moko256 on GitHub on 2016/02/02.
 */
public class Static {
    static Twitter twitter;
    static User user;
    static String nowUserDataFile;
    static int nowUserDataFileInt;
    static final String consumerKey=BuildConfig.CONSUMER_KEY;
    static final String consumerSecret=BuildConfig.CONSUMER_SECRET;
    static String token;
    static String tokenSecret;

    public static String plusAtMark(String string){
        return "@"+string;
    }

}
