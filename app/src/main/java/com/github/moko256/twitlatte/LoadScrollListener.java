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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by moko256 on 2016/06/05.
 *
 * @author moko256
 */
public abstract class LoadScrollListener extends RecyclerView.OnScrollListener{

    private int previousTotal = 0;
    private boolean loading = true;

    private RecyclerView.LayoutManager layoutManager;

    public LoadScrollListener(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView,int dx, int dy) {
        super.onScrolled(recyclerView,dx,dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem;
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            firstVisibleItem = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null)[0];
        } else {
            firstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
            load();
            loading = true;
        }

    }

    public abstract void load();

}
