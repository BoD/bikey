# For Butterknife
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

# For Google Play Services
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# For PebbleKit
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn javax.inject.**


# For ACRA
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep class org.acra.ACRA {
    *;
}
-keep class org.acra.ReportingInteractionMode {
    *;
}
-keepnames class org.acra.sender.HttpSender$** {
    *;
}
-keepnames class org.acra.ReportField {
    *;
}
-keep public class org.acra.ErrorReporter
{
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}
-keep public class org.acra.ErrorReporter
{
    public void handleSilentException(java.lang.Throwable);
}