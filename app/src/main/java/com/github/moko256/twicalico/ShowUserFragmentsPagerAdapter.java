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

package com.github.moko256.twicalico;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.moko256.twicalico.entity.Type;
import com.github.moko256.twicalico.widget.FragmentPagerAdapter;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class ShowUserFragmentsPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private long userId;
    private boolean isShowFavoriteTab;

    ShowUserFragmentsPagerAdapter(FragmentManager fm, Context context, long userId) {
        super(fm);

        this.context = context;
        this.userId = userId;
        isShowFavoriteTab = ! (GlobalApplication.clientType == Type.MASTODON && userId != GlobalApplication.userId);
    }

    @Override
    public Fragment getItem(int position) {
        if (isShowFavoriteTab) {
            switch (position){
                case 0:
                    return UserInfoFragment.newInstance(userId);
                case 1:
                    return UserTimelineFragment.newInstance(userId);
                case 2:
                    return UserLikeFragment.newInstance(userId);
                case 3:
                    return UserFollowsFragment.newInstance(userId);
                case 4:
                    return UserFollowersFragment.newInstance(userId);
                default:
                    return null;
            }
        } else {
            switch (position){
                case 0:
                    return UserInfoFragment.newInstance(userId);
                case 1:
                    return UserTimelineFragment.newInstance(userId);
                case 2:
                    return UserFollowsFragment.newInstance(userId);
                case 3:
                    return UserFollowersFragment.newInstance(userId);
                default:
                    return null;
            }
        }
    }

    @Override
    public int getCount() {
        return isShowFavoriteTab? 5: 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof ToolbarTitleInterface) {
            return context.getString(((ToolbarTitleInterface)fragment).getTitleResourceId());
        } else {
            return null;
        }
    }
}
