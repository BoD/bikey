package org.jraf.android.bike.app.ride.edit;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.provider.RideColumns;
import org.jraf.android.bike.backend.provider.RideState;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

public class RideEditActivity extends FragmentActivity {
    private EditText edtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_edit);
        edtName = (EditText) findViewById(R.id.edtName);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        View customActionBarView = getLayoutInflater().inflate(R.layout.ride_edit_actionbar, null);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View btnDone = customActionBarView.findViewById(R.id.actionbar_done);
        btnDone.setOnClickListener(mDoneOnClickListener);

        View btnDiscard = customActionBarView.findViewById(R.id.actionbar_discard);
        btnDiscard.setOnClickListener(mDiscardOnClickListener);
    }

    private OnClickListener mDoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new TaskFragment(new Task<RideEditActivity>() {
                private Uri mCreatedRideUri;

                @Override
                protected void doInBackground() throws Exception {
                    String name = getActivity().edtName.getText().toString().trim();
                    ContentValues values = new ContentValues(3);
                    values.put(RideColumns.CREATED_DATE, System.currentTimeMillis());
                    if (!name.isEmpty()) {
                        values.put(RideColumns.NAME, name);
                    }
                    values.put(RideColumns.STATE, RideState.CREATED.getValue());
                    mCreatedRideUri = getContentResolver().insert(RideColumns.CONTENT_URI, values);
                }

                @Override
                protected void onPostExecuteOk() {
                    setResult(RESULT_OK, new Intent(null, mCreatedRideUri));
                    getActivity().finish();
                }
            }).execute(getSupportFragmentManager());
        }
    };

    private OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
