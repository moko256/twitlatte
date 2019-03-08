/*
 * Copyright 2015-2019 The twitlatte authors
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

package com.github.moko256.twitlatte.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * Created by moko256 on 2019/03/08.
 *
 * @author moko256
 */
class PercentBarBackgroundDrawable: Drawable() {
    var percent: Float = 100f //0..100
    var lineSize = 4f
    var color = 0x000000

    override fun draw(canvas: Canvas) {
        if (percent > 0) {
            val halfLineSize = lineSize / 2
            canvas.drawRect(
                    Rect(
                            halfLineSize.toInt(),
                            lineSize.toInt(),
                            Math.round(percent * bounds.width() / 100 - halfLineSize),
                            bounds.height() - lineSize.toInt()
                    ),
                    Paint().also {
                        it.style = Paint.Style.STROKE
                        it.strokeWidth = lineSize
                        it.pathEffect = CornerPathEffect(lineSize)
                        it.color = color
                    }
            )
        }
    }

    override fun setAlpha(alpha: Int) {
        //TODO("not implemented")
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // Do nothing
    }
}