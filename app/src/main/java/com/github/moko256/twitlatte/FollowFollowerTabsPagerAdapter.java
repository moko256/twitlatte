/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte;

import android.content.Context;

import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
class FollowFollowerTabsPagerAdapter extends FragmentPagerAdapter {
    final private Context context;
    final private long userId;

    FollowFollowerTabsPagerAdapter(FragmentManager fm, Context context,long userId) {
        super(fm);
        this.context = context;
        this.userId = userId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return UserFollowsFragment.newInstance(userId);
            case 1:
                return UserFollowersFragment.newInstance(userId);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getFragment(position);
        if(fragment instanceof ToolbarTitleInterface){
            return context.getString(((ToolbarTitleInterface)fragment).getTitleResourceId());
        } else {
            return null;
        }
    }
}
