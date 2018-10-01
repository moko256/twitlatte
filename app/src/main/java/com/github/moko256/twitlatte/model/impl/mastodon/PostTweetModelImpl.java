/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte.model.impl.mastodon;

import android.content.ContentResolver;
import android.net.Uri;

import com.github.moko256.mastodon.MastodonTwitterImpl;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.model.base.PostTweetModel;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Media;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import twitter4j.AlternativeHttpClientImpl;
import twitter4j.GeoLocation;

/**
 * Created by moko256 on 2017/10/23.
 *
 * @author moko256
 */

public class PostTweetModelImpl implements PostTweetModel {

    private final MastodonClient client;
    private final ContentResolver contentResolver;

    private long inReplyToStatusId = -1;
    private boolean possiblySensitive = false;
    private String tweetText = "";
    private String contentWarning = "";
    private final ArrayList<Uri> uriList = new ArrayList<>();
    private GeoLocation location;
    private String visibility = "Public";

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
    public String getContentWarning() {
        return contentWarning;
    }

    @Override
    public void setContentWarning(String contentWarning) {
        this.contentWarning = contentWarning;
    }

    @Override
    public int getTweetLength() {
        String s = contentWarning + tweetText;
        return s.codePointCount(0, s.length());
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
    public String getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public Completable postTweet() {
        return Completable.create(subscriber -> {
            try {
                ArrayList<Long> ids = null;
                if (uriList.size() > 0) {
                    ids = new ArrayList<>(uriList.size());
                    for (Uri uri: uriList) {
                        InputStream image = contentResolver.openInputStream(uri);
                        String name = uri.getLastPathSegment();
                        Attachment attachment = new Media(((MastodonTwitterImpl) GlobalApplication.twitter).client)
                                .postMedia(
                                        MultipartBody.Part.createFormData(
                                                "file",
                                                name,
                                                AlternativeHttpClientImpl.createInputStreamRequestBody(
                                                        MediaType.parse(Objects.requireNonNull(
                                                                contentResolver.getType(uri))
                                                        ),
                                                        image
                                                )
                                        ), null, null)
                                .execute();
                        ids.add(attachment.getId());
                    }
                }
                new Statuses(client).postStatus(
                        tweetText,
                        inReplyToStatusId == -1? null: inReplyToStatusId,
                        ids,
                        possiblySensitive,
                        contentWarning.isEmpty()? null: contentWarning,
                        com.sys1yagi.mastodon4j.api.entity.Status.Visibility.valueOf(visibility),
                        null
                ).execute();
                subscriber.onComplete();
            } catch (NullPointerException | Mastodon4jRequestException e){
                subscriber.tryOnError(e);
            }
        });
    }

}
