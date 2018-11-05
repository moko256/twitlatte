# ProGuard rules for release build

# see https://www.guardsquare.com/en/products/proguard/manual/usage/optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!code/removal/advanced,!class/merging/*,field/*,code/*,class/*,method/inlining/*,method/marking/final,method/marking/private

-keepattributes SourceFile,LineNumberTable

-keep,allowoptimization class androidx.appcompat.app.AppCompatViewInflater

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*


-dontwarn com.fasterxml.jackson.databind.**

##---------------Begin: proguard configuration for twitter4j  ----------

-dontwarn twitter4j.**
-keep class twitter4j.AlternativeHttpClientImpl { *; }
-keep,allowoptimization class twitter4j.Logger
-keep class twitter4j.LoggerFactory
-keep,allowoptimization class twitter4j.StdOutLogger
-keep class twitter4j.StdOutLoggerFactory
-keep class twitter4j.conf.PropertyConfigurationFactory

##---------------End: proguard configuration for twitter4j  ----------


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep,allowoptimization class * implements com.google.gson.TypeAdapterFactory
-keep,allowoptimization class * implements com.google.gson.JsonSerializer
-keep,allowoptimization class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ----------


##---------------Begin: proguard configuration for OkHttp  ----------

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
# -dontwarn okhttp3.internal.platform.ConscryptPlatform

##---------------End: proguard configuration for OkHttp  ----------