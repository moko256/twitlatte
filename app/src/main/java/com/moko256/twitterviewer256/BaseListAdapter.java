package com.moko256.twitterviewer256;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;

/**
 * Created by moko256 on 2016/10/09.
 *
 * @author moko256
 */

abstract class BaseListAdapter<I,V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    protected LayoutInflater inflater;
    protected ArrayList<I> data;
    protected Context context;

    BaseListAdapter(Context context, ArrayList<I> data) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.data = data;
    }

}
