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

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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
    List<Long> list;
    long next_cursor;

    CompositeDisposable subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        subscription = new CompositeDisposable();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view)==0){
                    outRect.top=Math.round(getResources().getDisplayMetrics().density*8f);
                }
            }
        });

        if (getActivity() instanceof BaseTweetListFragment.GetRecyclerViewPool) {
            getRecyclerView().setRecycledViewPool(((GetRecyclerViewPool) getActivity()).getUserListViewPool());
        }

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
            Long[] l = (Long[]) savedInstanceState.getSerializable("list");
            if(l != null){
                list.addAll(Arrays.asList(l));
            }
            next_cursor=savedInstanceState.getLong("next_cursor",-1);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list.toArray(new Long[list.size()]));
        outState.putLong("next_cursor", next_cursor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.dispose();
        adapter=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        subscription.add(
                getResponseSingle(-1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> setRefreshing(false))
                        .subscribe(
                                result-> {
                                    int size = list.size();
                                    list.clear();
                                    adapter.notifyItemRangeRemoved(0, size);

                                    list.addAll(
                                            Observable
                                                    .fromIterable(result)
                                                    .map(User::getId)
                                                    .toList().blockingGet()
                                    );
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
        onInitializeList();
    }

    @Override
    protected void onLoadMoreList() {
        subscription.add(
                getResponseSingle(next_cursor)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    int size = result.size();
                                    if (size > 0) {
                                        int l = list.size();
                                        list.addAll(
                                                Observable
                                                        .fromIterable(result)
                                                        .map(User::getId)
                                                        .toList().blockingGet()
                                        );
                                        adapter.notifyItemRangeInserted(l, size);
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

    public Single<PagableResponseList<User>> getResponseSingle(long cursor) {
        return Single.create(
                subscriber->{
                    try {
                        PagableResponseList<User> pageableResponseList=getResponseList(cursor);
                        next_cursor=pageableResponseList.getNextCursor();
                        GlobalApplication.userCache.addAll(pageableResponseList);
                        subscriber.onSuccess(pageableResponseList);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    public abstract PagableResponseList<User> getResponseList(long cursor) throws TwitterException;

    interface GetRecyclerViewPool {
        RecyclerView.RecycledViewPool getUserListViewPool();
    }

}
