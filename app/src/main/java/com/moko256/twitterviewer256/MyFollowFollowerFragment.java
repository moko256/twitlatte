package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
public class MyFollowFollowerFragment extends Fragment implements ToolbarTitleInterface,NavigationPositionInterface,UseTabsInterface {
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view=inflater.inflate(R.layout.fragment_follow_follower, null);

        viewPager=(ViewPager) view.findViewById(R.id.follow_follower_pager);
        viewPager.setAdapter(new FollowFollowerTabsPagerAdapter(getChildFragmentManager(),getContext()));

        return view;
    }

    @Override
    public int getTitleResourceId() {
        return R.string.follow_and_follower;
    }

    @Override
    public int getNavigationPosition() {
        return R.id.nav_follow_and_follower;
    }

    @Override
    public ViewPager getTabsViewPager() {
        return viewPager;
    }
}
