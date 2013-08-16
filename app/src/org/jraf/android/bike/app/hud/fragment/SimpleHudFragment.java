package org.jraf.android.bike.app.hud.fragment;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.bike.R;
import org.jraf.android.bike.app.hud.HudActivity;

public abstract class SimpleHudFragment extends Fragment {
    private TextView mTxtValue;

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

    protected void setTextEnabled(boolean enabled) {
        mTxtValue.setEnabled(enabled);
    }

    protected int getLayoutResId() {
        return R.layout.hud_simple;
    }

    protected Uri getRideUri() {
        return ((HudActivity) getActivity()).getRideUri();
    }
}