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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper;
import com.github.moko256.twitlatte.entity.UpdateEvent;
import com.github.moko256.twitlatte.model.impl.ListModelImpl;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.viewmodel.ListViewModel;
import com.github.moko256.twitlatte.widget.AdapterObservableBinderKt;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
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

    private CompositeDisposable disposable;

    private ListViewModel listViewModel;

    private Function1<UpdateEvent, Unit> adapterObservableBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        if (!listViewModel.getInitilized()) {
            listViewModel.model = new ListModelImpl(
                    (sinceId, maxId, limit) -> {
                        Paging paging = new Paging().count(limit);
                        if (sinceId != null) {
                            paging.setSinceId(sinceId);
                        }
                        if (maxId != null) {
                            paging.setMaxId(maxId);
                        }
                        if (sinceId == null && maxId == null) {
                            paging.setPage(1);
                        }
                        return getResponseList(paging);
                    },
                    new CachedIdListSQLiteOpenHelper(
                            requireContext().getApplicationContext(),
                            GlobalApplication.accessToken,
                            getCachedIdsDatabaseName()
                    )
            );
            listViewModel.start();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int dp8 = Math.round(8f * getResources().getDisplayMetrics().density);

        recyclerView.setPadding(dp8, 0, 0, 0);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = dp8;
                outRect.top = dp8;
            }
        });

        if (getActivity() instanceof GetRecyclerViewPool) {
            recyclerView.setRecycledViewPool(((GetRecyclerViewPool) getActivity()).getTweetListViewPool());
        }

        adapter=new StatusesAdapter(getContext(), listViewModel.model.getIdsList());
        adapter.onLoadMoreClick = position -> listViewModel.model.loadOnGap(position);
        adapter.onFavoriteClick = (position, id, hasFavorited) -> {
            if (hasFavorited) {
                Single
                        .create(subscriber -> {
                            try {
                                Status newStatus = GlobalApplication.twitter.destroyFavorite(id);
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }

                                    notifyBySnackBar(
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNLIKE
                                            )
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    notifyErrorBySnackBar(throwable).show();
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }
                                }
                        );
            } else {
                Single
                        .create(subscriber -> {
                            try {
                                Status newStatus = GlobalApplication.twitter.createFavorite(id);
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }

                                    notifyBySnackBar(
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.LIKE
                                            )
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    notifyErrorBySnackBar(throwable).show();
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }
                                }
                        );
            }
        };
        adapter.onRepeatClick = (position, id, hasFavorited) -> {
            if (hasFavorited) {
                Single
                        .create(subscriber -> {
                            try {
                                Status newStatus = GlobalApplication.twitter.unRetweetStatus(id);
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }

                                    notifyBySnackBar(
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.UNREPEAT
                                            )
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    notifyErrorBySnackBar(throwable).show();
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }
                                }
                        );
            } else {
                Single
                        .create(subscriber -> {
                            try {
                                Status newStatus = GlobalApplication.twitter.retweetStatus(id);
                                GlobalApplication.statusCache.add(newStatus, false);
                                subscriber.onSuccess(newStatus);
                            } catch (TwitterException e) {
                                subscriber.tryOnError(e);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }

                                    notifyBySnackBar(
                                            TwitterStringUtils.getDidActionStringRes(
                                                    GlobalApplication.clientType,
                                                    TwitterStringUtils.Action.REPEAT
                                            )
                                    ).show();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    notifyErrorBySnackBar(throwable).show();
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }
                                }
                        );
            }
        };

        recyclerView.setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        recyclerView.getLayoutManager().scrollToPosition(listViewModel.model.getSeeingPosition());

        adapterObservableBinder = AdapterObservableBinderKt.convertObservableConsumer(recyclerView);

        disposable = new CompositeDisposable(
                listViewModel
                        .model
                        .getListEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            adapterObservableBinder.invoke(it);

                            if (swipeRefreshLayout.isRefreshing()){
                                setRefreshing(false);
                            }
                        }),

                listViewModel
                        .model
                        .getErrorEventObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            notifyErrorBySnackBar(it).show();

                            if (swipeRefreshLayout.isRefreshing()){
                                setRefreshing(false);
                            }
                        })
        );
    }

    @Override
    public void onDestroyView() {
        disposable.dispose();
        adapterObservableBinder = null;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int position = getFirstVisibleItemPosition(layoutManager);
        listViewModel.model.removeOldCache(position);
        recyclerView.swapAdapter(null, true);
        adapter=null;
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        int position = getFirstVisibleItemPosition(recyclerView.getLayoutManager());
        if (position >= 0) {
            listViewModel.model.updateSeeingPosition(position);
        }
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        listViewModel.model.refreshFirst();
    }

    @Override
    protected void onUpdateList() {
        listViewModel.model.refreshOnTop();
    }

    @Override
    protected void onLoadMoreList() {
        listViewModel.model.loadOnBottom();
    }

    @Override
    protected boolean isInitializedList() {
        return !listViewModel.model.getIdsList().isEmpty();
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

    protected abstract ResponseList<Status> getResponseList(Paging paging) throws TwitterException;

    interface GetRecyclerViewPool {
        RecyclerView.RecycledViewPool getTweetListViewPool();
    }

}