package com.github.moko256.twicalico;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by moko256 on 2016/12/21.
 */

public class StringArrayAdapter extends RecyclerView.Adapter<StringArrayAdapter.ViewHolder> {

    private Context context;
    private String[] stringArray;

    public StringArrayAdapter(Context context,String[] array){
        this.context=context;
        stringArray=array;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(stringArray[position]);
    }

    @Override
    public int getItemCount() {
        return stringArray!=null?stringArray.length:0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
