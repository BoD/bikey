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
package org.jraf.android.bikey.app.ride.map;

import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.log.LogManager;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RideMapActivity extends FragmentActivity {
    private Uri mRideUri;

    @InjectView(R.id.conMap)
    protected FrameLayout mConMap;

    @InjectView(R.id.vieStatusBarTint)
    protected View mVieStatusBarTint;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_map);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mRideUri = getIntent().getData();

        ButterKnife.inject(this);
        tintedStatusBarHack();

        // Add a padding since the map is below the action bar (and status bar / nav bar on >= kitkat)
        int statusBarHeight = 0;
        int navigationBarHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            statusBarHeight = getStatusBarHeight();
            navigationBarHeight = getNavigationBarHeight();
        }
        getMap().setPadding(0, getActioBarHeight() + statusBarHeight, 0, navigationBarHeight);

        updateMapType();

        loadData();
    }

    private void updateMapType() {
        String mapType = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_RIDE_MAP_TYPE, Constants.PREF_RIDE_MAP_TYPE_NORMAL);
        switch (mapType) {
            case Constants.PREF_RIDE_MAP_TYPE_NORMAL:
                getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case Constants.PREF_RIDE_MAP_TYPE_SATELLITE:
                getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case Constants.PREF_RIDE_MAP_TYPE_TERRAIN:
                getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ride_map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String mapType = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_RIDE_MAP_TYPE, Constants.PREF_RIDE_MAP_TYPE_NORMAL);
        switch (mapType) {
            case Constants.PREF_RIDE_MAP_TYPE_NORMAL:
                menu.findItem(R.id.action_map_normal).setChecked(true);
                break;
            case Constants.PREF_RIDE_MAP_TYPE_SATELLITE:
                menu.findItem(R.id.action_map_satellite).setChecked(true);
                break;
            case Constants.PREF_RIDE_MAP_TYPE_TERRAIN:
                menu.findItem(R.id.action_map_terrain).setChecked(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_map_normal:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_RIDE_MAP_TYPE, Constants.PREF_RIDE_MAP_TYPE_NORMAL)
                        .commit();
                invalidateOptionsMenu();
                updateMapType();
                return true;

            case R.id.action_map_satellite:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_RIDE_MAP_TYPE, Constants.PREF_RIDE_MAP_TYPE_SATELLITE)
                        .commit();
                invalidateOptionsMenu();
                updateMapType();
                return true;

            case R.id.action_map_terrain:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_RIDE_MAP_TYPE, Constants.PREF_RIDE_MAP_TYPE_TERRAIN)
                        .commit();
                invalidateOptionsMenu();
                updateMapType();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void tintedStatusBarHack() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mVieStatusBarTint.setVisibility(View.GONE);
        } else {
            mVieStatusBarTint.setVisibility(View.VISIBLE);
            LayoutParams params = mVieStatusBarTint.getLayoutParams();
            params.height = getStatusBarHeight();
            mVieStatusBarTint.setLayoutParams(params);
        }
    }

    private int getActioBarHeight() {
        int res;
        // Retrieve the action bar height from the theme
        TypedArray a = getTheme().obtainStyledAttributes(R.style.Theme_Bikey_Map, new int[] { android.R.attr.actionBarSize });
        res = a.getDimensionPixelSize(0, 0);
        a.recycle();
        return res;
    }

    private int getStatusBarHeight() {
        int res = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            res = getResources().getDimensionPixelSize(resourceId);
        }
        return res;
    }

    private int getNavigationBarHeight() {
        int res = 0;
        if (!hasNavigationBar()) return 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            res = getResources().getDimensionPixelSize(resourceId);
        }
        return res;
    }

    private boolean hasNavigationBar() {
        Resources res = getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);

            return hasNav;
        }
        // Fallback
        return !ViewConfiguration.get(this).hasPermanentMenuKey();
    }

    private void loadData() {
        new TaskFragment(new Task<RideMapActivity>() {
            private String mName;
            private List<LatLng> mLatLngArray;

            @Override
            protected void doInBackground() throws Throwable {
                RideManager rideManager = RideManager.get();
                Uri rideUri = getActivity().mRideUri;
                RideCursor rideCursor = rideManager.query(rideUri);
                mName = rideCursor.getName();
                rideCursor.close();

                LogManager logManager = LogManager.get();

                mLatLngArray = logManager.getLatLngArray(rideUri, 100);
            }

            @Override
            protected void onPostExecuteOk() {
                RideMapActivity a = getActivity();
                if (mName != null) a.setTitle(mName);

                // Map
                if (mLatLngArray.size() > 0) {
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(mLatLngArray);
                    polylineOptions.color(getResources().getColor(R.color.map_polyline));
                    Polyline polyline = getMap().addPolyline(polylineOptions);
                    // Calculate bounds
                    LatLngBounds bounds = new LatLngBounds(mLatLngArray.get(0), mLatLngArray.get(0));
                    for (LatLng latLng : mLatLngArray) {
                        bounds = bounds.including(latLng);
                    }
                    int padding = getResources().getDimensionPixelSize(R.dimen.ride_detail_map_padding);
                    getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    mConMap.setVisibility(View.VISIBLE);
                }
            }
        }).execute(getSupportFragmentManager());
    }


    /*
     * Map.
     */

    private GoogleMap getMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        return mMap;
    }

}
