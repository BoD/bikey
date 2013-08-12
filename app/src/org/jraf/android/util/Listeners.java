package org.jraf.android.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Listeners<T> implements Iterable<T> {
    private Set<T> mListeners = new HashSet<T>(3);

    public static <T> Listeners<T> newInstance() {
        return new Listeners<T>();
    }

    public boolean add(T listener) {
        int prevSize = mListeners.size();
        boolean res = mListeners.add(listener);
        onListenerCountChanged(prevSize, mListeners.size());
        return res;
    }

    public boolean remove(T listener) {
        int prevSize = mListeners.size();
        boolean res = mListeners.remove(listener);
        onListenerCountChanged(prevSize, mListeners.size());
        return res;
    }

    @Override
    public Iterator<T> iterator() {
        return mListeners.iterator();
    }

    private void onListenerCountChanged(int prevSize, int newSize) {
        if (prevSize == 0 && newSize == 1) {
            onFirstListener();
        } else if (newSize == 0) {
            onNoMoreListeners();
        }
    }

    protected void onFirstListener() {}

    protected void onNoMoreListeners() {}
}
