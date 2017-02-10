package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public abstract class BaseTweetListFragment extends BaseListFragment {

    StatusesAdapter adapter;
    ArrayList<Status> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        super.onCreate(savedInstanceState);
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
        if (savedInstanceState != null){
            ArrayList<Status> l=(ArrayList<Status>) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(l);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", (ArrayList) list);
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
        getResponseObservable(new Paging(1,20))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result-> {
                            list.addAll(result);
                            adapter.notifyDataSetChanged();
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getView(), getContext().getString(R.string.error_occurred_with_error_code,
                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, v -> onInitializeList())
                                    .show();
                        },
                        ()-> getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onUpdateList() {
        Paging paging=new Paging(list.get(0).getId());
        paging.count(50);

        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                list.addAll(0, result);
                                adapter.notifyItemRangeInserted(0,size);
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
                            Snackbar.make(getView(), getContext().getString(R.string.error_occurred_with_error_code,
                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, v -> onUpdateList())
                                    .show();
                        },
                        () -> getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onLoadMoreList() {
        Paging paging=new Paging();
        paging.maxId(list.get(list.size()-1).getId()-1L);
        paging.count(50);
        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                list.addAll(result);
                                adapter.notifyItemRangeInserted(list.size(),size);
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

    @Override
    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager() {
        if (getContext().getResources().getConfiguration().smallestScreenWidthDp>=600){
            return new LinearLayoutManager(getContext());
        } else {
            return new LinearLayoutManager(getContext());
        }
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