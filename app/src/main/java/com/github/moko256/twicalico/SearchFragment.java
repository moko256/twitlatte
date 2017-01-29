package com.github.moko256.twicalico;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

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
public class SearchFragment extends BaseListFragment<StatusesAdapter,Status> {

    private String searchText="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void onInitializeList() {
        getResponseObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> {
                            getContentList().addAll(result);
                            getListAdapter().notifyDataSetChanged();
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
    protected void onUpdateList() {

    }

    @Override
    protected void onLoadMoreList() {

    }

    @Override
    protected boolean isInitializedList() {
        return false;
    }

    @Override
    protected StatusesAdapter initializeListAdapter(Context context, ArrayList<Status> data) {
        return new StatusesAdapter(context,data);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_search_toolbar,menu);
        SearchView searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();
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
        super.onCreateOptionsMenu(menu,inflater);
    }
}
