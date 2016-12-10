package com.github.moko256.twitlatte;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.Gravity;
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
public abstract class BaseTweetListFragment extends BaseListFragment<StatusesAdapter,Status> {


    @Override
    protected void onInitializeList() {
        if(!getSwipeRefreshLayout().isRefreshing())getSwipeRefreshLayout().setRefreshing(false);

        getResponseObservable(new Paging(1,20))
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
        if(!getSwipeRefreshLayout().isRefreshing())getSwipeRefreshLayout().setRefreshing(false);

        Paging paging=new Paging(getContentList().get(0).getId());

        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                getContentList().addAll(0, result);
                                getListAdapter().notifyItemRangeInserted(0,size);
                                TypedValue value=new TypedValue();
                                Toast t=Toast.makeText(getContext(),"New Tweet",Toast.LENGTH_SHORT);
                                t.setGravity(
                                        Gravity.TOP|Gravity.CENTER,
                                        0,
                                        getContext().getTheme().resolveAttribute(R.attr.actionBarSize, value, true)?
                                                TypedValue.complexToDimensionPixelOffset(value.data, getResources().getDisplayMetrics()):
                                                0
                                );
                                t.show();
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            getSwipeRefreshLayout().setRefreshing(false);
                            Snackbar.make(getView(), "Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Try", v -> onUpdateList())
                                    .show();
                        },
                        () -> getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onLoadMoreList() {
        Paging paging=new Paging();
        paging.maxId(getContentList().get(getContentList().size()-1).getId());
        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                int l= getContentList().size();
                                result.remove(0);
                                getContentList().addAll(result);
                                getListAdapter().notifyItemRangeInserted(l,size);
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getView(), "Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Try", v -> onLoadMoreList())
                                    .show();
                        },
                        () -> {}
                );
    }

    @Override
    protected boolean isInitializedList() {
        return getContentList().size()!=0;
    }

    @Override
    protected StatusesAdapter initializeListAdapter(Context context, ArrayList<Status> data) {
        return new StatusesAdapter(context,data);
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