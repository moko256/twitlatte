/*
 * Copyright 2015-2019 The twitlatte authors
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment extends Fragment implements LoadScrollListener.OnLoadListener, MovableTopInterface, SwipeRefreshLayout.OnRefreshListener {

    protected RecyclerView recyclerView;
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRefreshAvailable;

    private boolean isProgressCircleLoading = false;

    private View viewForSnackBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        FragmentActivity activity = getActivity();

        if (activity instanceof HasRefreshLayoutInterface) {
            view = inflater.inflate(R.layout.fragment_base_list, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_base_list_refreshable, container, false);
            swipeRefreshLayout = view.findViewById(R.id.srl);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        }

        if (activity instanceof HasNotifiableAppBar) {
            adapterDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    if (positionStart == 0) {
                        ((HasNotifiableAppBar) requireActivity()).requestAppBarCollapsed();
                    }
                }
            };
        }

        recyclerView = view.findViewById(R.id.TLlistView);
        recyclerView.setLayoutManager(initializeRecyclerViewLayoutManager());
        recyclerView.addOnScrollListener(new LoadScrollListener(recyclerView.getLayoutManager(), this));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof GetViewForSnackBar) {
            viewForSnackBar = ((GetViewForSnackBar) activity).getViewForSnackBar();
        }
        if (activity instanceof HasRefreshLayoutInterface) {
            swipeRefreshLayout = ((HasRefreshLayoutInterface) activity).get();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isRefreshAvailable = true;
        swipeRefreshLayout.setRefreshing(isProgressCircleLoading);
        swipeRefreshLayout.setOnRefreshListener(this);
        if (adapterDataObserver != null) {
            recyclerView.getAdapter().registerAdapterDataObserver(adapterDataObserver);
        }

        if (!isInitializedList()) {
            onInitializeList();
        }
    }

    @Override
    public void onPause() {
        if (adapterDataObserver != null) {
            recyclerView.getAdapter().unregisterAdapterDataObserver(adapterDataObserver);
        }

        isRefreshAvailable = false;
        super.onPause();
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

        swipeRefreshLayout = null;
        recyclerView = null;
    }

    @Override
    public void onBottomLoad() {
        if (isInitializedList()) {
            onLoadMoreList();
        }
    }

    @Override
    public void onRefresh() {
        if (isInitializedList()) {
            onUpdateList();
        } else {
            onInitializeList();
        }
    }

    protected abstract void onInitializeList();

    protected abstract void onUpdateList();

    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setRecycleChildrenOnDetach(true);
        return layoutManager;
    }

    protected boolean isRefreshing() {
        if (swipeRefreshLayout == null || !isRefreshAvailable) {
            return isProgressCircleLoading;
        } else {
            return swipeRefreshLayout.isRefreshing();
        }
    }

    protected void setRefreshing(boolean b) {
        isProgressCircleLoading = b;
        if (swipeRefreshLayout != null && isRefreshAvailable) {
            swipeRefreshLayout.setRefreshing(b);
        }
    }

    protected Snackbar notifyBySnackBar(int stringId) {
        return Snackbar.make(
                viewForSnackBar,
                stringId,
                Snackbar.LENGTH_SHORT
        );
    }

    protected Snackbar notifyErrorBySnackBar(Throwable e) {
        return Snackbar.make(
                viewForSnackBar,
                e.getMessage(),
                Snackbar.LENGTH_LONG
        );
    }

    protected int getFirstVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
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

