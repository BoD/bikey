package org.jraf.android.bike.backend.provider;

public enum RideState {

    /**
     * Initial state: the ride has been created but has not started yet.
     */
    CREATED(0),

    /**
     * The ride is currently active and being recorded.<br>
     * Only one ride can be active at any time.
     */
    ACTIVE(1),

    /**
     * The ride has been started but recording is currently paused.
     */
    PAUSED(2);


    private int mValue;

    RideState(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public static RideState from(int value) {
        for (RideState rideState : values()) {
            if (rideState.mValue == value) return rideState;
        }
        return null;
    }
}
