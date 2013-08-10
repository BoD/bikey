package org.jraf.android.bike.app.hud;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.bike.R;

public abstract class SimpleHudFragment extends Fragment {
    protected TextView mTxtValue;

    public SimpleHudFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(getLayoutResId(), container, false);
        mTxtValue = (TextView) res.findViewById(R.id.txtValue);
        return res;
    }

    protected void setText(CharSequence text) {
        mTxtValue.setText(text);
    }

    protected abstract int getLayoutResId();
}