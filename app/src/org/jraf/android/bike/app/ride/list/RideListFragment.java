package org.jraf.android.bike.app.ride.list;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.provider.RideCursorWrapper;

public class RideListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private RideAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RideAdapter(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.ride_list_empty));
    }


    /*
     * LoaderCallbacks implementation.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), RideColumns.CONTENT_URI, null, null, null, RideColumns.CREATED_DATE + " desc") {
            @Override
            public Cursor loadInBackground() {
                return new RideCursorWrapper(super.loadInBackground());
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getListAdapter() == null) setListAdapter(mAdapter);
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
