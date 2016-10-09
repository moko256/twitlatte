package com.moko256.twitterviewer256;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
class FollowFollowerTabsPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> mFragments;
    private Context mContext;

    FollowFollowerTabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        ArrayList<BaseListFragment> baseTwitterListFragments=new ArrayList<>();

        baseTwitterListFragments.add(new MyFollowUserFragment());
        baseTwitterListFragments.add(new MyFollowerUserFragment());

        mFragments=new ArrayList<>(baseTwitterListFragments);
        mContext=context;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment=mFragments.get(position);
        if(fragment instanceof ToolbarTitleInterface){
            return mContext.getString(((ToolbarTitleInterface)fragment).getTitleResourceId());
        }
        else return null;
    }
}
