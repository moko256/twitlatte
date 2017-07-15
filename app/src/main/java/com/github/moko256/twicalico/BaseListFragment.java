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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment extends Fragment implements MoveableTopInterface {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private boolean isProgressCircleLoading = true;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null){
            onRestoreInstanceState(savedInstanceState);
        }

        if (!isInitializedList()){
            onInitializeList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);

        View view=inflater.inflate(getLayoutResourceId(), container ,false);

        recyclerView= view.findViewById(R.id.TLlistView);
        recyclerView.setLayoutManager(initializeRecyclerViewLayoutManager());
        recyclerView.addOnScrollListener(new LoadScrollListener((StaggeredGridLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load() {
                if(isInitializedList()){
                    onLoadMoreList();
                }
            }
        });

        swipeRefreshLayout= view.findViewById(R.id.srl);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(()->{
            if (isInitializedList()){
                onUpdateList();
            }else{
                onInitializeList();
            }
        });

        progressBar = view.findViewById(R.id.loadingProgress);
        updateProgressCircleLoading();

        return view;
    }

    @Override
    public void moveToTop() {
        if (((StaggeredGridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPositions(null)[0]
                < 5) {
            getRecyclerView().smoothScrollToPosition(0);
        } else {
            getRecyclerView().scrollToPosition(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        swipeRefreshLayout=null;
        recyclerView=null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isProgressCircleLoading", isProgressCircleLoading);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        isProgressCircleLoading = savedInstanceState.getBoolean("isProgressCircleLoading", true);
    }

    protected abstract void onInitializeList();
    protected abstract void onUpdateList();
    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager(){
        return new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
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

    protected View getSnackBarParentContainer(){
        Activity parent=getActivity();
        if(parent instanceof GetSnackBarParentContainerId){
            return parent.findViewById(((GetSnackBarParentContainerId) getActivity()).getSnackBarParentContainerId());
        } else {
            return getView();
        }
    }

    public interface GetSnackBarParentContainerId {
        @IdRes
        int getSnackBarParentContainerId();
    }

    protected void setProgressCircleLoading(boolean isLoading){
        isProgressCircleLoading = isLoading;
        updateProgressCircleLoading();
    }

    private void updateProgressCircleLoading(){
        if (progressBar != null) progressBar.setVisibility(isProgressCircleLoading? View.VISIBLE: View.GONE);
    }

}

