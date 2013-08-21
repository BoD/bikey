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
package org.jraf.android.util;

public class Log {
    private static class CallerInfo {
        public String tag;
        public String method;
    }

    public static void d() {
        CallerInfo callerInfo = getCallerInfo();
        android.util.Log.d(callerInfo.tag, callerInfo.method);
    }

    public static void d(String msg) {
        CallerInfo callerInfo = getCallerInfo();
        android.util.Log.d(callerInfo.tag, callerInfo.method + " " + msg);
    }

    public static void w(String msg) {
        CallerInfo callerInfo = getCallerInfo();
        android.util.Log.w(callerInfo.tag, callerInfo.method + " " + msg);
    }

    public static void w(String msg, Throwable t) {
        CallerInfo callerInfo = getCallerInfo();
        android.util.Log.w(callerInfo.tag, callerInfo.method + " " + msg, t);
    }

    public static void e(String msg, Throwable t) {
        CallerInfo callerInfo = getCallerInfo();
        android.util.Log.e(callerInfo.tag, callerInfo.method + " " + msg, t);
    }


    private static CallerInfo getCallerInfo() {
        CallerInfo res = new CallerInfo();
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        res.tag = element.getClassName();
        res.tag = res.tag.substring(res.tag.lastIndexOf('.') + 1);
        res.method = element.getMethodName();
        return res;
    }
}
