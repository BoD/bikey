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
