/*
 * Copyright 2016 The twicalico authors
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

import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
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

    CompositeSubscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        subscription = new CompositeSubscription();
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
            ArrayList l=(ArrayList) savedInstanceState.getSerializable("list");
            if(l!=null){
                Long longs[] = new Long[l.size()];
                for (int i = 0; i < longs.length; i++) {
                    longs[i] = (Long)l.get(i);
                }
                list.addAll(Arrays.asList(longs));
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
        subscription.unsubscribe();
        subscription = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        subscription.add(
                getResponseSingle(-1)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result-> {
                                    list.addAll(
                                            Observable
                                                    .from(result)
                                                    .map(User::getId)
                                                    .toList().toSingle().toBlocking().value()
                                    );
                                    adapter.notifyDataSetChanged();
                                    getSwipeRefreshLayout().setRefreshing(false);
                                    setProgressCircleLoading(false);
                                },
                                e -> {
                                    e.printStackTrace();
                                    Snackbar.make(getSnackBarParentContainer(), TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.retry, v -> onInitializeList())
                                            .show();
                                }
                        )
        );
    }

    @Override
    protected void onUpdateList() {
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void onLoadMoreList() {
        subscription.add(
                getResponseSingle(next_cursor)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    int size = result.size();
                                    if (size > 0) {
                                        int l = list.size();
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

    public Single<PagableResponseList<User>> getResponseSingle(long cursor) {
        return Single.create(
                subscriber->{
                    try {
                        PagableResponseList<User> pagableResponseList=getResponseList(cursor);
                        next_cursor=pagableResponseList.getNextCursor();
                        GlobalApplication.userCache.addAll(pagableResponseList);
                        subscriber.onSuccess(pagableResponseList);
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    public abstract PagableResponseList<User> getResponseList(long cursor) throws TwitterException;

}
