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

package com.github.moko256.twicalico.model.impl.twitter;

import android.content.ContentResolver;
import android.net.Uri;

import com.github.moko256.twicalico.model.base.PostTweetModel;
import com.twitter.twittertext.TwitterTextParseResults;
import com.twitter.twittertext.TwitterTextParser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import rx.Single;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/07/22.
 *
 * @author moko256
 */

public class PostTweetModelImpl implements PostTweetModel {

    private Twitter twitter;
    private ContentResolver contentResolver;

    private long inReplyToStatusId = -1;
    private boolean possiblySensitive;
    private String tweetText;
    private List<Uri> uriList = new ArrayList<>();
    private GeoLocation location;

    private TwitterTextParseResults resultCache = null;
    private int MAX_TWEET_LENGTH = TwitterTextParser.TWITTER_TEXT_WEIGHTED_CHAR_COUNT_CONFIG.getMaxWeightedTweetLength();

    public PostTweetModelImpl(Twitter twitter, ContentResolver contentResolver){
        this.twitter = twitter;
        this.contentResolver = contentResolver;
    }

    @Override
    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    @Override
    public void setInReplyToStatusId(long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    @Override
    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    @Override
    public void setPossiblySensitive(boolean possiblySensitive) {
        this.possiblySensitive = possiblySensitive;
    }

    @Override
    public String getTweetText() {
        return tweetText;
    }

    @Override
    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
        resultCache = null;
    }

    @Override
    public int getTweetLength() {
        updateCounter();
        return resultCache.weightedLength;
    }

    @Override
    public int getMaxTweetLength() {
        return MAX_TWEET_LENGTH;
    }

    @Override
    public boolean isReply() {
        return inReplyToStatusId != -1;
    }

    @Override
    public boolean isValidTweet() {
        updateCounter();
        if (tweetText.length() == 0){
            return uriList.size() > 0;
        } else {
            return resultCache.isValid;
        }
    }

    private void updateCounter(){
        if (resultCache == null){
            resultCache = TwitterTextParser.parseTweet(tweetText);
        }
    }

    @Override
    public List<Uri> getUriList() {
        return uriList;
    }

    @Override
    public int getUriListSizeLimit() {
        return 4;
    }

    @Override
    public GeoLocation getLocation() {
        return location;
    }

    @Override
    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    @Override
    public Single<Status> postTweet() {
        return Single.create(subscriber -> {
            try {
                StatusUpdate statusUpdate = new StatusUpdate(tweetText);
                if (uriList.size() > 0) {
                    long ids[] = new long[uriList.size()];
                    for (int i = 0; i < uriList.size(); i++) {
                        Uri uri = uriList.get(i);
                        InputStream image = contentResolver.openInputStream(uri);
                        ids[i] = twitter.uploadMedia(uri.getLastPathSegment(), image).getMediaId();
                    }
                    statusUpdate.setMediaIds(ids);
                    statusUpdate.setPossiblySensitive(possiblySensitive);
                }
                if (isReply()){
                    statusUpdate.setInReplyToStatusId(inReplyToStatusId);
                }
                if (location != null){
                    statusUpdate.setLocation(location);
                }
                subscriber.onSuccess(twitter.updateStatus(statusUpdate));
            } catch (FileNotFoundException | TwitterException e){
                subscriber.onError(e);
            }
        });
    }

}
