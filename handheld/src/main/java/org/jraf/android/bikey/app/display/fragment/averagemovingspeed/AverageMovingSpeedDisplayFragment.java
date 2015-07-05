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
package org.jraf.android.bikey.app.display.fragment.averagemovingspeed;

import android.net.Uri;

import org.jraf.android.bikey.app.display.fragment.LogDisplayFragment;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.common.UnitUtil;

public class AverageMovingSpeedDisplayFragment extends LogDisplayFragment {
    public static AverageMovingSpeedDisplayFragment newInstance() {
        return new AverageMovingSpeedDisplayFragment();
    }

    @Override
    protected CharSequence queryValue() {
        Uri rideUri = getRideUri();
        if (rideUri == null) return "";
        return UnitUtil.formatSpeed((float) LogManager.get().getAverageMovingSpeed(rideUri));
    }
}
