package com.github.moko256.twicalico;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2016/03/23.
 *
 * @author moko256
 */
public class TimeLineFragment extends BaseTweetListFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return GlobalApplication.twitter.getHomeTimeline(paging);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.timeline;
    }

    @Override
    public int getNavigationPosition() {
        return R.id.nav_timeline;
    }

}
