/*
 * Copyright 2018 The twicalico authors
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

import com.sys1yagi.mastodon4j.api.Range;

import twitter4j.Paging;

/**
 * Created by moko256 on 2017/10/04.
 *
 * @author moko256
 */

class MTRangePagingConverter{
    static Range convert(Paging paging){
        return new Range(convertLong(paging.getMaxId()), convertLong(paging.getSinceId()), (paging.getCount() == -1)? 0: paging.getCount());
    }

    private static Long convertLong(long l){
        if (l == -1L){
            return null;
        }
        else {
            return l;
        }
    }

}
