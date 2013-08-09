package org.jraf.android.bike.backend;

public class DataAccess {
    private static final DataAccess INSTANCE = new DataAccess();

    public static DataAccess get() {
        return INSTANCE;
    }

    private boolean mRecording;

    private DataAccess() {}

    public boolean isRecording() {
        return mRecording;
    }
}
