# Do not obfuscate
-dontobfuscate

# Resolves some obscure proguard/dex problem that breaks the build
# (See http://stackoverflow.com/a/7587680/15695)
-optimizations !code/allocation/variable

# Keep line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# For PebbleKit
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn javax.inject.**
-dontwarn com.google.common.**

# For retrolambda: https://github.com/evant/gradle-retrolambda
-dontwarn java.lang.invoke.*