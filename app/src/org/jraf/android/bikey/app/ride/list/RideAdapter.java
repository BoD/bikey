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
package org.jraf.android.bikey.app.ride.list;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.RideCursorWrapper;
import org.jraf.android.bikey.backend.provider.RideState;
import org.jraf.android.bikey.util.UnitUtil;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.ui.ViewHolder;

public class RideAdapter extends ResourceCursorAdapter {
    private ColorStateList mColorDefault;
    private ColorStateList mColorActive;

    public RideAdapter(Context context) {
        super(context, R.layout.ride_list_item, null, 0);

        // Retrieve the default text secondary color from the theme
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme, new int[] { android.R.attr.textColorSecondary });
        int resId = a.getResourceId(0, 0);
        a.recycle();
        mColorDefault = context.getResources().getColorStateList(resId);
        mColorActive = context.getResources().getColorStateList(R.color.ride_list_item_active);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RideCursorWrapper c = (RideCursorWrapper) cursor;

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
        String details = null;
        Animator animator = (Animator) view.getTag(R.id.animator);
        // Cancel the animation / reset the alpha in any case
        if (animator != null) animator.cancel();
        txtSummary.setAlpha(1);
        RideState rideState = RideState.from(c.getState().intValue());
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
                double distance = c.getDistance();
                details = UnitUtil.formatDistance((float) distance, true) + "  -  ";

                // Duration
                long duration = c.getDuration();
                details += DateTimeUtil.formatDuration(context, duration);
                txtSummary.setTextColor(mColorDefault);
                txtSummary.setEnabled(true);
                break;
        }
        txtSummary.setText(details);
    }
}
