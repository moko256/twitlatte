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
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
public abstract class BaseTweetListFragment extends BaseTwitterListFragment {
    TweetListAdapter listAdapter;
    RecyclerView recyclerView;
    ArrayList<Status> homeTl;

    @Override
    public void startProcess(View view) {
        homeTl=new ArrayList<>();
        listAdapter = new TweetListAdapter(getContext(),homeTl,null);
        recyclerView = (RecyclerView) view.findViewById(R.id.TLlistView);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load(int page) {
                if(homeTl.size()!=0){
                    Paging paging=new Paging();
                    paging.maxId(homeTl.get(homeTl.size()-1).getId());
                    getResponseObservable(paging)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        int size = result.size();
                                        if (size > 0) {
                                            int l=homeTl.size();
                                            result.remove(0);
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
        ArrayList<Status> list=(ArrayList<Status>) savedInstanceState.getSerializable("list");
        if(list!=null){
            homeTl.addAll(list);
            listAdapter.notifyDataSetChanged();
        }
        else initializationProcess(view);
    }

    @Override
    public void initializationProcess(View view) {
        getResponseObservable(new Paging(1,20))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> homeTl.addAll(result),
                        Throwable::printStackTrace,
                        ()-> listAdapter.notifyDataSetChanged()
                );
    }

    public void updateProcess(View view, SwipeRefreshLayout swipeRefreshLayout) {
        Paging paging=homeTl.size()!=0?new Paging(homeTl.get(0).getId()):new Paging(1,20);

        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                homeTl.addAll(0, result);
                                listAdapter.notifyItemRangeInserted(0,size);
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
                        () -> swipeRefreshLayout.setRefreshing(false)
                );
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", (ArrayList) homeTl);
    }

    public Observable<ResponseList<Status>> getResponseObservable(Paging paging) {
        return Observable.create(
                subscriber->{
                    try {
                        subscriber.onNext(getResponseList(paging));
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    public abstract ResponseList<Status> getResponseList(Paging paging) throws TwitterException;

}


