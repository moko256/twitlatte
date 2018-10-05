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

package com.github.moko256.twitlatte.cacheMap;

import android.content.Context;

import com.github.moko256.mastodon.MTStatus;
import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.converter.StatusConverterKt;
import com.github.moko256.twitlatte.database.CachedStatusesSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.StatusObject;
import com.github.moko256.twitlatte.entity.StatusObjectKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import twitter4j.Status;
import twitter4j.User;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class StatusCacheMap {

    private final LruCache<Long, StatusObject> cache =new LruCache<>(GlobalApplication.statusCacheListLimit / 4);
    private CachedStatusesSQLiteOpenHelper diskCache;

    public void prepare(Context context, AccessToken accessToken){
        if (diskCache != null){
            diskCache.close();
        }
        if (cache.size() > 0){
            cache.evictAll();
        }
        diskCache = new CachedStatusesSQLiteOpenHelper(context, accessToken);
    }

    public int size() {
        return cache.size();
    }

    public void add(@Nullable final Status status, boolean incrementCount) {
        if (status != null && status.getRetweetedStatus() != null && status.getQuotedStatus() != null) {
            GlobalApplication.userCache.add(status.getUser());
            StatusObject cacheStatus = (status instanceof MTStatus)?
                    StatusConverterKt.convertToCommonStatus(((MTStatus) status).status):
                    StatusConverterKt.convertToCommonStatus(status);
            cache.put(status.getId(), cacheStatus);
            diskCache.addCachedStatus(cacheStatus, incrementCount);
        } else {
            addAll(Collections.singletonList(status), incrementCount);
        }
    }

    @Nullable
    StatusObject get(Long id){
        StatusObject memoryCache = cache.get(id);
        if (memoryCache == null){
            StatusObject storageCache = diskCache.getCachedStatus(id);
            if (storageCache != null) {
                cache.put(StatusObjectKt.getId(storageCache), storageCache);
            }
            return storageCache;
        } else {
            return memoryCache;
        }
    }

    public void addAll(Collection<? extends Status> c, long... excludeIncrementIds) {
        addAll(c, true, excludeIncrementIds);
    }

    private void addAll(Collection<? extends Status> c, boolean incrementCount, long... excludeIncrementIds) {
        if (c.size() > 0) {
            ArrayList<Status> statuses = new ArrayList<>(c.size() * 3);
            ArrayList<Status> repeats = new ArrayList<>(c.size());
            ArrayList<Status> quotes = new ArrayList<>(c.size());

            ArrayList<User> users = new ArrayList<>(c.size() * 3);

            for (Status status : c) {
                if (status != null) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }

                    if (status.getRetweetedStatus() != null) {
                        repeats.add(status.getRetweetedStatus());
                        if (status.getRetweetedStatus().getQuotedStatus() != null){
                            quotes.add(status.getRetweetedStatus().getQuotedStatus());
                        }
                    } else if (status.getQuotedStatus() != null){
                        quotes.add(status.getQuotedStatus());
                    }
                }
            }

            for (Status status : repeats) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }
                }
            }

            for (Status status : quotes) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                    if (!users.contains(status.getUser())){
                        users.add(status.getUser());
                    }
                }
            }

            GlobalApplication.userCache.addAll(users);

            ArrayList<StatusObject> cachedStatuses = new ArrayList<>(statuses.size());
            for (Status status : statuses){
                StatusObject cachedStatus = (status instanceof MTStatus)?
                        StatusConverterKt.convertToCommonStatus(((MTStatus) status).status):
                        StatusConverterKt.convertToCommonStatus(status);
                cache.put(status.getId(), cachedStatus);
                cachedStatuses.add(cachedStatus);
            }

            diskCache.addCachedStatuses(cachedStatuses, incrementCount, excludeIncrementIds);
        }
    }

    public void delete(List<Long> ids){
        List<Long> list = new ArrayList<>(ids.size() * 6);
        for (Long id : ids) {
            if (id != null) {
                list.add(id);
            }
        }
        List<Long> use = diskCache.getIdsInUse(list);

        HashSet<Long> remove = new HashSet<>();
        remove.addAll(list);
        remove.addAll(use);
        diskCache.deleteCachedStatuses(remove);
    }

}