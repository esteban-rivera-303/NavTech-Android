# Reglas R8 del release (isMinifyEnabled = true).
# Se aplican a todos los módulos (app, core, feature-*).
# Retrofit, OkHttp, Hilt, Firebase y kotlinx.serialization traen sus propias reglas.

# --- Modelos serializados por reflexión (Gson / Moshi) ---
# Los DTOs de red y los objetos persistidos con Gson dependen de los nombres de campo
# y de constructores que solo invoca la librería. Se conservan los miembros, pero las
# clases y métodos siguen ofuscándose.
-keepclassmembers class com.zanaaustralis.** {
    <fields>;
    <init>(...);
}

# Genéricos y anotaciones que leen Gson, Moshi y Retrofit en tiempo de ejecución.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Gson 2.10 no incluye reglas propias: TypeToken necesita su firma genérica.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Moshi KotlinJsonAdapterFactory (feature-kyc) lee los metadatos de Kotlin por reflexión.
-keep class kotlin.Metadata { *; }

# Interfaz JS del WebView de KYC.
-keepclassmembers class com.zanaaustralis.feature_kyc.presentation.AndroidLogInterface {
    @android.webkit.JavascriptInterface <methods>;
}

# Trazas legibles: se conservan números de línea, ocultando el nombre del archivo fuente.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
