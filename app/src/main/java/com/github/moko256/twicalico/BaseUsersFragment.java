package com.github.moko256.twicalico;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

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
public abstract class BaseUsersFragment extends BaseListFragment<UsersAdapter,User> {

    long next_cursor;

    @Override
    protected void onInitializeList() {
        if(!getSwipeRefreshLayout().isRefreshing())getSwipeRefreshLayout().setRefreshing(false);

        getResponseObservable(-1)
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
                        ()->getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onUpdateList() {
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void onLoadMoreList() {

        getResponseObservable(next_cursor)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                int l = getContentList().size();
                                getContentList().addAll(result);
                                getListAdapter().notifyItemRangeInserted(l, size);
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
    protected UsersAdapter initializeListAdapter(Context context, ArrayList<User> data) {
        return new UsersAdapter(context,data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            next_cursor=savedInstanceState.getLong("next_cursor",-1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
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
