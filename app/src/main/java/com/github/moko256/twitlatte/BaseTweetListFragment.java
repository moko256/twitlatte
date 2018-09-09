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

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
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

import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.viewmodel.ListViewModel;

import java.util.Objects;

import either.EitherKt;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
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

    private Disposable disposable;

    private ListViewModel listViewModel;

    private long LAST_SAVED_LIST_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        listViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        if (!listViewModel.getInitilized()) {
            listViewModel.statusIdsDatabase = new CachedIdListSQLiteOpenHelper(
                    requireContext().getApplicationContext(),
                    GlobalApplication.accessToken,
                    getCachedIdsDatabaseName()
            );
            listViewModel.serverRepository = (sinceId, maxId, limit) -> {
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
            };
            listViewModel.start();
        }
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CheckResult")
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

        adapter=new StatusesAdapter(getContext(), listViewModel.getList());
        adapter.onLoadMoreClick = position -> listViewModel.loadOnGap(position);
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
                                }
                        );
            }
        };

        setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        LAST_SAVED_LIST_ID = listViewModel.getSeeingId();
        getRecyclerView().getLayoutManager().scrollToPosition(listViewModel.getList().indexOf(LAST_SAVED_LIST_ID));

        disposable = listViewModel.getListObserver()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    EitherKt.fold(
                            it,
                            left -> {
                                switch (left.getType()) {
                                    case ADD_FIRST:
                                        adapter.notifyDataSetChanged();
                                        break;

                                    case ADD_TOP:
                                        adapter.notifyItemRangeInserted(left.getPosition(), left.getSize());
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
                                        break;

                                    case ADD_BOTTOM:
                                        adapter.notifyItemRangeInserted(left.getPosition(), left.getSize());
                                        break;

                                    case REMOVE_ONLY_GAP:
                                        adapter.notifyItemRemoved(left.getPosition());
                                        break;

                                    case INSERT_AT_GAP:
                                        View startView = getRecyclerView().getLayoutManager().findViewByPosition(left.getPosition());
                                        int offset = (startView == null) ? 0 : (startView.getTop() - getRecyclerView().getPaddingTop());

                                        boolean noGap = listViewModel.getList().get(left.getPosition() + left.getSize() - 1).equals(listViewModel.getList().get(left.getPosition() + 1));

                                        if (noGap) {
                                            adapter.notifyItemRemoved(left.getPosition());
                                        } else {
                                            adapter.notifyItemChanged(left.getPosition());
                                        }

                                        RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
                                        if (layoutManager instanceof LinearLayoutManager) {
                                            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(left.getPosition() + left.getSize(), offset);
                                            adapter.notifyItemRangeInserted(left.getPosition(), left.getSize());
                                        } else {
                                            ((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(left.getPosition() + left.getSize(), offset);
                                        }

                                }
                                return Unit.INSTANCE;
                            },
                            right -> {
                                notifyErrorBySnackBar(right).show();
                                return Unit.INSTANCE;
                            }
                    );
                    if (getSwipeRefreshLayout().isRefreshing()){
                        setRefreshing(false);
                    }
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        disposable.dispose();
        RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
        int position = getFirstVisibleItemPosition(layoutManager);
        listViewModel.removeOldCache(position);
        super.onDestroyView();
        adapter=null;
    }

    @Override
    public void onStop() {
        super.onStop();
        int position = getFirstVisibleItemPosition(getRecyclerView().getLayoutManager());
        if (position >= 0) {
            long id = listViewModel.getList().get(
                    position
            );
            if (id != LAST_SAVED_LIST_ID) {
                listViewModel.saveSeeingPosition(id);
                LAST_SAVED_LIST_ID = id;
            }
        }
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        listViewModel.refreshFirst();
    }

    @Override
    protected void onUpdateList() {
        listViewModel.refreshOnTop();
    }

    @Override
    protected void onLoadMoreList() {
        listViewModel.loadOnBottom();
    }

    @Override
    protected boolean isInitializedList() {
        return !listViewModel.getList().isEmpty();
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