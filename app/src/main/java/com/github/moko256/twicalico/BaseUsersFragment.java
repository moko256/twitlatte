package com.github.moko256.twicalico;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public abstract class BaseUsersFragment extends BaseListFragment {

    UsersAdapter adapter;
    ArrayList<Long> list;
    long next_cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view)==0){
                    outRect.top=Math.round(getContext().getResources().getDisplayMetrics().density*8f);
                }
            }
        });

        adapter=new UsersAdapter(getContext(), list);
        setAdapter(adapter);
        if(!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        return view;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Long> l=(ArrayList<Long>) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(l);
            }
            next_cursor=savedInstanceState.getLong("next_cursor",-1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list);
        outState.putLong("next_cursor", next_cursor);
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
    }

    @Override
    protected void onInitializeList() {
        getResponseObservable(-1)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> {
                            GlobalApplication.userCache.addAll(result);
                            list.addAll(
                                    Observable
                                            .from(result)
                                            .map(User::getId)
                                            .toList().toSingle().toBlocking().value()
                            );
                            adapter.notifyDataSetChanged();
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getView(), getContext().getString(R.string.error_occurred_with_error_code,
                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, v -> onInitializeList())
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
                                int l = list.size();
                                GlobalApplication.userCache.addAll(result);
                                list.addAll(
                                        Observable
                                                .from(result)
                                                .map(User::getId)
                                                .toList().toSingle().toBlocking().value()
                                );
                                adapter.notifyItemRangeInserted(l, size);
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getView(), getContext().getString(R.string.error_occurred_with_error_code,
                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, v -> onLoadMoreList())
                                    .show();
                        },
                        () -> {}
                );
    }

    @Override
    protected boolean isInitializedList() {
        return !list.isEmpty();
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
