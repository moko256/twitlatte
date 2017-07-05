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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

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
            searchText=getActivity().getIntent().getStringExtra(BUNDLE_KEY_SEARCH_QUERY);
        }
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchText=savedInstanceState.getString(BUNDLE_KEY_SEARCH_QUERY,"");
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_SEARCH_QUERY,searchText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchText=null;
    }

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        SearchResultList result=new SearchResultList();
        if (!searchText.equals("")){
            Query query=new Query(searchText)
                    .count(paging.getCount())
                    .sinceId(paging.getSinceId())
                    .maxId(paging.getMaxId());
            result.addAll(GlobalApplication.twitter.search().search(query).getTweets());
        }
        return result;
    }

    private class SearchResultList extends ArrayList<Status> implements ResponseList<Status>{

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
    protected String getCachedIdsDatabaseName() {
        return "Search_" + searchText.hashCode();
    }
}
