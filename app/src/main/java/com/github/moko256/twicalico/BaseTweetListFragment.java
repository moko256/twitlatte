/*
 * Copyright 2018 The twicalico authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twicalico;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.moko256.twicalico.array.ArrayUtils;
import com.github.moko256.twicalico.database.CachedIdListSQLiteOpenHelper;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
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
    List<Long> list;

    CompositeSubscription subscription;

    CachedIdListSQLiteOpenHelper statusIdsDatabase;

    int LAST_SAVED_LIST_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        subscription = new CompositeSubscription();
        statusIdsDatabase = new CachedIdListSQLiteOpenHelper(getContext(), GlobalApplication.userId, getCachedIdsDatabaseName());
        if (savedInstanceState == null){
            List<Long> c = statusIdsDatabase.getIds();
            if (c.size() > 0) {
                list.addAll(c);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int span;
                int spanCount;
                if (view.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams){
                    span = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
                    spanCount = ((StaggeredGridLayoutManager) parent.getLayoutManager()).getSpanCount();
                } else {
                    span = 0;
                    spanCount = 1;
                }
                float dens = getResources().getDisplayMetrics().density;

                outRect.left = Math.round(dens * (span == 0 ? 8f : 4f));
                outRect.right = Math.round(dens * (span == spanCount - 1 ? 8f : 4f));
                outRect.top = Math.round(dens * 8f);
            }
        });

        if (getActivity() instanceof GetRecyclerViewPool) {
            getRecyclerView().setRecycledViewPool(((GetRecyclerViewPool) getActivity()).getTweetListViewPool());
        }

        adapter=new StatusesAdapter(getContext(), list);
        adapter.setOnLoadMoreClick(position -> subscription.add(
                getResponseSingle(
                        new Paging()
                                .maxId(list.get(position-1)-1L)
                                .sinceId(list.get(list.size() >= position + 2? position + 2: position + 1))
                                .count(GlobalApplication.statusLimit))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result.size() > 0) {
                                        list.remove(position);
                                        statusIdsDatabase.deleteIds(ArrayUtils.convertToLongList(-1L));
                                        adapter.notifyItemRemoved(position);
                                        List<Long>ids = Observable
                                                .from(result)
                                                .map(Status::getId)
                                                .toList().toSingle().toBlocking().value();
                                        if (ids.get(ids.size() - 1).equals(list.get(position))) {
                                            ids.remove(ids.size() - 1);
                                        } else {
                                            ids.add(-1L);
                                        }
                                        list.addAll(position, ids);
                                        statusIdsDatabase.insertIds(position, ids);
                                        adapter.notifyItemRangeInserted(position, ids.size());
                                    } else {
                                        list.remove(position);
                                        statusIdsDatabase.deleteIds(ArrayUtils.convertToLongList(-1L));
                                        adapter.notifyItemRemoved(position);
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    Snackbar.make(getSnackBarParentContainer(), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.retry, v -> onLoadMoreList())
                                            .show();
                                }
                        )
        ));
        setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        LAST_SAVED_LIST_POSITION = statusIdsDatabase.getListViewPosition();
        getRecyclerView().getLayoutManager().scrollToPosition(LAST_SAVED_LIST_POSITION);

        return view;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            Long[] l=(Long[]) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(Arrays.asList(l));
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list.toArray(new Long[list.size()]));
    }

    @Override
    public void onDestroyView() {
        RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
        int position = getFirstVisibleItemPosition(layoutManager);
        if (list.size() - position > GlobalApplication.statusCacheListLimit){
            List<Long> subList = list.subList(position + GlobalApplication.statusCacheListLimit, list.size());
            statusIdsDatabase.deleteIds(subList);

            boolean[] results = statusIdsDatabase.hasIdsOtherTable(subList);
            List<Long> deletableIds = new ArrayList<>();
            for (int i = 0; i < subList.size(); i++) {
                if (!results[i]) {
                    deletableIds.add(subList.get(i));
                }
            }
            GlobalApplication.statusCache.delete(deletableIds);
        }
        super.onDestroyView();
        subscription.unsubscribe();
        adapter=null;
    }

    @Override
    public void onStop() {
        super.onStop();
        int position = getFirstVisibleItemPosition(getRecyclerView().getLayoutManager());
        if (position != LAST_SAVED_LIST_POSITION){
            statusIdsDatabase.setListViewPosition(position);
            LAST_SAVED_LIST_POSITION = position;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        statusIdsDatabase.close();
        statusIdsDatabase = null;
        subscription = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        subscription.add(
                getResponseSingle(new Paging(1,20))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result-> {
                                    List<Long> ids = Observable
                                            .from(result)
                                            .map(Status::getId)
                                            .toList().toSingle().toBlocking().value();
                                    list.addAll(ids);
                                    statusIdsDatabase.addIds(ids);
                                    adapter.notifyDataSetChanged();
                                    setRefreshing(false);
                                },
                                e -> {
                                    e.printStackTrace();
                                    Snackbar.make(getSnackBarParentContainer(), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.retry, v -> {
                                                setRefreshing(true);
                                                onInitializeList();
                                            })
                                            .show();
                                    setRefreshing(false);
                                }
                        )
        );
    }

    @Override
    protected void onUpdateList() {
        subscription.add(
                getResponseSingle(new Paging(list.get(list.size() >= 2? 1: 0)).count(GlobalApplication.statusLimit))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result.size() > 0) {
                                        List<Long> ids = Observable
                                                .from(result)
                                                .map(Status::getId)
                                                .toList().toSingle().toBlocking().value();
                                        if (ids.get(ids.size() - 1).equals(list.get(0))) {
                                            ids.remove(ids.size() - 1);
                                        } else {
                                            ids.add(-1L);
                                        }

                                        if (ids.size() > 0){
                                            list.addAll(0, ids);
                                            statusIdsDatabase.insertIds(0, ids);
                                            adapter.notifyItemRangeInserted(0, ids.size());
                                            TypedValue value=new TypedValue();
                                            Toast t=Toast.makeText(getContext(),R.string.new_post,Toast.LENGTH_SHORT);
                                            t.setGravity(
                                                    Gravity.TOP|Gravity.CENTER,
                                                    0,
                                                    getContext().getTheme().resolveAttribute(R.attr.actionBarSize, value, true)?
                                                            TypedValue.complexToDimensionPixelOffset(value.data, getResources().getDisplayMetrics()):
                                                            0
                                            );
                                            t.show();
                                        }
                                    }
                                    setRefreshing(false);
                                },
                                e -> {
                                    e.printStackTrace();
                                    Snackbar.make(getSnackBarParentContainer(), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.retry, v -> {
                                                setRefreshing(true);
                                                onUpdateList();
                                            })
                                            .show();
                                    setRefreshing(false);
                                }
                        )
        );
    }

    @Override
    protected void onLoadMoreList() {
        subscription.add(
                getResponseSingle(new Paging().maxId(list.get(list.size()-1)-1L).count(GlobalApplication.statusLimit))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    int size = result.size();
                                    if (size > 0) {
                                        List<Long> ids = Observable
                                                .from(result)
                                                .map(Status::getId)
                                                .toList().toSingle().toBlocking().value();
                                        list.addAll(ids);
                                        statusIdsDatabase.insertIds(list.size() - size, ids);
                                        adapter.notifyItemRangeInserted(list.size() - size, size);
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    Snackbar.make(getSnackBarParentContainer(), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.retry, v -> onLoadMoreList())
                                            .show();
                                }
                        )
        );
    }

    @Override
    protected boolean isInitializedList() {
        return !list.isEmpty();
    }

    @Override
    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int count = (int) Math.ceil(
                (double)
                        //Calculated as:
                        //Picture area: (16 : 9) + Other content: (16 : 3)
                        (size.x * 12) / (size.y * 16)
        );
        if (count == 1) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setRecycleChildrenOnDetach(true);
            return layoutManager;
        } else {
            return new StaggeredGridLayoutManager(
                    count,
                    StaggeredGridLayoutManager.VERTICAL
            );
        }
    }

    protected abstract String getCachedIdsDatabaseName();

    public Single<ResponseList<Status>> getResponseSingle(Paging paging) {
        return Single.create(
                subscriber->{
                    try {
                        ResponseList<Status> statuses = getResponseList(paging);
                        if (statuses.size() > 0){
                            GlobalApplication.statusCache.addAll(statuses);
                        }
                        subscriber.onSuccess(statuses);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    public abstract ResponseList<Status> getResponseList(Paging paging) throws TwitterException;

    interface GetRecyclerViewPool {
        RecyclerView.RecycledViewPool getTweetListViewPool();
    }

}