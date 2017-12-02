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

package com.github.moko256.mastodon;

import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.entity.Account;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterResponse;
import twitter4j.User;

/**
 * Created by moko256 on 2017/10/23.
 *
 * @author moko256
 */

public class MTPagableResponseList<E extends TwitterResponse> extends ArrayList<E> implements PagableResponseList<E> {
    Long previous;
    Long next;

    MTPagableResponseList(Pageable p){
        super();
        next = p.getLink().getMaxId();
        previous = p.getLink().getSinceId();
    }

    static MTPagableResponseList<User> convert(Pageable<Account> p){
        MTPagableResponseList<User> users = new MTPagableResponseList<>(p);
        for (Account a : p.getPart()) {
            users.add(new MTUser(a));
        }
        return users;
    }

    @Override
    public boolean hasPrevious() {
        return previous != null;
    }

    @Override
    public long getPreviousCursor() {
        return hasPrevious()? previous: -1;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public long getNextCursor() {
        return hasNext()? next: -1;
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }
}
