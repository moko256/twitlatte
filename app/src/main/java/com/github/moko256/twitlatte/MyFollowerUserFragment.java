package com.github.moko256.twitlatte;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
public class MyFollowerUserFragment extends BaseUsersFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    @Override
    public PagableResponseList<User> getResponseList(long cursorLong) throws TwitterException {
        return Static.twitter.getFollowersList(Static.user.getScreenName(),cursorLong);
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
