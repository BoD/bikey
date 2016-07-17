# Do not obfuscate
-dontobfuscate

# Resolves some obscure proguard/dex problem
-optimizations !code/allocation/variable

# Keep line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# For PebbleKit
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn javax.inject.**
-dontwarn com.google.common.**