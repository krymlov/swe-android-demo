# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

##---------------Begin: proguard configuration common for all Android apps ----------
-verbose

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class **.R$*

# Explicitly preserve all serialization members.
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class org.apache.harmony.beans.** { *; }

-dontwarn java.awt.Color
-dontwarn java.awt.Component
-dontwarn java.awt.Font
-dontwarn java.awt.Graphics
-dontwarn java.awt.Image
-dontwarn java.awt.Panel
-dontwarn java.awt.Rectangle
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
-dontwarn sun.reflect.ReflectionFactory