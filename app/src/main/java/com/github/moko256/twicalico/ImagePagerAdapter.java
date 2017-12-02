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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */
class ImagePagerAdapter extends FragmentPagerAdapter {

    ImagePagerChildFragment[] fragments;

    ImagePagerAdapter(FragmentManager fm, MediaEntity[] mediaEntities, Context context) {
        super(fm);
        int length=mediaEntities.length;
        fragments=new ImagePagerChildFragment[length];
        for (int i = 0; i < length; i++) {
            fragments[i]=ImagePagerChildFragment.getInstance(mediaEntities[i]);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}