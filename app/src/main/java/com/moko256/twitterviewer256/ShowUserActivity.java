package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by moko256 on GitHub on 2016/03/11.
 */
public class ShowUserActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.show_user_fragment_container, new ShowUserFragment())
                .commit();

        Toolbar toolbar=(Toolbar) findViewById(R.id.show_user_tool_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        /*
        listAdapter = new TweetListAdapter(ShowUserActivity.this,userTweet,null);
        recyclerView = (RecyclerView) findViewById(R.id.show_user_tweet);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new AsyncTask<Void,Void, User>(){
            @Override
            public User doInBackground(Void... str){
                String userId=(String) getIntent().getSerializableExtra("userName");
                User user=null;
                if(userId!=null){
                    try {
                        user=Static.twitter.showUser(userId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }else {
                    user=(User) getIntent().getSerializableExtra("user");
                }
                return user;
            }

            @Override
            public void onPostExecute(User item){

                Glide.with(ShowUserActivity.this).load(item.getBiggerProfileImageURL()).into((ImageView) findViewById(R.id.show_user_image));
                Glide.with(ShowUserActivity.this).load(item.getProfileBannerRetinaURL()).into((ImageView)findViewById(R.id.show_user_bgimage));

                ((TextView) findViewById(R.id.show_user_bio)).setText(item.getDescription());
                ((Toolbar) findViewById(R.id.show_user_tool_bar)).setTitle(item.getName());

                getApi(item.getId(),new Paging(1,20))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result-> userTweet.addAll(result),
                                Throwable::printStackTrace,
                                ()->listAdapter.notifyDataSetChanged()
                        );

            }
        }.execute();
        */
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    /*
    private Observable<ResponseList<Status>> getApi(Long userId,Paging paging){
        return Observable.create(
                subscriber->{
                    try {
                        subscriber.onNext(Static.twitter.getUserTimeline(userId,paging));
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }
    */

}
