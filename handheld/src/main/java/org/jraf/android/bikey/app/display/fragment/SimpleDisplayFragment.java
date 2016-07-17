/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.app.display.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.display.DisplayActivity;
import org.jraf.android.util.app.base.BaseFragment;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.ui.graph.GraphView;

public abstract class SimpleDisplayFragment extends BaseFragment<DisplayActivity> {
    private TextView mTxtValue;
    private GraphView mGraValues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(getLayoutResId(), container, false);
        mTxtValue = (TextView) res.findViewById(R.id.txtValue);
        mGraValues = (GraphView) res.findViewById(R.id.graValues);
        if (mGraValues != null) {
            mGraValues.setType(0, GraphView.Type.LINES);
            mGraValues.setColor(0, 0xFFFF0000);
            mGraValues.setType(1, GraphView.Type.LINES);
            mGraValues.setColor(1, 0xFF00FF00);
            mGraValues.setType(2, GraphView.Type.LINES);
            mGraValues.setColor(2, 0xFF0000FF);
        }
        return res;
    }

    protected void setText(CharSequence text) {
        mTxtValue.setText(text);
    }

    protected void setTextEnabled(boolean enabled) {
        mTxtValue.setEnabled(enabled);
    }

    protected void setValues(int index, float[] values) {
        mGraValues.setValues(index, values);
    }

    protected int getLayoutResId() {
        return R.layout.display_simple;
    }

    @Nullable
    protected Uri getRideUri() {
        if (getCallbacks() == null) {
            Log.w("null callbacks");
            return null;
        }
        return getCallbacks().getRideUri();
    }
}
