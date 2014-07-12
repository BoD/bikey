/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.text.TextUtils;

public class Path {
    private final Path mParent;
    private final String mName;


    public Path(Path parent, String name) {
        mParent = parent;
        mName = name;
    }

    @Override
    public String toString() {
        List<String> pathList = new ArrayList<>();
        Path path = this;
        while (path != null) {
            pathList.add(path.mName);
            path = path.mParent;
        }
        Collections.reverse(pathList);
        String res = TextUtils.join("/", pathList);
        res = "/" + res;
        return res;
    }

    public boolean matches(String path) {
        return toString().equals(path);
    }

    public static class Notif extends Path {
        private static final Path PARENT = new Path(null, "notif");

        public static Path SHOW = new Path(PARENT, "show");
        public static Path HIDE = new Path(PARENT, "hide");

        public Notif(Path parent, String name) {
            super(parent, name);
        }
    }
}
