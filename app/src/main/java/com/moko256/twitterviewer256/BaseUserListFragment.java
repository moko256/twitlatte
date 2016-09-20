package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/03/29.
 *
 * @author moko256
 */
public abstract class BaseUserListFragment extends BaseTwitterListFragment {
    UserListAdapter listAdapter;
    RecyclerView recyclerView;
    ArrayList<User> homeTl;
    long next_cursor;

    @Override
    public void startProcess(View view) {
        homeTl=new ArrayList<>();
        listAdapter = new UserListAdapter(getContext(),homeTl,null);
        recyclerView = (RecyclerView) view.findViewById(R.id.TLlistView);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load(int page) {
                if(homeTl.size()!=0){
                    getResponseObservable(next_cursor)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        int size = result.size();
                                        if (size > 0) {
                                            int l=homeTl.size();
                                            homeTl.addAll(result);
                                            listAdapter.notifyItemRangeInserted(l,size);
                                        }
                                    },
                                    e -> {
                                        e.printStackTrace();
                                        Snackbar.make(view, "Error", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Try", v -> {
                                                    this.load(page);
                                                })
                                                .show();
                                    },
                                    () -> {}
                            );
                }
            }
        });
    }

    @Override
    public void restoreProcess(View view,Bundle savedInstanceState) {
        ArrayList<User> list=(ArrayList<User>) savedInstanceState.getSerializable("UserList");
        next_cursor=savedInstanceState.getLong("next_cursor",-1);
        if(list!=null){
            homeTl.addAll(list);
            listAdapter.notifyDataSetChanged();
        }
        else initializationProcess(view);


    }

    @Override
    public void initializationProcess(View view) {
        getResponseObservable(-1)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> homeTl.addAll(result),
                        Throwable::printStackTrace,
                        ()->listAdapter.notifyDataSetChanged()
                );
    }

    @Override
    public void updateProcess(View view,SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("UserList", (ArrayList) homeTl);
        outState.putLong("next_cursor",next_cursor);
    }

    public Observable<PagableResponseList<User>> getResponseObservable(long cursor) {
        return Observable.create(
                subscriber->{
                    try {
                        PagableResponseList<User> pagableResponseList=getResponseList(cursor);
                        next_cursor=pagableResponseList.getNextCursor();
                        subscriber.onNext(pagableResponseList);
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    public abstract PagableResponseList<User> getResponseList(long cursor) throws TwitterException;

}
