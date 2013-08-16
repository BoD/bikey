package org.jraf.android.bikey.backend.log;

import android.net.Uri;

public interface LogListener {
    void onLogAdded(Uri rideUri);
}
