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

import com.github.moko256.core.client.base.entity.AccessToken;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.github.moko256.twitlatte.core.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class ShowUserFragmentsPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_INFO = 0;
    private static final int FRAGMENT_TIMELINE = 1;
    private static final int FRAGMENT_LIKE = 2;
    private static final int FRAGMENT_MEDIA = 3;
    private static final int FRAGMENT_FOLLOW = 4;
    private static final int FRAGMENT_FOLLOWER = 5;


    final private List<Integer> list;

    final private Context context;
    final private long userId;

    ShowUserFragmentsPagerAdapter(Client client, FragmentManager fm, Context context, long userId) {
        super(fm);

        AccessToken accessToken = client.getAccessToken();
        list = new ArrayList<>(5);
        list.add(FRAGMENT_INFO);
        list.add(FRAGMENT_TIMELINE);
        if (accessToken.getClientType() == CLIENT_TYPE_MASTODON){
            list.add(FRAGMENT_MEDIA);
        }
        if (!(accessToken.getClientType() == CLIENT_TYPE_MASTODON && userId != accessToken.getUserId())){
            list.add(FRAGMENT_LIKE);
        }
        list.add(FRAGMENT_FOLLOW);
        list.add(FRAGMENT_FOLLOWER);

        this.context = context;
        this.userId = userId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (list.get(position)){
            case FRAGMENT_INFO:
                return UserInfoFragment.newInstance(userId);
            case FRAGMENT_TIMELINE:
                return UserTimelineFragment.Companion.newInstance(userId);
            case FRAGMENT_LIKE:
                return UserLikeFragment.Companion.newInstance(userId);
            case FRAGMENT_MEDIA:
                return MediaTimelineFragment.Companion.newInstance(userId);
            case FRAGMENT_FOLLOW:
                return UserFollowsFragment.Companion.newInstance(userId);
            case FRAGMENT_FOLLOWER:
                return UserFollowersFragment.Companion.newInstance(userId);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getFragment(position);
        if (fragment instanceof ToolbarTitleInterface) {
            return context.getString(((ToolbarTitleInterface)fragment).getTitleResourceId());
        } else {
            return null;
        }
    }
}
