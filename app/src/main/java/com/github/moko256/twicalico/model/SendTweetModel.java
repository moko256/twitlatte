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

package com.github.moko256.twicalico.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.twitter.Validator;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/03/31.
 *
 * @author moko256
 */

public class SendTweetModel {

    private Twitter twitter;
    private ContentResolver contentResolver;

    private long inReplyToStatusId = -1;
    private String tweetText;
    private List<Uri> uriList;

    private Validator validator = new Validator();

    public SendTweetModel(Twitter twitter, ContentResolver contentResolver){
        this.twitter = twitter;
        this.contentResolver = contentResolver;
    }

    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public boolean isReply() {
        return inReplyToStatusId != -1;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public int getTweetLength(){
      return validator.getTweetLength(tweetText);
    }

    public boolean isValidTweet(){
        return validator.isValidTweet(tweetText);
    }

    public List<Uri> getUriList() {
        return uriList;
    }

    public void setUriList(List<Uri> uriList) {
        this.uriList = uriList;
    }

    public Single<Status> postTweet(){
        Single<Status> single = Single.create(subscriber -> {
            if (isValidTweet()){
                try {
                    StatusUpdate statusUpdate = new StatusUpdate(tweetText);
                    if (uriList != null && uriList.size() > 0) {
                        long ids[] = new long[uriList.size()];
                        for (int i = 0; i < uriList.size(); i++) {
                            Uri uri = uriList.get(i);
                            InputStream image = contentResolver.openInputStream(uri);
                            ids[i] = twitter.uploadMedia(uri.getLastPathSegment(), image).getMediaId();
                        }
                        statusUpdate.setMediaIds(ids);
                    }
                    if (isReply()){
                        statusUpdate.setInReplyToStatusId(inReplyToStatusId);
                    }
                    subscriber.onSuccess(twitter.updateStatus(statusUpdate));
                } catch (FileNotFoundException | TwitterException e){
                    subscriber.onError(e);
                }
            } else {
                subscriber.onError(new InvalidTextException());
            }
        });
        return single
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static class InvalidTextException extends Throwable {
        InvalidTextException(){
            super("Invalid length of tweet text");
        }
    }
}
