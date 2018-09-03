/*
 * Copyright 2015-2018 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte

import android.os.Build
import android.text.Layout
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.mastodon.MTUser
import com.github.moko256.twitlatte.database.CachedUsersSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.Emoji
import com.github.moko256.twitlatte.entity.Status
import com.github.moko256.twitlatte.entity.Visibility
import com.github.moko256.twitlatte.glide.GlideRequests
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter
import com.github.moko256.twitlatte.widget.TweetImageTableView
import io.reactivex.disposables.CompositeDisposable
import twitter4j.User
import java.util.*

/**
 * Created by moko256 on 2018/09/03.
 *
 * @author moko256
 */
class StatusViewBinder(private val glideRequests: GlideRequests, private val viewGroup: ViewGroup) {
    private val disposable = CompositeDisposable()

    private var hasStatus = false

    private var contextEmojiSetter: EmojiToTextViewSetter? = null
    private var spoilerTextEmojiSetter: EmojiToTextViewSetter? = null
    private var userNameEmojiSetter: EmojiToTextViewSetter? = null

    public val userImage: ImageView = viewGroup.findViewById(R.id.tweet_icon)
    public val replyUserName: TextView = viewGroup.findViewById(R.id.tweet_reply_user_name)
    public val userName: TextView = viewGroup.findViewById(R.id.tweet_user_name)
    public val userId: TextView = viewGroup.findViewById(R.id.tweet_user_id)
    public val tweetSpoilerText: TextView = viewGroup.findViewById(R.id.tweet_spoiler)
    public val contentOpenerToggle: CheckBox = viewGroup.findViewById(R.id.tweet_spoiler_opener)
    public val tweetContext: TextView = viewGroup.findViewById(R.id.tweet_content)
    public val timeStampText: TextView
    public val quoteTweetLayout: RelativeLayout
    public val quoteTweetImages: TweetImageTableView
    public val quoteTweetUserName: TextView
    public val quoteTweetUserId: TextView
    public val quoteTweetContext: TextView
    public val imageTableView: TweetImageTableView
    public val likeButton: CheckBox
    public val retweetButton: CheckBox
    public val replyButton: ImageButton
    public val likeCount: TextView
    public val retweetCount: TextView
    public val repliesCount: TextView

