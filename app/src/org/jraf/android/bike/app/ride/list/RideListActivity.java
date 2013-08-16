package org.jraf.android.bike.app.ride.list;

import java.io.File;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.HudActivity;
import org.jraf.android.bike.app.ride.edit.RideEditActivity;
import org.jraf.android.bike.backend.export.DbExporter;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;

public class RideListActivity extends FragmentActivity implements AlertDialogListener {
    private static final String FRAGMENT_RETAINED_STATE = "FRAGMENT_RETAINED_STATE";

    private static final int DIALOG_CONFIRM_DELETE = 0;
    private static final int DIALOG_SHARE = 1;
    private RideListStateFragment mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_list);
        setTitle(R.string.ride_list_title);
        restoreState();
    }

    private void restoreState() {
        mState = (RideListStateFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_RETAINED_STATE);
        if (mState == null) {
            mState = new RideListStateFragment();
            getSupportFragmentManager().beginTransaction().add(mState, FRAGMENT_RETAINED_STATE).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ride_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(this, RideEditActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * Ride selected.
     */

    public void onRideSelected(Uri rideUri) {
        startActivity(new Intent(null, rideUri, this, HudActivity.class));
    }


    /*
     * Delete.
     */

    public void showDeleteDialog(long[] checkedItemIds) {
        int quantity = checkedItemIds.length;
        String message = getResources().getQuantityString(R.plurals.ride_list_deleteDialog_message, quantity, quantity);

        AlertDialogFragment.newInstance(DIALOG_CONFIRM_DELETE, null, message, 0, getString(android.R.string.ok), getString(android.R.string.cancel),
                checkedItemIds).show(getSupportFragmentManager());
    }

    @Override
    public void onClickPositive(int tag, Object payload) {
        switch (tag) {
            case DIALOG_CONFIRM_DELETE:
                RideManager.get().delete((long[]) payload);
                break;
        }
    }

    @Override
    public void onClickNegative(int tag, Object payload) {}


    /*
     * Share.
     */

    public void showShareDialog(long[] checkedItemIds) {
        AlertDialogFragment.newInstance(DIALOG_SHARE, R.string.ride_list_shareDialog_title, 0, R.array.export_choices, 0, 0, checkedItemIds[0]).show(
                getSupportFragmentManager());
    }

    @Override
    public void onClickListItem(int tag, int index, Object payload) {
        Uri rideUri = ContentUris.withAppendedId(RideColumns.CONTENT_URI, (Long) payload);
        switch (index) {
            case 0:
                // Database
                mState.mExporter = new DbExporter(rideUri);
                break;
        }
        startExport();
    }

    private void startExport() {
        new TaskFragment(new Task<RideListActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                getActivity().mState.mExporter.export();
            }

            @Override
            protected void onPostExecuteOk() {
                File exportedFile = getActivity().mState.mExporter.getExportFile();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject));
                String messageBody = getString(R.string.export_body);
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + exportedFile.getAbsolutePath()));
                //                sendIntent.setType("message/rfc822");
                sendIntent.setType("application/bikey");
                sendIntent.putExtra(Intent.EXTRA_TEXT, messageBody);

                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.ride_list_action_share)));

            }
        }.toastFail(R.string.export_failToast)).execute(getSupportFragmentManager());
    }
}
