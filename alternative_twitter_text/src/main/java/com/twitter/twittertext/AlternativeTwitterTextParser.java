// Copyright 2018 Twitter, Inc.
// Licensed under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// Modifications Copyright 2015-2018 The twitlatte authors

package com.twitter.twittertext;

import com.twitter.twittertext.TwitterTextConfiguration.TwitterTextWeightedRange;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class to parse tweet text with a {@link TwitterTextConfiguration} and returns a
 * {@link TwitterTextParseResults} object
 */
public final class AlternativeTwitterTextParser {

    private AlternativeTwitterTextParser() {
    }

    public static final TwitterTextParseResults EMPTY_TWITTER_TEXT_PARSE_RESULTS =
            new TwitterTextParseResults(0, 0, false, Range.EMPTY, Range.EMPTY);

    private static final Extractor EXTRACTOR = new Extractor();

    /**
     * Parses a given tweet text with the given {@link TwitterTextConfiguration}
     *
     * @param tweet which is to be parsed
     * @param config {@link TwitterTextConfiguration}
     * @return {@link TwitterTextParseResults} object
     */
    @Nonnull
    public static TwitterTextParseResults parseTweet(@Nullable final String tweet,
                                                     @Nonnull final TwitterTextConfiguration config) {
        return parseTweet(tweet, config, true);
    }

    /**
     * Parses a given tweet text with the given {@link TwitterTextConfiguration} and
     * optionally control if urls should/shouldn't be normalized to
     * {@link TwitterTextConfiguration.DEFAULT_TRANSFORMED_URL_LENGTH}
     *
     * @param tweet which is to be parsed
     * @param config {@link TwitterTextConfiguration}
     * @param extractURLs boolean indicating if URLs should be extracted for counting
     * @return {@link TwitterTextParseResults} object
     */
    @Nonnull
    private static TwitterTextParseResults parseTweet(@Nullable final String tweet,
                                                      @Nonnull final TwitterTextConfiguration config,
                                                      boolean extractURLs) {
        if (tweet == null || tweet.trim().length() == 0) {
            return EMPTY_TWITTER_TEXT_PARSE_RESULTS;
        }

        final String normalizedTweet = Normalizer.normalize(tweet, Normalizer.Form.NFC);
        final int tweetLength = normalizedTweet.length();

        if (tweetLength == 0) {
            return EMPTY_TWITTER_TEXT_PARSE_RESULTS;
        }

        final int scale = config.getScale();
        final int maxWeightedTweetLength = config.getMaxWeightedTweetLength();
        final int scaledMaxWeightedTweetLength = maxWeightedTweetLength * scale;
        final int transformedUrlWeight = config.getTransformedURLLength() * scale;
        final List<TwitterTextWeightedRange> ranges = config.getRanges();

        final List<Extractor.Entity> urlEntities = EXTRACTOR.extractURLsWithIndices(normalizedTweet);

        boolean hasInvalidCharacters = false;
        int weightedCount = 0;
        int offset = 0;
        int validOffset = 0;

        final Map<Integer, Integer> emojiMap = new HashMap<>();
        if (config.getEmojiParsingEnabled()) {
            final Matcher emojiMatcher = TwitterTextEmojiRegex.VALID_EMOJI_PATTERN
                    .matcher(normalizedTweet);
            while (emojiMatcher.find()) {
                final int start = emojiMatcher.start();
                final int end = emojiMatcher.end();
                emojiMap.put(start, end - start);
            }
        }

        while (offset < tweetLength) {
            int charWeight = config.getDefaultWeight();

            if (extractURLs) {
                final ListIterator<Extractor.Entity> urlEntityIterator = urlEntities.listIterator();
                while (urlEntityIterator.hasNext()) {
                    final Extractor.Entity urlEntity = urlEntityIterator.next();
                    if (urlEntity.start == offset) {
                        final int urlLength = urlEntity.end - urlEntity.start;
                        weightedCount += transformedUrlWeight;
                        offset += urlLength;
                        if (weightedCount <= scaledMaxWeightedTweetLength) {
                            validOffset += urlLength;
                        }
                        urlEntityIterator.remove();
                        break;
                    }
                }
            }

            if (offset < tweetLength) {
                final int codePoint = normalizedTweet.codePointAt(offset);

                int emojiLength = -1;
                if (emojiMap.containsKey(offset)) {
                    charWeight = config.getDefaultWeight();
                    emojiLength = emojiMap.get(offset);
                }

                if (emojiLength == -1) {
                    for (final TwitterTextWeightedRange weightedRange : ranges) {
                        if (weightedRange.getRange().isInRange(codePoint)) {
                            charWeight = weightedRange.getWeight();
                            break;
                        }
                    }
                }

                weightedCount += charWeight;

                hasInvalidCharacters = hasInvalidCharacters ||
                        Validator.hasInvalidCharacters(normalizedTweet.substring(offset, offset + 1));

                final int offsetDelta;
                if (emojiLength != -1) {
                    offsetDelta = emojiLength;
                } else {
                    offsetDelta = Character.charCount(codePoint);
                }
                offset += offsetDelta;
                if (!hasInvalidCharacters && weightedCount <= scaledMaxWeightedTweetLength) {
                    validOffset += offsetDelta;
                }
            }
        }
        final int normalizedTweetOffset = tweet.length() - normalizedTweet.length();
        final int scaledWeightedLength = weightedCount / scale;
        final boolean isValid = !hasInvalidCharacters && scaledWeightedLength <= maxWeightedTweetLength;
        final int permillage = scaledWeightedLength * 1000 / maxWeightedTweetLength;
        return new TwitterTextParseResults(scaledWeightedLength, permillage, isValid,
                new Range(0, offset + normalizedTweetOffset - 1),
                new Range(0, validOffset + normalizedTweetOffset - 1));
    }
}
