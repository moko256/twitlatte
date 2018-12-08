/*
 * Copyright 2015-2018 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.moko256.core.client.base.entity.Media;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import static com.github.moko256.core.client.base.ApiClientKt.CLIENT_TYPE_NOTHING;

/**
 * Created by moko256 on 2016/06/26.
 *
 * @author moko256
 */
public class ShowMediasActivity extends AppCompatActivity {
    private static final String FRAG_MEDIA_ENTITIES="MediaEntities";
    private static final String FRAG_CLIENT_TYPE="client_type";
    private static final String FRAG_POSITION="position";

    private List<Media> mediaEntities;

    private ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        mediaEntities = (List<Media>) getIntent().getSerializableExtra(FRAG_MEDIA_ENTITIES);

        int position=getIntent().getIntExtra(FRAG_POSITION,0);

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        pager= findViewById(R.id.activity_show_image_view_pager);
        pager.setAdapter(
                new MediasAdapter(
                        getSupportFragmentManager(),
                        mediaEntities,
                        getIntent().getIntExtra(FRAG_CLIENT_TYPE, CLIENT_TYPE_NOTHING)
                )
        );
        pager.setCurrentItem(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager=null;
        mediaEntities=null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static Intent getIntent(Context context, Media[] entities, int clientType, int position){
        return new Intent(context,ShowMediasActivity.class)
                .putExtra(FRAG_MEDIA_ENTITIES, (Serializable) Arrays.asList(entities))
                .putExtra(FRAG_CLIENT_TYPE, clientType)
                .putExtra(FRAG_POSITION,position);
    }
}
