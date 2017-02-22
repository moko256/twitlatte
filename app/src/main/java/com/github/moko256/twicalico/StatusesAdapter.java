package com.github.moko256.twicalico;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.ViewHolder> {

    private ArrayList<Status> data;
    private Context context;

    StatusesAdapter(Context context, ArrayList<Status> data) {
        this.context = context;
        this.data = data;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        Status status=data.get(position);
        Status item = status.isRetweet()?status.getRetweetedStatus():status;
        AppConfiguration conf=GlobalApplication.configuration;
        if((conf.isPatternTweetMuteEnabled() && item.getText().matches(conf.getTweetMutePattern())) ||
                (conf.isPatternUserScreenNameMuteEnabled() && item.getUser().getScreenName().matches(conf.getUserScreenNameMutePattern())) ||
                (conf.isPatternUserNameMuteEnabled() && item.getUser().getName().matches(conf.getUserNameMutePattern())) ||
                (conf.isPatternTweetSourceMuteEnabled() && item.getSource().matches(conf.getTweetSourceMutePattern()))
                ){
            return 1;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        /*
        if (i==1){
            ViewHolder vh=new ViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
            vh.tweetContext.setTextColor(context.getResources().getColor(R.color.imageToggleDisable));
            vh.tweetContext.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            return vh;
        }
        */
        return new ViewHolder(new StatusView(context));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.statusView.setStatus(data.get(i));
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.statusView.setStatus(null);
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        StatusView statusView;

        ViewHolder(final View itemView) {
            super(itemView);

            statusView= (StatusView) itemView;
        }
    }
}
