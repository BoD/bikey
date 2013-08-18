package org.jraf.android.bikey.app.ride.list;

import android.content.Context;
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
    public RideAdapter(Context context) {
        //        super(context, android.R.layout.simple_list_item_2, null, 0);
        super(context, android.R.layout.simple_list_item_activated_2, null, 0);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RideCursorWrapper c = (RideCursorWrapper) cursor;

        // Name or date
        TextView txtText1 = ViewHolder.get(view, android.R.id.text1);
        String name = c.getName();
        long createdDateLong = c.getCreatedDate();
        String createdDateTimeStr = DateUtils.formatDateTime(context, createdDateLong, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        if (name == null) {
            txtText1.setText(createdDateTimeStr);
        } else {
            txtText1.setText(name + "  -  " + createdDateTimeStr);
        }

        // Details
        TextView txtText2 = ViewHolder.get(view, android.R.id.text2);
        String details = "";

        // Distance
        double distance = c.getDistance();
        details += UnitUtil.formatDistance((float) distance, true) + "  -  ";

        // Duration
        RideState rideState = RideState.from(c.getState().intValue());
        if (rideState == RideState.CREATED) {
            details += context.getString(R.string.ride_list_notStarted);
        } else {
            long duration = c.getDuration();
            if (rideState == RideState.ACTIVE) {
                long activatedDate = c.getActivatedDate();
                long additionalDuration = System.currentTimeMillis() - activatedDate;
                duration += additionalDuration;
            }
            details += DateTimeUtil.formatDuration(context, duration);
        }
        txtText2.setText(details);
    }

}
