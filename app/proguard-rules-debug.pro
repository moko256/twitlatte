# ProGuard rules for debug build

-ignorewarnings

-keep class twitter4j.* { *; }
-keep class twitter4j.conf.PropertyConfigurationFactory

-keep class com.github.moko256.twitlatte.** { *; }

-keep class androidx.appcompat.app.AppCompatViewInflater


# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}


-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase