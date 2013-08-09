package org.jraf.android.bike.backend.provider;

public enum RideState {

    CREATED(0);

    private int mValue;

    RideState(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
