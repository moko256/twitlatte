# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\koutaro\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable

-dontwarn twitter4j.**
-keep,allowoptimization class twitter4j.* { *; }
-keep,allowoptimization class twitter4j.conf.PropertyConfigurationFactory

-keep,allowoptimization class androidx.appcompat.app.AppCompatViewInflater

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

-dontwarn okio.**


# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep,allowoptimization class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep,allowoptimization class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**


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