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

import android.content.Context;
import android.support.v4.util.LruCache;

import java.util.Collection;

import twitter4j.User;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class UserCacheMap {

    private CachedUsersSQLiteOpenHelper diskCache;
    private LruCache<Long, User> cache=new LruCache<>(10000);

    public UserCacheMap(Context context){
        diskCache = new CachedUsersSQLiteOpenHelper(context);
    }

    public int size() {
        return cache.size();
    }

    public void add(User user) {
        cache.put(user.getId(), user);
        diskCache.addCachedUser(user);
    }

    public void addAll(Collection<? extends User> c) {
        for (User user : c) {
            add(user);
        }
    }

    public User get(long id) {
        User memoryCache = cache.get(id);
        return memoryCache != null? memoryCache: diskCache.getCachedUser(id);
    }

    public void clear() {

    }
}