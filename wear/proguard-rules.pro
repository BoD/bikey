# Do not obfuscate
-dontobfuscate

# Resolves some obscure proguard/dex problem
-optimizations !code/allocation/variable

# Keep line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

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
-optimizations !code/allocation/variable
