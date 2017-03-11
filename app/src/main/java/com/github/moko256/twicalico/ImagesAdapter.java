package com.github.moko256.twicalico;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by moko256 on 2017/03/11.
 *
 * @author moko256
 */

public class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD = 2;

    Context context;

    private ArrayList<Uri> images=new ArrayList<>();
    private int limit = 0;

    private View.OnClickListener onAddButtonClickListener;

    public ImagesAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < limit) && (position < images.size())? VIEW_TYPE_IMAGE: VIEW_TYPE_ADD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE){
            return new ImageChildViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_images_adapter_image_child, parent, false));
        } else {
            return new AddImageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_images_adapter_add_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE){
            ImageChildViewHolder viewHolder = (ImageChildViewHolder) holder;
            Uri image = images.get(position);
            viewHolder.title.setText(image.getLastPathSegment());
            Glide.with(context).load(image).into(viewHolder.image);
        } else {
            holder.itemView.setOnClickListener(onAddButtonClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return images.size()<limit? images.size() + 1: limit;
    }


    public View.OnClickListener getOnAddButtonClickListener() {
        return onAddButtonClickListener;
    }

    public void setOnAddButtonClickListener(View.OnClickListener onAddButtonClickListener) {
        this.onAddButtonClickListener = onAddButtonClickListener;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public ArrayList<Uri> getImagesList() {
        return images;
    }

    public void clearImages() {
        images.clear();
        images = null;
    }


    class ImageChildViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;

        ImageChildViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.layout_images_adapter_image_child_image);
            title = (TextView) itemView.findViewById(R.id.layout_images_adapter_image_child_title);
        }
    }

    class AddImageViewHolder extends RecyclerView.ViewHolder{

        AddImageViewHolder(View itemView) {
            super(itemView);
        }
    }
}
