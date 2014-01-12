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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.app.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.ride.list.RideListActivity;

public class ShortcutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent result = new Intent();
        Intent shortcutIntent = new Intent(this, RideListActivity.class);
        shortcutIntent.setAction(RideListActivity.ACTION_QUICK_START);
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.ride_list_btnQuickStart));
        ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_quick_start);
        result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        setResult(RESULT_OK, result);
        finish();
    }
}
