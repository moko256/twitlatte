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

import java.util.Collection;
import java.util.HashMap;

import twitter4j.User;

/**
 * Created by moko256 on 2016/12/22.
 *
 * @author moko256
 */

public class UserCacheMap {

    private HashMap<Long, User> cache=new HashMap<>();

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void add(User user) {
        cache.put(user.getId(), user);
    }

    public void addAll(Collection<? extends User> c) {
        for (User user : c) {
            add(user);
        }
    }

    public User get(long id) {
        return cache.get(id);
    }

    public void clear() {

    }
}