package com.github.moko256.twicalico;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/06/26.
 *
 * @author moko256
 */
public class ShowImageActivity extends AppCompatActivity {
    public static String FRAG_MEDIA_ENTITIES="MediaEntities";
    private static String FRAG_POSITION="position";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        MediaEntity[] mediaEntities=(MediaEntity[]) getIntent().getSerializableExtra(FRAG_MEDIA_ENTITIES);
        int position=getIntent().getIntExtra(FRAG_POSITION,0);

        ViewPager pager= (ViewPager) findViewById(R.id.activity_show_image_view_pager);
        pager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager(),mediaEntities,this));
        pager.setCurrentItem(position);

    }

    public static Intent getIntent(Context context, MediaEntity[] entities , int position){
        Intent intent=new Intent(context,ShowImageActivity.class);
        intent.putExtra(FRAG_MEDIA_ENTITIES,entities);
        intent.putExtra(FRAG_POSITION,position);
        return intent;
    }
}
