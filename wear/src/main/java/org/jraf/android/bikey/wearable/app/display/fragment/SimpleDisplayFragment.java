/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.wearable.app.display.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.wearable.app.display.DisplayActivity;
import org.jraf.android.util.app.base.BaseFragment;

public abstract class SimpleDisplayFragment extends BaseFragment<DisplayActivity> {
    private TextView mTxtValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(getLayoutResId(), container, false);
        mTxtValue = (TextView) res.findViewById(R.id.txtValue);
        return res;
    }

    protected void setText(CharSequence text) {
        mTxtValue.setText(text);
    }

    protected void setTextEnabled(boolean enabled) {
        mTxtValue.setEnabled(enabled);
    }

    protected int getLayoutResId() {
        return R.layout.display_simple;
    }
}
