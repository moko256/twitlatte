package com.github.moko256.twitlatte;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */
class ImagePagerAdapter extends FragmentPagerAdapter {

    ImagePagerChildFragment[] fragments;

    ImagePagerAdapter(FragmentManager fm, ExtendedMediaEntity[] mediaEntities, Context context) {
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