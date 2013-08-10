package org.jraf.android.bike.app.ride.list;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.provider.RideCursorWrapper;

public class RideListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private RideAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new RideAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.ride_list_empty));

        ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(R.string.ride_list_title);
                int quantity = getListView().getCheckedItemCount();
                mode.setSubtitle(getResources().getQuantityString(R.plurals.ride_list_cab_subtitle, quantity, quantity));
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.ride_list_contextual, menu);
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int quantity = getListView().getCheckedItemCount();
                mode.setSubtitle(getResources().getQuantityString(R.plurals.ride_list_cab_subtitle, quantity, quantity));
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ((RideListActivity) getActivity()).showDeleteDialog(getListView().getCheckedItemIds());
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((RideListActivity) getActivity()).onRideSelected(id);
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
        mAdapter.swapCursor(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
