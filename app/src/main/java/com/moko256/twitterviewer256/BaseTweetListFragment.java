package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

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
    ArrayList<Status> statuses;

    @Override
    public void startProcess(View view) {
        statuses =new ArrayList<>();
        listAdapter = new TweetListAdapter(getContext(), statuses);
        recyclerView = (RecyclerView) view.findViewById(R.id.TLlistView);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load(int page) {
                if(statuses.size()!=0){
                    Paging paging=new Paging();
                    paging.maxId(statuses.get(statuses.size()-1).getId());
                    getResponseObservable(paging)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        int size = result.size();
                                        if (size > 0) {
                                            int l= statuses.size();
                                            result.remove(0);
                                            statuses.addAll(result);
                                            listAdapter.notifyItemRangeInserted(listAdapter.getHeaderCount()+l,size);
                                        }
                                    },
                                    e -> {
                                        e.printStackTrace();
                                        Snackbar.make(view, "Error", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Try", v -> this.load(page))
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
            statuses.addAll(list);
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
                        result-> statuses.addAll(result),
                        Throwable::printStackTrace,
                        ()-> listAdapter.notifyDataSetChanged()
                );
    }

    public void updateProcess(View view, SwipeRefreshLayout swipeRefreshLayout) {
        Paging paging= statuses.size()!=0?new Paging(statuses.get(0).getId()):new Paging(1,20);

        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                statuses.addAll(0, result);
                                listAdapter.notifyItemRangeInserted(listAdapter.getHeaderCount(),size);
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
                            swipeRefreshLayout.setRefreshing(false);
                            Toast t=Toast.makeText(getContext(),"New Tweet",Toast.LENGTH_LONG);
                            t.setGravity(Gravity.TOP|Gravity.CENTER,0,0);
                            t.show();
                        }
                );
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", (ArrayList) statuses);
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

    public TweetListAdapter getListAdapter() {
        return listAdapter;
    }

}