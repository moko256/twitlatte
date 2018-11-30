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

import android.os.Bundle;

import com.github.moko256.twitlatte.entity.Paging;
import com.github.moko256.twitlatte.entity.Post;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * Created by moko256 on 2016/07/27.
 *
 * @author moko256
 */
public class SearchResultFragment extends BaseTweetListFragment {

    private final static String BUNDLE_KEY_SEARCH_QUERY="query";

    private String searchText="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            searchText = getActivity().getIntent().getStringExtra(BUNDLE_KEY_SEARCH_QUERY);
        } else {
            searchText = savedInstanceState.getString(BUNDLE_KEY_SEARCH_QUERY,"");
        }
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_SEARCH_QUERY,searchText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchText=null;
    }

    @NotNull
    @Override
    public List<Post> request(@NotNull Paging paging) throws Throwable {
        if (!searchText.equals("")){
            return client.getApiClient().getPostByQuery(searchText, paging);
        } else {
            return Collections.emptyList();
        }
    }

    private static final class SearchResultList extends ArrayList<Status> implements ResponseList<Status>{

        @Override
        public int getAccessLevel() {return 0;}

        @Override
        public RateLimitStatus getRateLimitStatus() {
            return new RateLimitStatus() {
                @Override
                public int getRemaining() {return 0;}

                @Override
                public int getLimit() {return 0;}

                @Override
                public int getResetTimeInSeconds() {return 0;}

                @Override
                public int getSecondsUntilReset() {return 0;}
            };
        }
    }

    @Override
    @NonNull
    protected String getCachedIdsDatabaseName() {
        byte[] bytes = searchText.getBytes();
        StringBuilder bytesStr = new StringBuilder("Search_");
        for (byte aByte : bytes) {
            bytesStr.append(aByte < 0? "_" + String.valueOf(-aByte): String.valueOf(aByte));
        }
        return bytesStr.toString();
    }
}
