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
 * Created by moko256 on GitHub on 2016/03/29.
 */
public abstract class BaseUserListFragment extends BaseTwitterListFragment {
    UserListAdapter listAdapter;
    RecyclerView listView;
    ArrayList<User> homeTl;
    long next_cursor;
    //long prevous_cursor;

    @Override
    public void startProcess(View view) {
        homeTl=new ArrayList<>();
        listAdapter = new UserListAdapter(getContext(),homeTl,null);
        listView = (RecyclerView) view.findViewById(R.id.TLlistView);
        listView.setAdapter(listAdapter);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager)listView.getLayoutManager()) {
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
                                            result.remove(0);
                                            homeTl.addAll(result);
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
                                    () -> {
                                        listAdapter.notifyDataSetChanged();
                                    }
                            );
                }
            }
        });
    }

    @Override
    public void restoreProcess(View view,Bundle savedInstanceState) {
        ArrayList<User> list=(ArrayList<User>) savedInstanceState.getSerializable("UserList");
        next_cursor=savedInstanceState.getLong("next_cursor",-1);
        //previous_cursor=savedInstanceState.getLong("previous_cursor",-1);
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
        /*
        cursor=homeTl.size()!=0?-1:-1;

        getApi(cursor)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                homeTl.addAll(0, result);
                                //listAdapter.notifyItemRangeChanged(0,size);
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(view, "Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Try", v -> {
                                    })
                                    .show();
                        },
                        () -> {
                            listAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                );
                */
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("UserList", (ArrayList) homeTl);
        outState.putLong("next_cursor",next_cursor);
        //outState.putLong("previous_cursor",previous_cursor);
    }

    public Observable<PagableResponseList<User>> getResponseObservable(long cursor) {
        return Observable.create(
                subscriber->{
                    try {
                        PagableResponseList<User> pagableResponseList=getResponseList(cursor);
                        next_cursor=pagableResponseList.getNextCursor();
                        //previous_cursor=pagableResponseList.getPreviousCursor();
                        subscriber.onNext(pagableResponseList);
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    };

    public abstract PagableResponseList<User> getResponseList(long cursor) throws TwitterException;

}
