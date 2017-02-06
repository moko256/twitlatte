package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        recyclerView=(RecyclerView) view.findViewById(R.id.TLlistView);
        recyclerView.setLayoutManager(initializeRecyclerViewLayoutManager());
        recyclerView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load() {
                if(isInitializedList()){
                    onLoadMoreList();
                }
            }
        });

        swipeRefreshLayout=(SwipeRefreshLayout) view.findViewById(R.id.srl);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(()->{
            if (isInitializedList()){
                onUpdateList();
            }else{
                onInitializeList();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        swipeRefreshLayout=null;
        recyclerView=null;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){}

    protected abstract void onInitializeList();
    protected abstract void onUpdateList();
    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager(){
        return new LinearLayoutManager(getContext());
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

    protected void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }
}

