# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -------------------------
# APP / ARGOS
# -------------------------

# Mantener clases usadas por reflexión si tienes navegación dinámica,
# deep links, factories o nombres cargados por string.
-keep class com.zanaaustralis.argos.**Screen { *; }
-keep class com.zanaaustralis.argos.**Activity { *; }
-keep class com.zanaaustralis.argos.**Fragment { *; }

# Mantener modelos serializables si usas Gson con reflexión.
-keep class com.zanaaustralis.core.data.remote.dto.** { *; }
-keep class com.zanaaustralis.core.domain.model.** { *; }

-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Si usas enums en requests/responses.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Eliminar logs en release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}