package com.github.moko256.twicalico;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */
public abstract class BaseListFragment<A extends BaseListAdapter<LI,? extends RecyclerView.ViewHolder>,LI> extends Fragment {

    private A adapter;
    private ArrayList<LI> list;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);

        View view=inflater.inflate(getLayoutResourceId(), container ,false);

        list=new ArrayList<>();
        adapter= initializeListAdapter(getContext(),list);
        recyclerView=(RecyclerView) view.findViewById(R.id.TLlistView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new LoadScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void load(int page) {
                if(isInitializedList()){
                    onLoadMoreList();
                }
            }
        });

        swipeRefreshLayout=initializeSwipeRefreshLayout(view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(()->{
            if (isInitializedList()){
                onUpdateList();
            }else{
                onInitializeList();
            }
        });

        if (savedInstanceState != null){
            ArrayList<LI> l=(ArrayList<LI>) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(l);
                adapter.notifyDataSetChanged();
            }
            else onInitializeList();
        }else{
            onInitializeList();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putSerializable("list", (ArrayList) list);
        super.onSaveInstanceState(outState);
    }

    protected abstract void onInitializeList();
    protected abstract void onUpdateList();
    protected abstract void onLoadMoreList();

    protected abstract boolean isInitializedList();

    protected abstract A initializeListAdapter(Context context, ArrayList<LI> data);

    protected SwipeRefreshLayout initializeSwipeRefreshLayout(View parent){
        return (SwipeRefreshLayout)parent.findViewById(R.id.srl);
    }

    @LayoutRes
    public int getLayoutResourceId(){
        return R.layout.fragment_base_list;
    }

    public A getListAdapter(){
        return adapter;
    }

    public ArrayList<LI> getContentList() {
        return list;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
}

