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
package org.jraf.android.bikey.app.ride.list;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.ride.RideCursor;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.ui.ViewHolder;

public class RideAdapter extends ResourceCursorAdapter {
    private ColorStateList mColorDefault;
    private ColorStateList mColorActive;

    public RideAdapter(Context context) {
        super(context, R.layout.ride_list_item, null, 0);

        // Retrieve the default text secondary color from the theme
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_Bikey, new int[] { android.R.attr.textColorSecondary });
        int resId = a.getResourceId(0, 0);
        a.recycle();
        mColorDefault = context.getResources().getColorStateList(resId);
        mColorActive = context.getResources().getColorStateList(R.color.ride_list_item_active);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RideCursor c = (RideCursor) cursor;

        // Title (name / date)
        TextView txtTitle = ViewHolder.get(view, R.id.txtTitle);
        String name = c.getName();
        long createdDateLong = c.getCreatedDate().getTime();
        String createdDateTimeStr = DateUtils.formatDateTime(context, createdDateLong, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        if (name == null) {
            txtTitle.setText(createdDateTimeStr);
        } else {
            txtTitle.setText(name + "\n" + createdDateTimeStr);
        }

        // Summary
        TextView txtSummary = ViewHolder.get(view, R.id.txtSummary);
        CharSequence details = null;
        Animator animator = (Animator) view.getTag(R.id.animator);
        // Cancel the animation / reset the alpha in any case
        if (animator != null) animator.cancel();
        txtSummary.setAlpha(1);
        RideState rideState = c.getState();
        switch (rideState) {
            case CREATED:
                details = context.getString(R.string.ride_list_notStarted);
                txtSummary.setTextColor(mColorDefault);
                txtSummary.setEnabled(false);
                break;

            case ACTIVE:
                details = context.getString(R.string.ride_list_active);
                if (animator == null) {
                    animator = AnimatorInflater.loadAnimator(context, R.animator.blink);
                    animator.setTarget(txtSummary);
                    view.setTag(R.id.animator, animator);
                }
                animator.start();
                txtSummary.setTextColor(mColorActive);
                txtSummary.setEnabled(true);
                break;

            case PAUSED:
                // Distance
                float distance = c.getDistance();
                details = TextUtils.concat(UnitUtil.formatDistance(distance, true, .85f), "  -  ");

                // Duration
                long duration = c.getDuration();
                details = TextUtils.concat(details, DateTimeUtil.formatDuration(context, duration));
                txtSummary.setTextColor(mColorDefault);
                txtSummary.setEnabled(true);
                break;
        }
        txtSummary.setText(details);
    }
}
