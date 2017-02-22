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
