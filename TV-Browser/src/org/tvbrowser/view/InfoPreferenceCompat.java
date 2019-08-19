/*
 * TV-Browser for Android
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify or merge the Software,
 * furthermore to publish and distribute the Software free of charge without modifications and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.tvbrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class InfoPreferenceCompat extends Preference {
  public InfoPreferenceCompat(Context context) {
    super(context);
  }

  public InfoPreferenceCompat(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public InfoPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onBindViewHolder(PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);

    TextView summaryView = (TextView)holder.findViewById(android.R.id.summary);
    summaryView.setMaxLines(Integer.MAX_VALUE);
  }
}