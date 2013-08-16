package org.jraf.android.bikey.app.ride.list;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.jraf.android.bikey.backend.provider.RideCursorWrapper;
import org.jraf.android.bikey.backend.provider.RideState;
import org.jraf.android.util.ui.ViewHolder;

public class RideAdapter extends ResourceCursorAdapter {
    private java.text.DateFormat mDateFormat;
    private java.text.DateFormat mTimeFormat;

    public RideAdapter(Context context) {
        //        super(context, android.R.layout.simple_list_item_2, null, 0);
        super(context, android.R.layout.simple_list_item_activated_2, null, 0);
        mDateFormat = DateFormat.getMediumDateFormat(context);
        mTimeFormat = DateFormat.getTimeFormat(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RideCursorWrapper c = (RideCursorWrapper) cursor;

        // Name / date
        TextView txtText1 = ViewHolder.get(view, android.R.id.text1);
        String name = c.getName();
        long createdDateLong = c.getCreatedDate();
        Date createDateDate = new Date(createdDateLong);
        String createdDateTimeStr = mDateFormat.format(createDateDate) + ", " + mTimeFormat.format(createDateDate);
        if (name == null) {
            txtText1.setText(createdDateTimeStr);
        } else {
            txtText1.setText(name + " (" + createdDateTimeStr + ")");
        }

        // Duration / distance / state
        TextView txtText2 = ViewHolder.get(view, android.R.id.text2);
        Long duration = c.getDuration();
        if (duration == null) duration = 0l;
        Double distance = c.getDistance();
        if (distance == null) distance = 0d;
        RideState rideState = RideState.from(c.getState().intValue());
        txtText2.setText(duration + ", " + distance + ", " + rideState);
    }

}
