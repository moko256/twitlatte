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

package com.github.moko256.twicalico;

import android.os.Bundle;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
public class UserFollowersFragment extends BaseUsersFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    long userId = -1;

    @Override
    protected void onInitializeList() {
        if (userId == -1){
            userId = getArguments().getLong("userId", -1);
        }
        super.onInitializeList();
    }

    public static UserFollowersFragment newInstance(long userId){
        UserFollowersFragment result = new UserFollowersFragment();
        Bundle args = new Bundle();
        args.putLong("userId", userId);
        result.setArguments(args);
        return result;
    }

    @Override
    public PagableResponseList<User> getResponseList(long cursorLong) throws TwitterException {
        return GlobalApplication.twitter.getFollowersList(userId, cursorLong);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.follower;
    }

    @Override
    public int getNavigationPosition() {
        return R.id.nav_follow_and_follower;
    }

}
