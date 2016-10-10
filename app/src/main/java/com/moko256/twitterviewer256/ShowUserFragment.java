package com.moko256.twitterviewer256;

import android.os.Bundle;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by moko256 on 2016/06/30.
 *
 * @author moko256
 */
public class ShowUserFragment extends BaseTweetListFragment {

    User user;

    @Override
    public void onInitializeList() {
        HasUserActivity activity=(HasUserActivity)getActivity();
        activity.getUserObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            user=result;
                            activity.updateHeader(result);
                            super.onInitializeList();
                        },
                        throwable->{
                            throwable.printStackTrace();
                            getActivity().finish();
                        },
                        ()->{}
                );
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", user);
    }

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return Static.twitter.getUserTimeline(user.getId(),paging);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_base_list;
    }

    public interface HasUserActivity{
        Observable<User> getUserObservable();
        void updateHeader(User user);
    }
}