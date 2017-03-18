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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
class FollowFollowerTabsPagerAdapter extends FragmentPagerAdapter {
    private Fragment[] mFragments;
    private Context mContext;

    FollowFollowerTabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        mFragments=new Fragment[]{
                new MyFollowUserFragment(),
                new MyFollowerUserFragment()
        };
        mContext=context;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment=mFragments[position];
        if(fragment instanceof ToolbarTitleInterface){
            return mContext.getString(((ToolbarTitleInterface)fragment).getTitleResourceId());
        }
        else return null;
    }
}
