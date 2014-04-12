/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jraf.android.bikey.R;

public class LabelTextView extends LinearLayout {
    private TextView mTxtLabel;
    private TextView mTxtText;

    public LabelTextView(Context context) {
        super(context);
    }

    public LabelTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);

        View layout = LayoutInflater.from(context).inflate(R.layout.label_text, this, true);
        mTxtLabel = (TextView) layout.findViewById(R.id.txtLabel);
        mTxtText = (TextView) layout.findViewById(R.id.txtText);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Android);
        CharSequence label = a.getText(R.styleable.Android_android_label);
        CharSequence text = a.getText(R.styleable.Android_android_text);
        a.recycle();

        setLabel(label);
        setText(text);
    }

    public void setLabel(int resid) {
        setLabel(getContext().getResources().getText(resid));
    }

    public final void setLabel(CharSequence text) {
        mTxtLabel.setText(text);
    }

    public void setText(int resid) {
        setText(getContext().getResources().getText(resid));
    }

    public final void setText(CharSequence text) {
        mTxtText.setText(text);
    }
}