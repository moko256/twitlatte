package com.github.moko256.twicalico;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
    ArrayList<Long> list;

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

                int span=((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
                float dens=getContext().getResources().getDisplayMetrics().density;

                outRect.left=Math.round(dens*(span==0?8f:4f));
                outRect.right=Math.round(dens*(span==((StaggeredGridLayoutManager) parent.getLayoutManager()).getSpanCount()-1?8f:4f));
                outRect.top=Math.round(dens*8f);
            }
        });

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
            ArrayList<Long> l=(ArrayList<Long>) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(l);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list);
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
                            GlobalApplication.statusCache.addAll(result);
                            list.addAll(
                                    Observable
                                    .from(result)
                                    .map(Status::getId)
                                    .toList().toSingle().toBlocking().value()
                            );
                            adapter.notifyDataSetChanged();
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getSnackBarParentContainer(), getContext().getString(R.string.error_occurred_with_error_code,
                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, v -> onInitializeList())
                                    .show();
                        },
                        ()-> getSwipeRefreshLayout().setRefreshing(false)
                );
    }

    @Override
    protected void onUpdateList() {
        Paging paging=new Paging(list.get(0));
        paging.count(50);

        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                GlobalApplication.statusCache.addAll(result);
                                list.addAll(
                                        0,
                                        Observable
                                                .from(result)
                                                .map(Status::getId)
                                                .toList().toSingle().toBlocking().value()
                                );
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
                            Snackbar.make(getSnackBarParentContainer(), getContext().getString(R.string.error_occurred_with_error_code,
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
        paging.maxId(list.get(list.size()-1)-1L);
        paging.count(50);
        getResponseObservable(paging)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            int size = result.size();
                            if (size > 0) {
                                GlobalApplication.statusCache.addAll(result);
                                list.addAll(
                                        Observable
                                                .from(result)
                                                .map(Status::getId)
                                                .toList().toSingle().toBlocking().value()
                                );
                                adapter.notifyItemRangeInserted(list.size(),size);
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            Snackbar.make(getSnackBarParentContainer(), getContext().getString(R.string.error_occurred_with_error_code,
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
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
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