    init {
        contentOpenerToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tweetContext.visibility = View.VISIBLE
            } else {
                tweetContext.visibility = View.GONE
            }
        }
        tweetContext.movementMethod = LinkMovementMethod.getInstance()
        timeStampText = viewGroup.findViewById(R.id.tweet_time_stamp_text)
        quoteTweetLayout = viewGroup.findViewById(R.id.tweet_quote_tweet)
        quoteTweetImages = viewGroup.findViewById(R.id.tweet_quote_images)
        quoteTweetUserName = viewGroup.findViewById(R.id.tweet_quote_tweet_user_name)
        quoteTweetUserId = viewGroup.findViewById(R.id.tweet_quote_tweet_user_id)
        quoteTweetContext = viewGroup.findViewById(R.id.tweet_quote_tweet_content)
        imageTableView = viewGroup.findViewById(R.id.tweet_image_container)

        likeButton = viewGroup.findViewById(R.id.tweet_content_like_button)
        retweetButton = viewGroup.findViewById(R.id.tweet_content_retweet_button)
        replyButton = viewGroup.findViewById(R.id.tweet_content_reply_button)

        likeCount = viewGroup.findViewById(R.id.tweet_content_like_count)
        retweetCount = viewGroup.findViewById(R.id.tweet_content_retweet_count)
        repliesCount = viewGroup.findViewById(R.id.tweet_content_replies_count)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tweetContext.breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
            quoteTweetContext.breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
        }
    }

    fun setStatus(user: User?, status: Status, quotedStatusUser: User? = null, quotedStatus: Status? = null) {
        if (hasStatus) {
            clear()
        }

        updateView(user, status, quotedStatusUser, quotedStatus)
        hasStatus = true
    }

    fun updateView(user: User?, status: Status, quotedStatusUser: User?, quotedStatus: Status?) {

        val isReply = status.inReplyToScreenName != null
        if (isReply) {
            if (replyUserName.visibility != View.VISIBLE) {
                replyUserName.visibility = View.VISIBLE
            }

            replyUserName.text = viewGroup.context.getString(
                    if (status.inReplyToStatusId != -1L)
                        R.string.reply_to
                    else
                        R.string.mention_to,
                    TwitterStringUtils.plusAtMark(status.inReplyToScreenName)
            )
        } else {
            if (replyUserName.visibility != View.GONE) {
                replyUserName.visibility = View.GONE
            }
        }

        val timelineImageLoadMode = GlobalApplication.preferenceRepository.getString(GlobalApplication.KEY_TIMELINE_IMAGE_LOAD_MODE, "normal")
        if (timelineImageLoadMode != "none") {
            glideRequests
                    .load(
                            if (timelineImageLoadMode == "normal")
                                user?.get400x400ProfileImageURLHttps()
                            else
                                user?.miniProfileImageURLHttps
                    )
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userImage)
        } else {
            userImage.setImageResource(R.drawable.border_frame_round)
        }

        val userNameText = TwitterStringUtils.plusUserMarks(
                user?.name,
                userName,
                user?.isProtected == true,
                user?.isVerified == true
        )
        userName.text = userNameText
        var userNameEmojis: MutableList<Emoji>? = null
        if (user is CachedUsersSQLiteOpenHelper.CachedUser) {
            userNameEmojis = user.emojis
        } else if (user is MTUser) {
            val emojis = user.account.emojis
            userNameEmojis = ArrayList(emojis.size)
            for (emoji in emojis) {
                userNameEmojis.add(Emoji(
                        emoji.shortcode,
                        emoji.url
                ))
            }
        }
        if (userNameEmojis != null) {
            if (userNameEmojiSetter == null) {
                userNameEmojiSetter = EmojiToTextViewSetter(glideRequests, userName)
            }
            val set = userNameEmojiSetter!!.set(userNameText, userNameEmojis.toTypedArray())
            if (set != null) {
                disposable.addAll(*set)
            }
        }
        userId.text = TwitterStringUtils.plusAtMark(user?.screenName)

        val linkedSequence = TwitterStringUtils.getLinkedSequence(viewGroup.context, status.text, status.urls)

        tweetContext.text = linkedSequence

        if (!TextUtils.isEmpty(linkedSequence)) {
            if (tweetContext.visibility != View.VISIBLE) {
                tweetContext.visibility = View.VISIBLE
            }

            if (status.emojis != null) {
                if (contextEmojiSetter == null) {
                    contextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetContext)
                }
                val set = contextEmojiSetter!!.set(linkedSequence, status.emojis)
                if (set != null) {
                    disposable.addAll(*set)
                }
            }
        } else {
            if (tweetContext.visibility != View.GONE) {
                tweetContext.visibility = View.GONE
            }
        }

        tweetSpoilerText.text = status.spoilerText
        if (status.spoilerText == null) {
            if (tweetSpoilerText.visibility != View.GONE) {
                tweetSpoilerText.visibility = View.GONE
            }
            if (contentOpenerToggle.visibility != View.GONE) {
                contentOpenerToggle.visibility = View.GONE
            }
        } else {
            contentOpenerToggle.isChecked = false
            if (tweetContext.visibility != View.GONE) {
                tweetContext.visibility = View.GONE
            }
            if (tweetSpoilerText.visibility != View.VISIBLE) {
                tweetSpoilerText.visibility = View.VISIBLE
            }
            if (contentOpenerToggle.visibility != View.VISIBLE) {
                contentOpenerToggle.visibility = View.VISIBLE
            }

            if (status.emojis != null) {
                if (spoilerTextEmojiSetter == null) {
                    spoilerTextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetSpoilerText)
                }
                val set = spoilerTextEmojiSetter!!.set(status.spoilerText, status.emojis)
                if (set != null) {
                    disposable.addAll(*set)
                }
            }
        }

        timeStampText.text = DateUtils.getRelativeTimeSpanString(
                status.createdAt.time,
                System.currentTimeMillis(),
                0
        )

        if (quotedStatus != null && quotedStatusUser != null) {
            if (quoteTweetLayout.visibility != View.VISIBLE) {
                quoteTweetLayout.visibility = View.VISIBLE
            }
            quoteTweetUserName.text = TwitterStringUtils.plusUserMarks(
                    quotedStatusUser.name,
                    quoteTweetUserName,
                    quotedStatusUser.isProtected,
                    quotedStatusUser.isVerified
            )
            if (quotedStatus.medias?.isNotEmpty() == true) {
                if (quoteTweetImages.visibility != View.VISIBLE) {
                    quoteTweetImages.visibility = View.VISIBLE
                }
                quoteTweetImages.setMediaEntities(quotedStatus.medias, quotedStatus.isSensitive)
            } else {
                if (quoteTweetImages.visibility != View.GONE) {
                    quoteTweetImages.visibility = View.GONE
                }
            }
            quoteTweetUserId.text = TwitterStringUtils.plusAtMark(quotedStatusUser.screenName)
            quoteTweetContext.text = quotedStatus.text
        } else {
            if (quoteTweetLayout.visibility != View.GONE) {
                quoteTweetLayout.visibility = View.GONE
            }
        }

        val medias = status.medias

        if (medias?.isNotEmpty() == true) {
            imageTableView.visibility = View.VISIBLE
            imageTableView.setMediaEntities(medias, status.isSensitive)
        } else {
            imageTableView.visibility = View.GONE
        }

        likeButton.isChecked = status.isFavorited

        retweetButton.isChecked = status.isRepeated
        retweetButton.isEnabled = if (status.visibility != null) {
            status.visibility == Visibility.Public.value
                    || status.visibility == Visibility.Unlisted.value
        } else {
            user?.isProtected == false
        }

        likeCount.text = if (status.favoriteCount != 0) TwitterStringUtils.convertToSIUnitString(status.favoriteCount) else ""
        retweetCount.text = if (status.repeatCount != 0) TwitterStringUtils.convertToSIUnitString(status.repeatCount) else ""
        repliesCount.text = if (status.repliesCount != 0) TwitterStringUtils.convertToSIUnitString(status.repliesCount) else ""
    }

    fun clear() {
        disposable.clear()
        hasStatus = false
    }

}