package com.moko256.twitterviewer256;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on GitHub on 2016/03/29.
 */
public class MyFollowUserFragment extends BaseUserListFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    @Override
    public PagableResponseList<User> getResponseList(long cursor) throws TwitterException {
        return Static.twitter.getFriendsList(Static.user.getScreenName(),cursor);
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
