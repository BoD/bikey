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

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
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

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.backend.provider.ride.RideCursorWrapper;

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
                // Enable share / edit if only one item is selected (can't share / edit several items at the same time)
                mode.getMenu().findItem(R.id.action_share).setVisible(quantity == 1);
                mode.getMenu().findItem(R.id.action_edit).setVisible(quantity == 1);
                // Enable merge only if several items are selected
                mode.getMenu().findItem(R.id.action_merge).setVisible(quantity > 1);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                long[] checkedItemIds = getListView().getCheckedItemIds();
                long checkedItemId = checkedItemIds[0];
                Uri checkedItemUri = ContentUris.withAppendedId(RideColumns.CONTENT_URI, checkedItemId);
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        getCallbacks().showDeleteDialog(checkedItemIds);
                        mode.finish();
                        return true;

                    case R.id.action_share:
                        getCallbacks().showShareDialog(checkedItemUri);
                        mode.finish();
                        return true;

                    case R.id.action_edit:
                        getCallbacks().edit(checkedItemUri);
                        mode.finish();
                        return true;

                    case R.id.action_merge:
                        getCallbacks().showMergeDialog(checkedItemIds);
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
        getCallbacks().onRideSelected(ContentUris.withAppendedId(RideColumns.CONTENT_URI, id));
    }

    private RideListCallbacks getCallbacks() {
        return (RideListCallbacks) getActivity();
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
