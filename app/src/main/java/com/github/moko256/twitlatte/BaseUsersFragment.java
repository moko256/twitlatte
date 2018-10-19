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
import android.view.View;

import com.github.moko256.twitlatte.converter.UserConverterKt;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
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

    private UsersAdapter adapter;
    private List<Long> list;
    private long next_cursor;

    private CompositeDisposable disposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list=new ArrayList<>();
        disposable = new CompositeDisposable();

        if (savedInstanceState != null) {
            long[] l = savedInstanceState.getLongArray("list");
            if(l != null){
                for (long id : l) {
                    list.add(id);
                }
            }
            next_cursor=savedInstanceState.getLong("next_cursor",-1);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view)==0){
                    outRect.top=Math.round(getResources().getDisplayMetrics().density*8f);
                }
            }
        });

        if (getActivity() instanceof BaseTweetListFragment.GetRecyclerViewPool) {
            recyclerView.setRecycledViewPool(((GetRecyclerViewPool) getActivity()).getUserListViewPool());
        }

        adapter=new UsersAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        if(!isInitializedList()){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        int size = list.size();
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        outState.putLongArray("list", array);
        outState.putLong("next_cursor", next_cursor);
    }

    @Override
    public void onDestroyView() {
        recyclerView.swapAdapter(null, true);
        adapter=null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
        list=null;
    }

    @Override
    protected void onInitializeList() {
        setRefreshing(true);
        disposable.add(
                getResponseSingle(-1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> setRefreshing(false))
                        .subscribe(
                                result-> {
                                    int size = list.size();
                                    list.clear();
                                    list.addAll(
                                            Observable
                                                    .fromIterable(result)
                                                    .map(User::getId)
                                                    .toList().blockingGet()
                                    );
                                    if (adapter != null) {
                                        adapter.notifyItemRangeRemoved(0, size);
                                        adapter.notifyItemRangeInserted(0, list.size());
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    notifyErrorBySnackBar(e).show();
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
        disposable.add(
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
                                        if (adapter != null) {
                                            adapter.notifyItemRangeInserted(l, size);
                                        }
                                    }
                                },
                                e -> {
                                    e.printStackTrace();
                                    notifyErrorBySnackBar(e).show();
                                }
                        )
        );
    }

    @Override
    protected boolean isInitializedList() {
        return !list.isEmpty();
    }

    private Single<PagableResponseList<User>> getResponseSingle(long cursor) {
        return Single.create(
                subscriber->{
                    try {
                        PagableResponseList<User> pageableResponseList=getResponseList(cursor);
                        next_cursor=pageableResponseList.getNextCursor();
                        ArrayList<com.github.moko256.twitlatte.entity.User> users = new ArrayList<>(pageableResponseList.size());
                        for (User user : pageableResponseList) {
                            users.add(UserConverterKt.convertToCommonUser(user));
                        }
                        GlobalApplication.userCache.addAll(users);
                        subscriber.onSuccess(pageableResponseList);
                    } catch (TwitterException e) {
                        subscriber.tryOnError(e);
                    }
                }
        );
    }

    protected abstract PagableResponseList<User> getResponseList(long cursor) throws TwitterException;

    interface GetRecyclerViewPool {
        RecyclerView.RecycledViewPool getUserListViewPool();
    }

}
