package org.jraf.android.bikey.app.ride.list;

import android.support.v4.app.Fragment;

import org.jraf.android.bikey.backend.export.Exporter;

public class RideListStateFragment extends Fragment {
    /*package*/Exporter mExporter;

    public RideListStateFragment() {
        setRetainInstance(true);
    }
}
