# ProGuard rules for debug build

-ignorewarnings

-keep class twitter4j.AlternativeHttpClientImpl {
    public <init>(twitter4j.HttpClientConfiguration);
}
-keep class twitter4j.conf.PropertyConfigurationFactory {
    public <init>();
}

-keep class com.github.moko256.twitlatte.** { *; }

-keep class com.twitter.twittertext.TwitterTextConfiguration {
    private static com.twitter.twittertext.TwitterTextConfiguration getDefaultConfig();
    private boolean emojiParsingEnabled;
}

-keep class androidx.appcompat.app.AppCompatViewInflater


-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase