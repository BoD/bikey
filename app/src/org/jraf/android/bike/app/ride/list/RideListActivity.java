package org.jraf.android.bike.app.ride.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.ride.edit.RideEditActivity;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;

public class RideListActivity extends FragmentActivity implements AlertDialogListener {
    private static final int DIALOG_CONFIRM_DELETE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_list);
        setTitle(R.string.ride_list_title);
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
}
