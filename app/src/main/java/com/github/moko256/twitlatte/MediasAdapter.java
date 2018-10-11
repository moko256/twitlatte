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

import com.github.moko256.twitlatte.entity.Media;
import com.github.moko256.twitlatte.mediaview.AbstractMediaFragment;
import com.github.moko256.twitlatte.mediaview.GifFragment;
import com.github.moko256.twitlatte.mediaview.ImageFragment;
import com.github.moko256.twitlatte.mediaview.MultiVideoFragment;
import com.github.moko256.twitlatte.mediaview.OneVideoFragment;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */
class MediasAdapter extends FragmentPagerAdapter {

    private final List<Media> medias;

    MediasAdapter(FragmentManager fm, List<Media> medias) {
        super(fm);
        this.medias = medias;
    }

    @Override
    public Fragment getItem(int position) {
        Media media = medias.get(position);

        AbstractMediaFragment fragment;
        switch (media.getImageType()){
            case "video_multi":
                fragment = new MultiVideoFragment();
                break;

            case "video_one":
                fragment = new OneVideoFragment();
                break;
            case "gif":
                fragment = new GifFragment();
                break;
            case "photo":
            default:
                fragment = new ImageFragment();
        }
        fragment.setMediaToArg(media);

        return fragment;
    }

    @Override
    public int getCount() {
        return medias.size();
    }
}