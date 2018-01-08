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

package com.github.moko256.twicalico.cacheMap;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.database.CachedUsersSQLiteOpenHelper;

import java.util.Collection;
import java.util.HashSet;

import twitter4j.User;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class UserCacheMap {

    private CachedUsersSQLiteOpenHelper diskCache;
    private LruCache<Long, User> cache=new LruCache<>(GlobalApplication.statusCacheListLimit / 4);

    public UserCacheMap(Context context, long userId, boolean isTwitter){
        diskCache = new CachedUsersSQLiteOpenHelper(context, userId, isTwitter);
    }

    public int size() {
        return cache.size();
    }

    public void add(User user) {
        if (user != null) {
            cache.put(user.getId(), user);
            diskCache.addCachedUser(user);
        }
    }

    public void addAll(Collection<? extends User> c) {
        if (c.size() > 0) {
            HashSet<User> hashSet = new HashSet<>(c);
            for (User user : hashSet) {
                if (user != null) {
                    cache.put(user.getId(), user);
                }
            }
            diskCache.addCachedUsers(hashSet);
        }
    }

    public User get(long id) {
        User memoryCache = cache.get(id);
        if (memoryCache == null){
            User storageCache = diskCache.getCachedUser(id);
            if (storageCache != null){
                cache.put(storageCache.getId(), storageCache);
            }
            return storageCache;
        } else {
            return memoryCache;
        }
    }
}