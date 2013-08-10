package org.jraf.android.bike.app.ride.edit;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.jraf.android.bike.R;
import org.jraf.android.bike.backend.ride.RideManager;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

public class RideEditActivity extends FragmentActivity {
    private EditText mEdtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_edit);
        mEdtName = (EditText) findViewById(R.id.edtName);
        mEdtName.setOnEditorActionListener(mNameOnEditorActionListener);
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

    private void createRide() {
        new TaskFragment(new Task<RideEditActivity>() {
            private Uri mCreatedRideUri;

            @Override
            protected void doInBackground() throws Throwable {
                String name = getActivity().mEdtName.getText().toString().trim();
                mCreatedRideUri = RideManager.get().create(name);
            }

            @Override
            protected void onPostExecuteOk() {
                setResult(RESULT_OK, new Intent(null, mCreatedRideUri));
                getActivity().finish();
            }
        }).execute(getSupportFragmentManager());
    }

    private OnClickListener mDoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            createRide();
        }
    };

    private OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private OnEditorActionListener mNameOnEditorActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            createRide();
            return true;
        }
    };
}
