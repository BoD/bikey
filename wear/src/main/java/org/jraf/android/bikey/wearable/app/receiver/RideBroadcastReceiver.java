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
package org.jraf.android.bikey.wearable.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class RideBroadcastReceiver extends BroadcastReceiver {
    private static final String PREFIX = RideBroadcastReceiver.class.getName() + ".";
    public static final String ACTION_PAUSE = PREFIX + "ACTION_PAUSE";
    public static final String ACTION_RESUME = PREFIX + "ACTION_RESUME";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));
    }
}
