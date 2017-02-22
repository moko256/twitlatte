package com.github.moko256.twicalico;

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

    private int[] firstVisibleItems;

    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    public LoadScrollListener(StaggeredGridLayoutManager staggeredGridLayoutManager) {
        this.staggeredGridLayoutManager = staggeredGridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView,int dx, int dy) {
        super.onScrolled(recyclerView,dx,dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = staggeredGridLayoutManager.getItemCount();
        firstVisibleItems=staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);
        int firstVisibleItem = firstVisibleItems[0];

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
            load();
            loading = true;
        }

    }

    public abstract void load();

}
