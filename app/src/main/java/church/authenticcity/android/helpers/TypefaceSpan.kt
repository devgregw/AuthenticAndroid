package church.authenticcity.android.helpers

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * NOTICE: this file was modified by Greg Whatley on 2/2/2018 at 1:15 PM.
 * Changes include conversion to Kotlin and support for [Typeface]s in the constructor.
 * Because of these changes, some of the original code was removed.
 *
 * See: https://gist.github.com/twaddington/b91341ea5615698b53b8
 */

/*
 * Copyright 2013 Simple Finance Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Style a [Spannable] with a custom [Typeface].
 *
 * @author Tristan Waddington
 */
class TypefaceSpan
/**
 * Load the [Typeface] and apply to a [Spannable].
 */
(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateMeasureState(p: TextPaint) {
        p.typeface = typeface
        // Note: This flag is required for proper typeface rendering
        p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = typeface

        // Note: This flag is required for proper typeface rendering
        tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
    }
}
