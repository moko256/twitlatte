# ProGuard rules for debug build

-ignorewarnings

-keep class twitter4j.* { *; }
-keep class twitter4j.Logger
-keep class twitter4j.LoggerFactory
-keep class twitter4j.StdOutLogger
-keep class twitter4j.StdOutLoggerFactory
-keep class twitter4j.conf.PropertyConfigurationFactory

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