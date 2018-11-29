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

import com.github.moko256.twitlatte.GlobalApplicationKt;
import com.github.moko256.twitlatte.database.CachedStatusesSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.entity.Post;
import com.github.moko256.twitlatte.entity.Repeat;
import com.github.moko256.twitlatte.entity.Status;
import com.github.moko256.twitlatte.entity.StatusObject;
import com.github.moko256.twitlatte.entity.StatusObjectKt;
import com.github.moko256.twitlatte.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class StatusCacheMap {

    private final LruCache<Long, StatusObject> cache =new LruCache<>(GlobalApplicationKt.LIMIT_OF_SIZE_OF_STATUSES_LIST / 4);
    private CachedStatusesSQLiteOpenHelper diskCache;
    private UserCacheMap userCache;

    public void prepare(Context context, AccessToken accessToken, UserCacheMap userCache){
        if (diskCache != null){
            diskCache.close();
        }
        if (cache.size() > 0){
            cache.evictAll();
        }
        diskCache = new CachedStatusesSQLiteOpenHelper(context, accessToken);
        this.userCache = userCache;
    }

    public void close() {
        if (diskCache != null) {
            diskCache.close();
            diskCache = null;
        }
        if (cache.size() > 0) {
            cache.evictAll();
        }
    }

    public int size() {
        return cache.size();
    }

    public void add(@Nullable final Post status, boolean incrementCount) {
        if (status != null && status.getRepeat() == null && status.getQuotedRepeatingStatus() == null) {
            userCache.add(status.getUser());
            cache.put(status.getStatus().getId(), status.getStatus());
            diskCache.addCachedStatus(status.getStatus(), incrementCount);
        } else {
            addAll(Collections.singletonList(status), incrementCount);
        }
    }

    @Nullable
    StatusObject get(Long id){
        StatusObject memoryCache = cache.get(id);
        if (memoryCache == null){
            try {
                StatusObject storageCache = diskCache.getCachedStatus(id);
                if (storageCache != null) {
                    cache.put(StatusObjectKt.getId(storageCache), storageCache);
                }
                return storageCache;
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return memoryCache;
        }
    }

    public void addAll(Collection<Post> c, long... excludeIncrementIds) {
        addAll(c, true, excludeIncrementIds);
    }

    private void addAll(Collection<Post> c, boolean incrementCount, long... excludeIncrementIds) {
        if (c.size() > 0) {
            ArrayList<StatusObject> statuses = new ArrayList<>(c.size() * 3);
            ArrayList<Repeat> repeats = new ArrayList<>(c.size());
            ArrayList<Status> quotes = new ArrayList<>(c.size());

            ArrayList<User> users = new ArrayList<>(c.size() * 3);

            for (Post status : c) {

                if (status.getStatus() != null && !statuses.contains(status.getStatus())){
                    statuses.add(status.getStatus());
                }

                if (status.getUser() != null && !users.contains(status.getUser())){
                    users.add(status.getUser());
                }

                if (status.getRepeatedUser() != null && !users.contains(status.getRepeatedUser())){
                    users.add(status.getRepeatedUser());
                }

                if (status.getQuotedRepeatingUser() != null && !users.contains(status.getQuotedRepeatingUser())){
                    users.add(status.getQuotedRepeatingUser());
                }

                if (status.getRepeat() != null) {
                    repeats.add(status.getRepeat());
                }

                if (status.getQuotedRepeatingStatus() != null){
                    quotes.add(status.getQuotedRepeatingStatus());
                }
            }

            for (Status status : quotes) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                }
            }

            statuses.addAll(repeats);

            userCache.addAll(users);

            for (StatusObject status : statuses){
                cache.put(StatusObjectKt.getId(status), status);
            }

            diskCache.addCachedStatuses(statuses, incrementCount, excludeIncrementIds);
        }
    }

    public void delete(List<Long> ids){
        List<Long> list = new ArrayList<>(ids.size());
        for (Long id : ids) {
            if (id != null) {
                list.add(id);
            }
        }
        List<Long> use = diskCache.getIdsInUse(list);

        HashSet<Long> remove = new HashSet<>(list.size() + use.size());
        remove.addAll(list);
        remove.addAll(use);
        diskCache.deleteCachedStatuses(remove);
    }

}