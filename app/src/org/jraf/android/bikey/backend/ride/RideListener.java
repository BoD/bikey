package org.jraf.android.bikey.backend.ride;

import android.net.Uri;

public interface RideListener {
    void onActivated(Uri rideUri);

    void onPaused(Uri rideUri);
}
