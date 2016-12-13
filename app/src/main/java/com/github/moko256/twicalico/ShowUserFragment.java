package com.github.moko256.twicalico;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

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
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return Static.twitter.getUserTimeline(user.getId(),paging);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_show_user_list;
    }

    @Override
    protected SwipeRefreshLayout initializeSwipeRefreshLayout(View parent) {
        return (SwipeRefreshLayout) getActivity().findViewById(R.id.show_user_fragment_container);
    }

    public interface HasUserActivity{
        Observable<User> getUserObservable();
        void updateHeader(User user);
    }
}