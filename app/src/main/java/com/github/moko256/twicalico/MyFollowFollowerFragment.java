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
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(new FollowFollowerTabsPagerAdapter(getChildFragmentManager(),getContext(),GlobalApplication.userId));

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
