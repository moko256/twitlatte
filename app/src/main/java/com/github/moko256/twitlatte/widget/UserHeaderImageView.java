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

package com.github.moko256.twitlatte.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.moko256.twitlatte.GlobalApplication;
import com.github.moko256.twitlatte.entity.ClientType;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by moko256 on 2017/07/15.
 *
 * @author moko256
 */

public class UserHeaderImageView extends AppCompatImageView{

    public UserHeaderImageView(Context context) {
        super(context);
    }

    public UserHeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserHeaderImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode != MeasureSpec.EXACTLY){
            mode = MeasureSpec.EXACTLY;
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) / (GlobalApplication.clientType == ClientType.TWITTER? 3 : 2),
                mode
        );
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
