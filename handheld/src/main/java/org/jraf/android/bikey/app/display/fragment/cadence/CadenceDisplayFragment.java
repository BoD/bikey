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
package org.jraf.android.bikey.app.display.fragment.cadence;

import org.jraf.android.bikey.app.display.fragment.SimpleDisplayFragment;
import org.jraf.android.bikey.backend.cadence.CadenceListener;
import org.jraf.android.bikey.backend.cadence.CadenceManager;
import org.jraf.android.bikey.common.UnitUtil;

public class CadenceDisplayFragment extends SimpleDisplayFragment {
    public static CadenceDisplayFragment newInstance() {
        return new CadenceDisplayFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Cadence
        CadenceManager.get().addListener(mCadenceListener);
    }

    @Override
    public void onStop() {
        // Cadence
        CadenceManager.get().removeListener(mCadenceListener);
        super.onStop();
    }

    private CadenceListener mCadenceListener = new CadenceListener() {
        @Override
        public void onCadenceChanged(Float cadence, float[][] rawData) {
            setText(UnitUtil.formatCadence(cadence));
            setValues(0, rawData[0]);
            setValues(1, rawData[1]);
            setValues(2, rawData[2]);
        }
    };
}
