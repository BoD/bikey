/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.backend.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractSelection <T extends AbstractSelection<?>> {
    private static final String SPACE = " ";
    private static final String EQ = "=?";
    private static final String PAREN_OPEN = "(";
    private static final String PAREN_CLOSE = ")";
    private static final String AND = " and ";
    private static final String OR = " or ";
    private static final String IS_NULL = " is null";
    private static final String IN = " in(";
    private static final String COMMA = ",";
    private static final String GT = ">?";
    private static final String LT = "<?";

    private StringBuilder mSelection = new StringBuilder();
    private List<String> mSelectionArgs = new ArrayList<String>(5);

    protected void addEquals(String column, Object... value) {
        mSelection.append(column);

        if (value == null) {
            // Single null value
            mSelection.append(IS_NULL);
        } else if (value.length > 1) {
            // Multiple values ('in' clause)
            mSelection.append(IN);
            for (int i = 0; i < value.length; i++) {
                mSelection.append(valueOf(value[i]));
                if (i < value.length - 1) {
                    mSelection.append(COMMA);
                }
            }
            mSelection.append(PAREN_CLOSE);
        } else {
            // Single value
            mSelection.append(EQ);
            mSelectionArgs.add(valueOf(value[0]));
        }
    }

    protected void addGreaterThan(String column, Object value) {
        mSelection.append(column);
        mSelection.append(GT);
        mSelectionArgs.add(valueOf(value));
    }

    protected void addLessThan(String column, Object value) {
        mSelection.append(column);
        mSelection.append(LT);
        mSelectionArgs.add(valueOf(value));
    }

    private String valueOf(Object obj) {
        if (obj instanceof Date) {
            return String.valueOf(((Date) obj).getTime());
        }
        return String.valueOf(obj);
    }

    @SuppressWarnings("unchecked")
    public T openParen() {
        mSelection.append(PAREN_OPEN);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T closeParen() {
        mSelection.append(PAREN_CLOSE);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T and() {
        mSelection.append(AND);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T or() {
        mSelection.append(OR);
        return (T) this;
    }

    /**
     * Returns the selection produced by this .
     */
    public String sel() {
        return mSelection.toString();
    }

    /**
     * Returns the selection arguments produced by this .
     */
    public String[] args() {
        int size = mSelectionArgs.size();
        if (size == 0) return null;
        return mSelectionArgs.toArray(new String[size]);
    }
}