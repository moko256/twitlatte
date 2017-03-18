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

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
public class MyFollowUserFragment extends BaseUsersFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    @Override
    public PagableResponseList<User> getResponseList(long cursor) throws TwitterException {
        return GlobalApplication.twitter.getFriendsList(GlobalApplication.user.getScreenName(),cursor);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.follow;
    }

    @Override
    public int getNavigationPosition() {
        return R.id.nav_follow_and_follower;
    }

}
