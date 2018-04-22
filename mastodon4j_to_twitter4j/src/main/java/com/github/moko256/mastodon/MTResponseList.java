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

package com.github.moko256.mastodon;

import com.sys1yagi.mastodon4j.api.Pageable;

import java.util.ArrayList;
import java.util.List;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * Created by moko256 on 2017/10/04.
 *
 * @author moko256
 */

public class MTResponseList<T> extends ArrayList<T> implements ResponseList<T> {

    MTResponseList(){
        super();
    }
    MTResponseList(int size){
        super(size);
    }

    public static ResponseList<Status> convert(Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statusPageable){
        List<com.sys1yagi.mastodon4j.api.entity.Status> part = statusPageable.getPart();
        ResponseList<Status> responseList = new MTResponseList<>(part.size());
        for (com.sys1yagi.mastodon4j.api.entity.Status status : part) {
            responseList.add(new MTStatus(status));
        }
        return responseList;
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
