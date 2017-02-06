package com.github.moko256.twicalico;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2016/07/27.
 *
 * @author moko256
 */
public class SearchFragment extends BaseListFragment {

    private static String BUNDLE_KEY_SEARCH_QUERY="query";

    private ArrayList<Status> list;
    private String searchText="";
    private StatusesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        adapter=new StatusesAdapter(getContext(), list);
        setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        return view;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Status> l=(ArrayList<Status>) savedInstanceState.getSerializable("list");
        if(l!=null){
            list.addAll(l);
        }
        searchText=savedInstanceState.getString(BUNDLE_KEY_SEARCH_QUERY,"");
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", (ArrayList) list);
        outState.putString(BUNDLE_KEY_SEARCH_QUERY,searchText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        list=null;
        searchText=null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_search_toolbar,menu);
        MenuItem searchMenu=menu.findItem(R.id.action_search);
        searchMenu.expandActionView();
        SearchView searchView=(SearchView) searchMenu.getActionView();
        searchView.setQuery(searchText,false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                searchText=searchWord;
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
    protected void onInitializeList() {
        getResponseObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> {
                            list.clear();
                            list.addAll(result);
                            adapter.notifyDataSetChanged();
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getView(), "Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Try", v -> onInitializeList())
                                    .show();
                        },
                        ()-> getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onUpdateList() {}

    @Override
    protected void onLoadMoreList() {}

    @Override
    protected boolean isInitializedList() {
        return false;
    }

    public Observable<List<Status>> getResponseObservable() {
        return Observable.create(
                subscriber->{
                    try {
                        if (!searchText.equals("")){
                            subscriber.onNext(GlobalApplication.twitter.search().search(new Query(searchText)).getTweets());
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }
}
