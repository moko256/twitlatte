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
public class SearchFragment extends BaseTweetListFragment {

    private final static String BUNDLE_KEY_SEARCH_QUERY="query";

    private String searchText="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            Intent intent=getActivity().getIntent();
            if (intent!=null&&intent.getAction()!=null&&intent.getAction().equals(Intent.ACTION_SEARCH)){
                searchText=intent.getStringExtra(SearchManager.QUERY);
                onInitializeList();
            }
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_search_toolbar,menu);

        MenuItem searchMenu=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) searchMenu.getActionView();

        MenuItemCompat.expandActionView(searchMenu);
        searchView.onActionViewExpanded();
        searchView.setQuery(searchText,false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                searchText=searchWord;
                list.clear();
                onInitializeList();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(() -> {
            getActivity().finish();
            return false;
        });
        super.onCreateOptionsMenu(menu,inflater);
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
}
