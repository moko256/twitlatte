/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.github.moko256.twitlatte.array.ArrayUtils;
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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
    private List<Long> list;

    private CompositeDisposable disposable;

    private CachedIdListSQLiteOpenHelper statusIdsDatabase;

    private int LAST_SAVED_LIST_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        disposable = new CompositeDisposable();
        statusIdsDatabase = new CachedIdListSQLiteOpenHelper(getContext(), GlobalApplication.accessToken, getCachedIdsDatabaseName());
        List<Long> c = statusIdsDatabase.getIds();
        if (c.size() > 0) {
            list.addAll(c);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        int dp8 = Math.round(8f * getResources().getDisplayMetrics().density);

        getRecyclerView().setPadding(dp8, 0, 0, 0);
        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = dp8;
                outRect.top = dp8;
            }
        });

        if (getActivity() instanceof GetRecyclerViewPool) {
            getRecyclerView().setRecycledViewPool(((GetRecyclerViewPool) getActivity()).getTweetListViewPool());
        }

        adapter=new StatusesAdapter(getContext(), list);
        adapter.setOnLoadMoreClick(position -> {
            long sinceId;

            if (list.size() > position + 2){
                if (list.get(position + 2) == -1) {
                    sinceId = list.get(position + 1);
                } else {
                    sinceId = list.get(position + 2);
                }
            } else {
                sinceId = list.get(position + 1);
            }

            disposable.add(
                    getResponseSingle(
                            new Paging()
                                    .maxId(list.get(position - 1) - 1L)
                                    .sinceId(sinceId)
                                    .count(GlobalApplication.statusLimit), list.size() >= position + 2 ? new long[]{list.get(position + 1)} : new long[0])
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        if (result.size() > 0) {
                                            View startView = getRecyclerView().getLayoutManager().findViewByPosition(position);
                                            int offset = (startView == null) ? 0 : (startView.getTop() - getRecyclerView().getPaddingTop());

                                            List<Long> ids = Observable
                                                    .fromIterable(result)
                                                    .map(Status::getId)
                                                    .toList().blockingGet();
                                            boolean noGap = ids.get(ids.size() - 1).equals(list.get(position + 1));
                                            if (noGap) {
                                                ids.remove(ids.size() - 1);
                                                list.remove(position);
                                                statusIdsDatabase.deleteIds(ArrayUtils.convertToLongList(-1L));
                                            }

                                            list.addAll(position, ids);
                                            statusIdsDatabase.insertIds(position, ids);

                                            if (noGap) {
                                                adapter.notifyItemRemoved(position);
                                            } else {
                                                adapter.notifyItemChanged(position);
                                            }

                                            RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
                                            if (layoutManager instanceof LinearLayoutManager) {
                                                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position + ids.size(), offset);
                                                adapter.notifyItemRangeInserted(position, ids.size());
                                            } else {
                                                ((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(position + ids.size(), offset);
                                            }

                                        } else {
                                            list.remove(position);
                                            statusIdsDatabase.deleteIds(ArrayUtils.convertToLongList(-1L));
                                            adapter.notifyItemRemoved(position);
                                        }
                                    },
                                    e -> {
                                        e.printStackTrace();
                                        getSnackBar(TwitterStringUtils.convertErrorToText(e)).show();
                                    }
                            )
            );
        });
        setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        LAST_SAVED_LIST_POSITION = statusIdsDatabase.getListViewPosition();
        getRecyclerView().getLayoutManager().scrollToPosition(LAST_SAVED_LIST_POSITION);

        return view;
    }

    @Override
    public void onDestroyView() {
        disposable.dispose();
        RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
        int position = getFirstVisibleItemPosition(layoutManager);
        if (list.size() - position > GlobalApplication.statusCacheListLimit){
            List<Long> subList = list.subList(position + GlobalApplication.statusCacheListLimit, list.size());
            statusIdsDatabase.deleteIds(subList);

            GlobalApplication.statusCache.delete(subList);
        }
        super.onDestroyView();
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
        disposable = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        disposable.add(
                getResponseSingle(new Paging(1,20))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> setRefreshing(false))
                        .subscribe(
                                result-> {
                                    List<Long> ids = Observable
                                            .fromIterable(result)
                                            .map(Status::getId)
                                            .toList().blockingGet();
                                    list.addAll(ids);
                                    statusIdsDatabase.addIds(ids);
                                    adapter.notifyDataSetChanged();
                                },
                                e -> {
                                    e.printStackTrace();
                                    getSnackBar(TwitterStringUtils.convertErrorToText(e)).show();
                                }
                        )
        );
    }

    @Override
    protected void onUpdateList() {
        long sinceId;

        if (list.size() >= 2){
            if (list.get(1) == -1) {
                sinceId = list.get(0);
            } else {
                sinceId = list.get(1);
            }
        } else {
            sinceId = list.get(0);
        }

        disposable.add(
                getResponseSingle(
                        new Paging(sinceId).count(GlobalApplication.statusLimit),
                        list.size() >= 2 ? new long[]{list.get(0)}: new long[0])
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> setRefreshing(false))
                        .subscribe(
                                result -> {
                                    if (result.size() > 0) {
                                        List<Long> ids = Observable
                                                .fromIterable(result)
                                                .map(Status::getId)
                                                .toList().blockingGet();
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
                                            Toast t=Toast.makeText(getContext(),R.string.new_posts,Toast.LENGTH_SHORT);
                                            t.setGravity(
                                                    Gravity.TOP|Gravity.CENTER,
                                                    0,
                                                    requireContext().getTheme().resolveAttribute(R.attr.actionBarSize, value, true)?
                                                            TypedValue.complexToDimensionPixelOffset(value.data, getResources().getDisplayMetrics()):
                                                            0
                                            );
                                            t.show();
                                        }
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    getSnackBar(TwitterStringUtils.convertErrorToText(e)).show();
                                }
                        )
        );
    }

    @Override
    protected void onLoadMoreList() {
        int bottomPos = list.size() -1;

        if (list.get(bottomPos) == -1L) {
            statusIdsDatabase.deleteIds(ArrayUtils.convertToLongList(-1L));
            list.remove(bottomPos);
            adapter.notifyItemRemoved(bottomPos);
        }

        disposable.add(
                getResponseSingle(new Paging().maxId(list.get(list.size() - 1) - 1L).count(GlobalApplication.statusLimit))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    int size = result.size();
                                    if (size > 0) {
                                        List<Long> ids = Observable
                                                .fromIterable(result)
                                                .map(Status::getId)
                                                .toList().blockingGet();
                                        list.addAll(ids);
                                        statusIdsDatabase.insertIds(list.size() - size, ids);
                                        adapter.notifyItemRangeInserted(list.size() - size, size);
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    getSnackBar(TwitterStringUtils.convertErrorToText(e)).show();
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
        WindowManager wm = (WindowManager) Objects.requireNonNull(requireActivity().getSystemService(Context.WINDOW_SERVICE));
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

    private Single<ResponseList<Status>> getResponseSingle(Paging paging, long... excludeIds) {
        return Single.create(
                subscriber->{
                    try {
                        ResponseList<Status> statuses = getResponseList(paging);
                        if (statuses.size() > 0){
                            GlobalApplication.statusCache.addAll(statuses, excludeIds);
                        }
                        subscriber.onSuccess(statuses);
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                }
        );
    }

    protected abstract ResponseList<Status> getResponseList(Paging paging) throws TwitterException;

    interface GetRecyclerViewPool {
        RecyclerView.RecycledViewPool getTweetListViewPool();
    }

}