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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment extends Fragment implements MovableTopInterface {

    protected RecyclerView recyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;

    private boolean isShowed = false;
    private boolean isProgressCircleLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        isShowed = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_base_list, container ,false);

        recyclerView= view.findViewById(R.id.TLlistView);
        swipeRefreshLayout= view.findViewById(R.id.srl);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.setLayoutManager(initializeRecyclerViewLayoutManager());
        recyclerView.addOnScrollListener(new LoadScrollListener(recyclerView.getLayoutManager()) {
            @Override
            protected void load() {
                if(isInitializedList()){
                    onLoadMoreList();
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setRefreshing(isProgressCircleLoading);
        swipeRefreshLayout.setOnRefreshListener(()->{
            if (isInitializedList()){
                onUpdateList();
            } else {
                onInitializeList();
            }
        });

        if (getUserVisibleHint() && !isInitializedList()){
            onInitializeList();
        }
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

    protected abstract void onInitializeList();
    protected abstract void onUpdateList();
    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setRecycleChildrenOnDetach(true);
        return layoutManager;
    }

    protected void setRefreshing(boolean b){
        isProgressCircleLoading = b;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(b);
        }
    }

    protected Snackbar notifyBySnackBar(int stringId){
        Activity parent = requireActivity();
        return Snackbar.make(
                parent instanceof GetViewForSnackBar
                        ? ((GetViewForSnackBar) parent).getViewForSnackBar()
                        : Objects.requireNonNull(getView()),
                stringId,
                Snackbar.LENGTH_SHORT
        );
    }

    protected Snackbar notifyErrorBySnackBar(Throwable e){
        Activity parent = requireActivity();
        return Snackbar.make(
                parent instanceof GetViewForSnackBar
                        ? ((GetViewForSnackBar) parent).getViewForSnackBar()
                        : Objects.requireNonNull(getView()),
                TwitterStringUtils.convertErrorToText(e),
                Snackbar.LENGTH_LONG
        );
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

    public interface GetViewForSnackBar {
        View getViewForSnackBar();
    }

}

