package org.jraf.android.bike.backend.log;

import android.net.Uri;

public interface LogListener {
    void onLogAdded(Uri rideUri);
}
