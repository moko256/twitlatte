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

package com.github.moko256.twicalico.model.impl.mastodon;

import android.content.ContentResolver;
import android.net.Uri;

import com.github.moko256.mastodon.MTStatus;
import com.github.moko256.mastodon.MastodonTwitterImpl;
import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.model.base.PostTweetModel;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Media;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.GeoLocation;
import twitter4j.Status;

/**
 * Created by moko256 on 2017/10/23.
 *
 * @author moko256
 */

public class PostTweetModelImpl implements PostTweetModel {

    private MastodonClient client;
    private ContentResolver contentResolver;

    private long inReplyToStatusId = -1;
    private boolean possiblySensitive = false;
    private String tweetText = "";
    private List<Uri> uriList = new ArrayList<>();
    private GeoLocation location;

    public PostTweetModelImpl(MastodonClient client, ContentResolver contentResolver){
        this.client = client;
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
    }

    @Override
    public int getTweetLength() {
        return tweetText.codePointCount(0, tweetText.length());
    }

    @Override
    public int getMaxTweetLength() {
        return 500;
    }

    @Override
    public boolean isReply() {
        return inReplyToStatusId != -1;
    }

    @Override
    public boolean isValidTweet() {
        int tweetLength = getTweetLength();
        return tweetLength != 0 && tweetLength <= getMaxTweetLength();
    }

    @Override
    public List<Uri> getUriList() {
        return uriList;
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
        Single<Status> single = Single.create(subscriber -> {
            try {
                List<Long> ids = null;
                if (uriList.size() > 0) {
                    ids = new ArrayList<>();
                    for (int i = 0; i < uriList.size(); i++) {
                        Uri uri = uriList.get(i);
                        InputStream image = contentResolver.openInputStream(uri);
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        byte [] buffer = new byte[1024];
                        while(true) {
                            int len = image.read(buffer);
                            if(len < 0) {
                                break;
                            }
                            bout.write(buffer, 0, len);
                        }
                        Attachment attachment = new Media(((MastodonTwitterImpl) GlobalApplication.twitter).client)
                                .postMedia(
                                        MultipartBody.Part.createFormData(
                                                "file",
                                                uri.getLastPathSegment(),
                                                RequestBody.create(MediaType.parse(contentResolver.getType(uri)), bout.toByteArray())
                                        ))
                                .execute();
                        ids.add(attachment.getId());
                    }
                }
                subscriber.onSuccess(new MTStatus(new Statuses(client).postStatus(
                        tweetText,
                        inReplyToStatusId == -1? null: inReplyToStatusId,
                        ids,
                        possiblySensitive,
                        null,
                        com.sys1yagi.mastodon4j.api.entity.Status.Visibility.Public
                ).execute()));
            } catch (IOException | Mastodon4jRequestException e){
                subscriber.onError(e);
            }
        });
        return single
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
