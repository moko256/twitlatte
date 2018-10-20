# ProGuard rules for debug build

-ignorewarnings

-keep class twitter4j.* { *; }
-keep class twitter4j.conf.PropertyConfigurationFactory

-keep class com.github.moko256.twitlatte.** { *; }

-keep class androidx.appcompat.app.AppCompatViewInflater


-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase