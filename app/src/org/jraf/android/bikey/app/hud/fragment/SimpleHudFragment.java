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
package org.jraf.android.bikey.app.hud.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.hud.HudActivity;
import org.jraf.android.util.app.base.BaseFragment;
import org.jraf.android.util.log.wrapper.Log;

public abstract class SimpleHudFragment extends BaseFragment<HudActivity> {
    private TextView mTxtValue;

    public SimpleHudFragment() {
        super();
    }

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
        return R.layout.hud_simple;
    }

    protected Uri getRideUri() {
        if (getCallbacks() == null) {
            Log.w("null callbacks");
            return null;
        }
        return getCallbacks().getRideUri();
    }
}
