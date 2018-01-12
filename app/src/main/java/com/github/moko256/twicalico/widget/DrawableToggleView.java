/*
 * Copyright 2018 The twicalico authors
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

package com.github.moko256.twicalico.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.github.moko256.twicalico.R;

/**
 * Created by moko256 on 2017/04/03.
 *
 * @author moko256
 */

public class DrawableToggleView extends AppCompatCheckBox {

    public  DrawableToggleView(Context context){
        this(context, null);
    }

    public DrawableToggleView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkboxStyle);
    }

    public DrawableToggleView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawableToggleView);
        Drawable drawable = VectorDrawableCompat.create(
                context.getResources(),
                array.getResourceId(R.styleable.DrawableToggleView_toggle_drawable, -1),
                context.getTheme()
        );
        if (drawable != null){
            setButtonDrawable(drawable);
        }
        array.recycle();
    }
}
