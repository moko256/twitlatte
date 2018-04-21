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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment extends Fragment implements MovableTopInterface {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isShowed = false;
    private boolean isProgressCircleLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null){
            onRestoreInstanceState(savedInstanceState);
        }

        isShowed = true;

        if (getUserVisibleHint() && !isInitializedList()){
            onInitializeList();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);

        View view=inflater.inflate(getLayoutResourceId(), container ,false);

        recyclerView= view.findViewById(R.id.TLlistView);
        recyclerView.setLayoutManager(initializeRecyclerViewLayoutManager());
        recyclerView.addOnScrollListener(new LoadScrollListener(recyclerView.getLayoutManager()) {
            @Override
            public void load() {
                if(isInitializedList()){
                    onLoadMoreList();
                }
            }
        });

        swipeRefreshLayout= view.findViewById(R.id.srl);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setRefreshing(isProgressCircleLoading);
        swipeRefreshLayout.setOnRefreshListener(()->{
            if (isInitializedList()){
                onUpdateList();
            } else {
                onInitializeList();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isShowed && isVisibleToUser && !isInitializedList()) {
            onInitializeList();
        }
    }

    @Override
    public void moveToTop() {
        if (getFirstVisibleItemPosition(recyclerView.getLayoutManager()) < 5) {
            recyclerView.smoothScrollToPosition(0);
        } else {
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        swipeRefreshLayout=null;
        recyclerView=null;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){

    }

    protected abstract void onInitializeList();
    protected abstract void onUpdateList();
    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setRecycleChildrenOnDetach(true);
        return layoutManager;
    }

    @LayoutRes
    public int getLayoutResourceId(){
        return R.layout.fragment_base_list;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    protected RecyclerView.Adapter getAdapter() {
        return recyclerView.getAdapter();
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    protected void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    protected void setRefreshing(boolean b){
        isProgressCircleLoading = b;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(b);
        }
    }

    protected Snackbar getSnackBar(String string){
        Activity parent=getActivity();
        if(parent instanceof GetSnackBar){
            return ((GetSnackBar) parent).getSnackBar(string);
        } else {
            return Snackbar.make(getView(), string, Snackbar.LENGTH_LONG);
        }
    }

    protected int getFirstVisibleItemPosition(RecyclerView.LayoutManager layoutManager){
        int position;
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            position = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null)[0];
        } else {
            position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return position;
    }

    public interface GetSnackBar {
        Snackbar getSnackBar(String string);
    }

}

