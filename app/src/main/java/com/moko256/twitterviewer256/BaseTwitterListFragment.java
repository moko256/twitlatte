package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by moko256 on GitHub on 2016/03/27.
 */
public abstract class BaseTwitterListFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);

        View view=inflater.inflate(getLayoutResource(), null);

        startProcess(view);

        SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.srl);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(() -> updateProcess(view,swipeRefreshLayout));

        if (savedInstanceState != null){
            restoreProcess(view,savedInstanceState);
        }else{
            initializationProcess(view);
        }

        return view;
    }

    @LayoutRes
    public int getLayoutResource(){
        return R.layout.fragment_base_list;
    }

    public abstract void startProcess(View view);
    public abstract void restoreProcess(View view,Bundle savedInstanceState);
    public abstract void initializationProcess(View view);
    public abstract void updateProcess(View view, SwipeRefreshLayout swipeRefreshLayout);

}

