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

import android.os.Bundle;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/03/04.
 *
 * @author moko256
 */
public class UserLikeFragment extends BaseTweetListFragment implements ToolbarTitleInterface {

    long userId = -1;

    @Override
    protected void onInitializeList() {
        if (userId == -1){
            userId = getArguments().getLong("userId", -1);
        }
        super.onInitializeList();
    }

    public static UserLikeFragment newInstance(long userId){
        UserLikeFragment result = new UserLikeFragment();
        Bundle args = new Bundle();
        args.putLong("userId", userId);
        result.setArguments(args);
        return result;
    }

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return GlobalApplication.twitter.getFavorites(userId, paging);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.like;
    }
}